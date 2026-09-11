package com.bradesco.orch.application;

import com.bradesco.orch.domain.entity.Etapa;
import com.bradesco.orch.domain.entity.Orquestracao;
import com.bradesco.orch.domain.entity.RegistroInteracao;
import com.bradesco.orch.domain.entity.StatusEtapa;
import com.bradesco.orch.domain.port.in.AprovarEtapaComando;
import com.bradesco.orch.domain.port.in.AprovarEtapaUseCase;
import com.bradesco.orch.domain.port.in.ResultadoAprovacao;
import com.bradesco.orch.domain.port.out.MensagemEtapa;
import com.bradesco.orch.domain.port.out.MensagemPublisher;
import com.bradesco.orch.domain.port.out.OrquestracaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Aprova (retoma) uma etapa suspensa por interação humana.
 *
 * <p>Não executa a etapa de forma síncrona: apenas registra a interação e
 * transiciona atomicamente {@code PENDENTE_DE_INTERACAO -> PENDENTE}, publicando
 * uma mensagem para a <b>mesma etapa</b> no Service Bus. O
 * {@code ProcessarEtapaService} reprocessa essa etapa pela via assíncrona normal
 * e, encontrando {@code interacao} preenchida, conclui e avança o fluxo sem
 * suspender novamente — a mesma máquina de orquestração continua, sem criar uma
 * segunda execução independente.</p>
 */
@Service
public class AprovarEtapaService implements AprovarEtapaUseCase {

    private static final Logger log = LoggerFactory.getLogger(AprovarEtapaService.class);

    private final OrquestracaoRepository repository;
    private final MensagemPublisher publisher;

    public AprovarEtapaService(OrquestracaoRepository repository, MensagemPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Override
    public ResultadoAprovacao aprovar(AprovarEtapaComando comando) {
        Optional<Orquestracao> encontrada = repository.buscarPorId(comando.orquestracaoId());
        if (encontrada.isEmpty()) {
            log.warn("Orquestracao nao encontrada para aprovacao: {}", comando.orquestracaoId());
            return ResultadoAprovacao.NAO_ENCONTRADA;
        }
        Orquestracao orquestracao = encontrada.get();

        Etapa etapa = orquestracao.etapaAtual(comando.etapa());
        if (etapa == null) {
            log.warn("Etapa {} inexistente na orquestracao {}", comando.etapa(), comando.orquestracaoId());
            return ResultadoAprovacao.NAO_ENCONTRADA;
        }

        // Guarda de idempotencia por estado: se a etapa ja nao esta mais
        // PENDENTE_DE_INTERACAO (ja aprovada, em execucao ou concluida), nao
        // dispara nova execucao nem publica novamente.
        if (etapa.getStatus() != StatusEtapa.PENDENTE_DE_INTERACAO) {
            log.info("Etapa {} em estado {} -> aprovacao ja processada (idempotente)",
                    comando.etapa(), etapa.getStatus());
            return ResultadoAprovacao.JA_PROCESSADA;
        }

        RegistroInteracao registro = new RegistroInteracao(
                comando.aprovadoPor(), Instant.now(), comando.observacao());

        // Transicao atomica condicional PENDENTE_DE_INTERACAO -> PENDENTE. So UMA
        // chamada concorrente vence; as demais tratam como ja processada.
        boolean venceu = repository.transicionarEtapaDeInteracaoParaPendente(
                comando.orquestracaoId(), comando.etapa(), registro);
        if (!venceu) {
            log.info("Perdeu a corrida na aprovacao da etapa {} -> ja processada", comando.etapa());
            return ResultadoAprovacao.JA_PROCESSADA;
        }

        publisher.publicar(new MensagemEtapa(comando.orquestracaoId(), comando.etapa(), comando.correlationId()));
        log.info("Etapa {} aprovada e republicada para retomada", comando.etapa());
        return ResultadoAprovacao.APROVADA;
    }
}
