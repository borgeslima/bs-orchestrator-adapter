package com.bradesco.orch.application;

import com.bradesco.orch.domain.entity.Controle;
import com.bradesco.orch.domain.entity.Credito;
import com.bradesco.orch.domain.entity.Etapa;
import com.bradesco.orch.domain.entity.EtapasCapGiro;
import com.bradesco.orch.domain.entity.HistoricoNegocioCapGiro;
import com.bradesco.orch.domain.entity.Orquestracao;
import com.bradesco.orch.domain.entity.RespostaEtapa;
import com.bradesco.orch.domain.entity.StatusEtapa;
import com.bradesco.orch.domain.entity.StatusOrquestracao;
import com.bradesco.orch.domain.port.in.IniciarOrquestracaoComando;
import com.bradesco.orch.domain.port.in.IniciarOrquestracaoUseCase;
import com.bradesco.orch.domain.port.out.CreditoRepository;
import com.bradesco.orch.domain.port.out.MensagemEtapa;
import com.bradesco.orch.domain.port.out.MensagemPublisher;
import com.bradesco.orch.domain.port.out.OrquestracaoRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Serviço de aplicação que inicia a orquestração {@code baas:cap-giro}: monta o
 * agregado com as 4 etapas na ordem fixa (primeira {@code PENDENTE}, demais
 * {@code AGUARDANDO}), persiste e — só então — publica a mensagem da primeira
 * etapa. Se a persistência falhar, propaga a exceção sem publicar.
 */
@Service
public class IniciarOrquestracaoService implements IniciarOrquestracaoUseCase {

    private final OrquestracaoRepository repository;
    private final CreditoRepository creditoRepository;
    private final MensagemPublisher publisher;

    public IniciarOrquestracaoService(OrquestracaoRepository repository,
                                      CreditoRepository creditoRepository,
                                      MensagemPublisher publisher) {
        this.repository = repository;
        this.creditoRepository = creditoRepository;
        this.publisher = publisher;
    }

    @Override
    public String iniciar(IniciarOrquestracaoComando comando) {
        String correlationId = (comando != null && comando.correlationId() != null)
                ? comando.correlationId()
                : UUID.randomUUID().toString();

        Orquestracao orquestracao = montarOrquestracao();

        // Persiste ANTES de publicar (garante que nao havera mensagem apontando
        // para orquestracao inexistente). Em falha, a excecao propaga sem publicar.
        Orquestracao salva = repository.salvar(orquestracao);

        // Grava o credito de negocio (mesmo id da orquestracao) com o historico
        // inicial PENDENTE, antes de disparar o fluxo assincrono.
        Credito credito = Credito.iniciar(salva.getId(), HistoricoNegocioCapGiro.DESCRICAO_INICIAL, Instant.now());
        creditoRepository.salvar(credito);

        publisher.publicar(new MensagemEtapa(salva.getId(), EtapasCapGiro.OFERTA, correlationId));

        return salva.getId();
    }

    private Orquestracao montarOrquestracao() {
        Instant agora = Instant.now();
        List<Etapa> etapas = new ArrayList<>(4);
        String[] nomes = EtapasCapGiro.NOMES_ORDENADOS;
        for (int i = 0; i < nomes.length; i++) {
            StatusEtapa status = (i == 0) ? StatusEtapa.PENDENTE : StatusEtapa.AGUARDANDO;
            Controle controle = new Controle(0, EtapasCapGiro.LIMITE_RETENTATIVAS_PADRAO);
            etapas.add(new Etapa(nomes[i], i, status, controle, new RespostaEtapa(null)));
        }

        return new Orquestracao(
                UUID.randomUUID().toString(),
                EtapasCapGiro.NOME_ORQUESTRACAO,
                0,
                StatusOrquestracao.PENDENTE,
                agora,
                agora,
                null,
                etapas
        );
    }
}
