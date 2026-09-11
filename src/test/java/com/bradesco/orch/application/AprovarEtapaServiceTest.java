package com.bradesco.orch.application;

import com.bradesco.orch.domain.entity.Controle;
import com.bradesco.orch.domain.entity.Etapa;
import com.bradesco.orch.domain.entity.Orquestracao;
import com.bradesco.orch.domain.entity.RegistroInteracao;
import com.bradesco.orch.domain.entity.RespostaEtapa;
import com.bradesco.orch.domain.entity.StatusEtapa;
import com.bradesco.orch.domain.entity.StatusOrquestracao;
import com.bradesco.orch.domain.port.in.AprovarEtapaComando;
import com.bradesco.orch.domain.port.in.ResultadoAprovacao;
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
 * Testes do {@link AprovarEtapaService}: idempotência do endpoint de aprovação e
 * validação de transições de estado inválidas.
 */
@ExtendWith(MockitoExtension.class)
class AprovarEtapaServiceTest {

    private static final String ORQ_ID = "orq-1";
    private static final String ETAPA = "etapa-1";

    @Mock
    private OrquestracaoRepository repository;
    @Mock
    private MensagemPublisher publisher;

    private AprovarEtapaService service;

    @BeforeEach
    void setUp() {
        service = new AprovarEtapaService(repository, publisher);
    }

    private Orquestracao orquestracaoComEtapa(StatusEtapa status) {
        Etapa etapa = new Etapa(ETAPA, 0, status, new Controle(0, 3), new RespostaEtapa(null));
        return new Orquestracao(ORQ_ID, "orq", 0, StatusOrquestracao.EM_EXECUCAO,
                Instant.now(), Instant.now(), 0L, List.of(etapa));
    }

    @Test
    void aprovar_transicionaERepublica_quandoEtapaAguardaInteracao() {
        Orquestracao orquestracao = orquestracaoComEtapa(StatusEtapa.PENDENTE_DE_INTERACAO);
        when(repository.buscarPorId(ORQ_ID)).thenReturn(Optional.of(orquestracao));
        when(repository.transicionarEtapaDeInteracaoParaPendente(eq(ORQ_ID), eq(ETAPA), any()))
                .thenReturn(true);

        ResultadoAprovacao resultado = service.aprovar(
                new AprovarEtapaComando(ORQ_ID, ETAPA, "analista", "ok", "corr-1"));

        assertThat(resultado).isEqualTo(ResultadoAprovacao.APROVADA);
        verify(publisher).publicar(any());
    }

    @Test
    void aprovar_retornaJaProcessada_quandoEtapaJaConcluida_naoExecutaNovamente() {
        Orquestracao orquestracao = orquestracaoComEtapa(StatusEtapa.CONCLUIDA);
        when(repository.buscarPorId(ORQ_ID)).thenReturn(Optional.of(orquestracao));

        ResultadoAprovacao resultado = service.aprovar(
                new AprovarEtapaComando(ORQ_ID, ETAPA, "analista", null, "corr-1"));

        assertThat(resultado).isEqualTo(ResultadoAprovacao.JA_PROCESSADA);
        verify(repository, never()).transicionarEtapaDeInteracaoParaPendente(any(), any(), any());
        verify(publisher, never()).publicar(any());
    }

    @Test
    void aprovar_ehIdempotente_segundaChamadaNaoPublicaDeNovo() {
        // Primeira chamada: etapa ainda PENDENTE_DE_INTERACAO, vence a transicao.
        Orquestracao orquestracao = orquestracaoComEtapa(StatusEtapa.PENDENTE_DE_INTERACAO);
        when(repository.buscarPorId(ORQ_ID)).thenReturn(Optional.of(orquestracao));
        when(repository.transicionarEtapaDeInteracaoParaPendente(eq(ORQ_ID), eq(ETAPA), any()))
                .thenReturn(true);

        ResultadoAprovacao primeira = service.aprovar(
                new AprovarEtapaComando(ORQ_ID, ETAPA, "analista", null, "corr-1"));
        assertThat(primeira).isEqualTo(ResultadoAprovacao.APROVADA);

        // Segunda chamada (mesma aprovacao reenviada): a etapa em memoria simulando
        // o estado real ja avancou para PENDENTE (nao esta mais aguardando interacao).
        Orquestracao orquestracaoAposPrimeira = orquestracaoComEtapa(StatusEtapa.PENDENTE);
        when(repository.buscarPorId(ORQ_ID)).thenReturn(Optional.of(orquestracaoAposPrimeira));

        ResultadoAprovacao segunda = service.aprovar(
                new AprovarEtapaComando(ORQ_ID, ETAPA, "analista", null, "corr-1"));

        assertThat(segunda).isEqualTo(ResultadoAprovacao.JA_PROCESSADA);
        // Publica apenas uma vez (na primeira aprovacao), nunca duas.
        verify(publisher, org.mockito.Mockito.times(1)).publicar(any());
    }

    @Test
    void aprovar_perdeACorridaNaTransicaoAtomica_retornaJaProcessada() {
        Orquestracao orquestracao = orquestracaoComEtapa(StatusEtapa.PENDENTE_DE_INTERACAO);
        when(repository.buscarPorId(ORQ_ID)).thenReturn(Optional.of(orquestracao));
        when(repository.transicionarEtapaDeInteracaoParaPendente(eq(ORQ_ID), eq(ETAPA), any()))
                .thenReturn(false);

        ResultadoAprovacao resultado = service.aprovar(
                new AprovarEtapaComando(ORQ_ID, ETAPA, "analista", null, "corr-1"));

        assertThat(resultado).isEqualTo(ResultadoAprovacao.JA_PROCESSADA);
        verify(publisher, never()).publicar(any());
    }

    @Test
    void aprovar_retornaNaoEncontrada_quandoOrquestracaoInexistente() {
        when(repository.buscarPorId(ORQ_ID)).thenReturn(Optional.empty());

        ResultadoAprovacao resultado = service.aprovar(
                new AprovarEtapaComando(ORQ_ID, ETAPA, "analista", null, "corr-1"));

        assertThat(resultado).isEqualTo(ResultadoAprovacao.NAO_ENCONTRADA);
    }

    @Test
    void aprovar_retornaNaoEncontrada_quandoEtapaInexistenteNaOrquestracao() {
        Orquestracao orquestracao = orquestracaoComEtapa(StatusEtapa.PENDENTE_DE_INTERACAO);
        when(repository.buscarPorId(ORQ_ID)).thenReturn(Optional.of(orquestracao));

        ResultadoAprovacao resultado = service.aprovar(
                new AprovarEtapaComando(ORQ_ID, "etapa-inexistente", "analista", null, "corr-1"));

        assertThat(resultado).isEqualTo(ResultadoAprovacao.NAO_ENCONTRADA);
    }
}
