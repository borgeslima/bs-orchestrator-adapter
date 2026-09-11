package com.bradesco.orch.application;

import com.bradesco.orch.domain.entity.Controle;
import com.bradesco.orch.domain.entity.Etapa;
import com.bradesco.orch.domain.entity.Orquestracao;
import com.bradesco.orch.domain.entity.RegistroInteracao;
import com.bradesco.orch.domain.entity.RespostaEtapa;
import com.bradesco.orch.domain.entity.StatusEtapa;
import com.bradesco.orch.domain.entity.StatusOrquestracao;
import com.bradesco.orch.domain.port.in.ProcessarEtapaComando;
import com.bradesco.orch.domain.port.in.ResultadoProcessamento;
import com.bradesco.orch.domain.port.out.EtapaProcessor;
import com.bradesco.orch.domain.port.out.EtapaProcessorRegistry;
import com.bradesco.orch.domain.port.out.InteracaoHumanaPolicy;
import com.bradesco.orch.domain.port.out.MensagemPublisher;
import com.bradesco.orch.domain.port.out.OrquestracaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes do {@link ProcessarEtapaService} focados no comportamento de
 * suspensão por interação humana e na retomada sem suspender novamente.
 */
@ExtendWith(MockitoExtension.class)
class ProcessarEtapaServiceInteracaoHumanaTest {

    private static final String ORQ_ID = "orq-1";
    private static final String ETAPA = "etapa-1";

    @Mock
    private OrquestracaoRepository repository;
    @Mock
    private EtapaProcessorRegistry registry;
    @Mock
    private MensagemPublisher publisher;
    @Mock
    private InteracaoHumanaPolicy interacaoHumanaPolicy;
    @Mock
    private EtapaProcessor<Object, Object> processor;

    private ProcessarEtapaService service;

    @BeforeEach
    void setUp() {
        service = new ProcessarEtapaService(repository, registry, publisher, interacaoHumanaPolicy);
    }

    private Orquestracao orquestracaoComEtapaPendente(RegistroInteracao interacao) {
        Etapa etapa = new Etapa(ETAPA, 0, StatusEtapa.PENDENTE, new Controle(0, 3), new RespostaEtapa(null), interacao);
        return new Orquestracao(ORQ_ID, "orq", 0, StatusOrquestracao.PENDENTE,
                Instant.now(), Instant.now(), 0L, List.of(etapa));
    }

    @Test
    @SuppressWarnings("unchecked")
    void processar_suspendeEtapa_quandoPolicyExigeInteracao_eNaoPublicaProximaEtapa() {
        Orquestracao orquestracao = orquestracaoComEtapaPendente(null);
        when(repository.buscarPorId(ORQ_ID)).thenReturn(Optional.of(orquestracao));
        when(registry.localizar(ETAPA)).thenReturn(Optional.of((EtapaProcessor) processor));
        when(repository.transicionarEtapaParaEmExecucao(ORQ_ID, ETAPA)).thenReturn(true);
        when(processor.execute(any())).thenReturn("resposta");
        when(interacaoHumanaPolicy.requerInteracao(ETAPA, "resposta")).thenReturn(true);

        ResultadoProcessamento resultado = service.processar(new ProcessarEtapaComando(ORQ_ID, ETAPA, "corr-1"));

        assertThat(resultado).isEqualTo(ResultadoProcessamento.AGUARDANDO_INTERACAO);
        assertThat(orquestracao.etapaAtual(ETAPA).getStatus()).isEqualTo(StatusEtapa.PENDENTE_DE_INTERACAO);
        verify(repository).atualizar(orquestracao);
        verify(publisher, never()).publicar(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void processar_naoSuspendeDeNovo_quandoEtapaJaFoiAprovada_eSeguemFluxoNormal() {
        RegistroInteracao registro = new RegistroInteracao("analista", Instant.now(), null);
        Orquestracao orquestracao = orquestracaoComEtapaPendente(registro);
        when(repository.buscarPorId(ORQ_ID)).thenReturn(Optional.of(orquestracao));
        when(registry.localizar(ETAPA)).thenReturn(Optional.of((EtapaProcessor) processor));
        when(repository.transicionarEtapaParaEmExecucao(ORQ_ID, ETAPA)).thenReturn(true);
        when(processor.execute(any())).thenReturn("resposta-final");

        ResultadoProcessamento resultado = service.processar(new ProcessarEtapaComando(ORQ_ID, ETAPA, "corr-1"));

        // Mesmo com policy nao mockada para retornar true, o motor nao deve
        // nem consultar a policy quando a etapa ja foi aprovada (interacao != null).
        assertThat(resultado).isEqualTo(ResultadoProcessamento.SUCESSO);
        assertThat(orquestracao.etapaAtual(ETAPA).getStatus()).isEqualTo(StatusEtapa.CONCLUIDA);
        verify(interacaoHumanaPolicy, never()).requerInteracao(eq(ETAPA), any());
    }

    @Test
    void processar_tratacomoDuplicidade_quandoEtapaAguardaInteracao() {
        Etapa etapa = new Etapa(ETAPA, 0, StatusEtapa.PENDENTE_DE_INTERACAO, new Controle(0, 3),
                new RespostaEtapa(null));
        Orquestracao orquestracao = new Orquestracao(ORQ_ID, "orq", 0, StatusOrquestracao.EM_EXECUCAO,
                Instant.now(), Instant.now(), 0L, List.of(etapa));
        when(repository.buscarPorId(ORQ_ID)).thenReturn(Optional.of(orquestracao));
        when(registry.localizar(ETAPA)).thenReturn(Optional.of((EtapaProcessor<?, ?>) processor));

        ResultadoProcessamento resultado = service.processar(new ProcessarEtapaComando(ORQ_ID, ETAPA, "corr-1"));

        assertThat(resultado).isEqualTo(ResultadoProcessamento.DUPLICIDADE);
        verify(repository, never()).transicionarEtapaParaEmExecucao(any(), any());
    }
}
