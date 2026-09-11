package com.bradesco.orch.application;

import com.bradesco.orch.domain.entity.Etapa;
import com.bradesco.orch.domain.entity.Orquestracao;
import com.bradesco.orch.domain.entity.StatusEtapa;
import com.bradesco.orch.domain.port.in.ProcessarEtapaComando;
import com.bradesco.orch.domain.port.in.ProcessarEtapaUseCase;
import com.bradesco.orch.domain.port.in.ResultadoProcessamento;
import com.bradesco.orch.domain.port.out.EtapaProcessor;
import com.bradesco.orch.domain.port.out.EtapaProcessorRegistry;
import com.bradesco.orch.domain.port.out.InteracaoHumanaPolicy;
import com.bradesco.orch.domain.port.out.MensagemEtapa;
import com.bradesco.orch.domain.port.out.MensagemPublisher;
import com.bradesco.orch.domain.port.out.OrquestracaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Motor de transição de etapas (aplicação). Orquestra as ports para: idempotência
 * por estado, transição atômica {@code PENDENTE -> EM_EXECUCAO}, execução do
 * processor, persistência do resultado, avanço/conclusão, política de retry e
 * suspensão por interação humana ({@code PENDENTE_DE_INTERACAO}).
 */
@Service
public class ProcessarEtapaService implements ProcessarEtapaUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessarEtapaService.class);

    private final OrquestracaoRepository repository;
    private final EtapaProcessorRegistry registry;
    private final MensagemPublisher publisher;
    private final InteracaoHumanaPolicy interacaoHumanaPolicy;

    public ProcessarEtapaService(OrquestracaoRepository repository,
                                 EtapaProcessorRegistry registry,
                                 MensagemPublisher publisher,
                                 InteracaoHumanaPolicy interacaoHumanaPolicy) {
        this.repository = repository;
        this.registry = registry;
        this.publisher = publisher;
        this.interacaoHumanaPolicy = interacaoHumanaPolicy;
    }

    @Override
    public ResultadoProcessamento processar(ProcessarEtapaComando comando) {
        // 1. Recupera o estado (fonte da verdade)
        Optional<Orquestracao> encontrada = repository.buscarPorId(comando.orquestracaoId());
        if (encontrada.isEmpty()) {
            log.warn("Orquestracao nao encontrada: {}", comando.orquestracaoId());
            return ResultadoProcessamento.ERRO_FINAL;
        }
        Orquestracao orquestracao = encontrada.get();

        // 2. Localiza a etapa pelo nome
        Etapa etapa = orquestracao.etapaAtual(comando.etapa());
        if (etapa == null) {
            log.warn("Etapa {} inexistente na orquestracao {}", comando.etapa(), comando.orquestracaoId());
            return ResultadoProcessamento.ERRO_FINAL;
        }

        // 3. Localiza o processor no registry
        Optional<EtapaProcessor<?, ?>> processorOpt = registry.localizar(comando.etapa());
        if (processorOpt.isEmpty()) {
            log.warn("Processor nao encontrado para etapa {}", comando.etapa());
            orquestracao.marcarErro(comando.etapa());
            repository.atualizar(orquestracao);
            return ResultadoProcessamento.PROCESSOR_NAO_ENCONTRADO;
        }

        // 4. Guarda de idempotencia por estado
        StatusEtapa statusAtual = etapa.getStatus();
        if (statusAtual == StatusEtapa.CONCLUIDA || statusAtual == StatusEtapa.EM_EXECUCAO) {
            log.info("Etapa {} em estado {} -> tratada como duplicidade", comando.etapa(), statusAtual);
            return ResultadoProcessamento.DUPLICIDADE;
        }
        if (statusAtual == StatusEtapa.PENDENTE_DE_INTERACAO) {
            // Suspensa aguardando aprovacao humana: mensagem fora de ordem (ex.: retry
            // antigo do Service Bus). Nao e erro; apenas nao reprocessa ainda.
            log.info("Etapa {} aguardando interacao humana -> ignorada (duplicidade)", comando.etapa());
            return ResultadoProcessamento.DUPLICIDADE;
        }
        if (statusAtual != StatusEtapa.PENDENTE) {
            // AGUARDANDO / ERRO: mensagem fora de ordem ou etapa ja falhada.
            log.info("Etapa {} em estado {} -> ignorada (duplicidade)", comando.etapa(), statusAtual);
            return ResultadoProcessamento.DUPLICIDADE;
        }

        // 5. Transicao atomica condicional PENDENTE -> EM_EXECUCAO (+tentativas, +version)
        boolean venceu = repository.transicionarEtapaParaEmExecucao(comando.orquestracaoId(), comando.etapa());
        if (!venceu) {
            log.info("Perdeu a corrida na transicao da etapa {} -> duplicidade", comando.etapa());
            return ResultadoProcessamento.DUPLICIDADE;
        }

        // Recarrega o estado apos a transicao atomica (tentativas/version atualizados)
        orquestracao = repository.buscarPorId(comando.orquestracaoId()).orElse(orquestracao);
        etapa = orquestracao.etapaAtual(comando.etapa());

        // 6. Executa o processor + callback
        return executarProcessor(orquestracao, etapa, processorOpt.get(), comando);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ResultadoProcessamento executarProcessor(Orquestracao orquestracao,
                                                     Etapa etapa,
                                                     EtapaProcessor processor,
                                                     ProcessarEtapaComando comando) {
        try {
            Object input = processor.execute(null);
            processor.callback(input);

            // Ja retomada por aprovacao humana nesta rodada: nao suspende de novo,
            // segue direto para a conclusao/avanco normal.
            boolean jaAprovada = etapa.getInteracao() != null;
            if (!jaAprovada && interacaoHumanaPolicy.requerInteracao(comando.etapa(), input)) {
                // Suspende no ponto: NAO conclui, NAO avanca, NAO publica proxima etapa.
                orquestracao.suspenderPorInteracao(comando.etapa(), input);
                repository.atualizar(orquestracao);
                log.info("Etapa {} suspensa aguardando interacao humana (PENDENTE_DE_INTERACAO)",
                        comando.etapa());
                return ResultadoProcessamento.AGUARDANDO_INTERACAO;
            }

            // Sucesso: conclui a etapa (grava callback.response) e avanca/conclui
            Optional<Etapa> proxima = orquestracao.concluirEtapaEAvancar(comando.etapa(), input);
            repository.atualizar(orquestracao);

            proxima.ifPresent(prox -> publisher.publicar(
                    new MensagemEtapa(orquestracao.getId(), prox.getName(), comando.correlationId())));

            return ResultadoProcessamento.SUCESSO;
        } catch (RuntimeException erro) {
            return tratarFalha(orquestracao, etapa, comando, erro);
        }
    }

    private ResultadoProcessamento tratarFalha(Orquestracao orquestracao,
                                               Etapa etapa,
                                               ProcessarEtapaComando comando,
                                               RuntimeException erro) {
        // A transicao atomica ja incrementou tentativas_realizadas.
        if (etapa.atingiuLimite()) {
            log.warn("Etapa {} atingiu o limite de tentativas -> ERRO_FINAL", comando.etapa(), erro);
            orquestracao.marcarErro(comando.etapa());
            repository.atualizar(orquestracao);
            return ResultadoProcessamento.ERRO_FINAL;
        }
        log.info("Falha transitoria na etapa {} (tentativa {}/{}) -> RETENTAR",
                comando.etapa(),
                etapa.getControle().getTentativasRealizadas(),
                etapa.getControle().getLimiteRetentativas());
        // Volta a etapa para PENDENTE para reprocessamento via Service Bus
        etapa.setStatus(StatusEtapa.PENDENTE);
        repository.atualizar(orquestracao);
        return ResultadoProcessamento.RETENTAR;
    }
}
