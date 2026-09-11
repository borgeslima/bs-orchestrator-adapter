package com.bradesco.orch.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes do motor de transição de {@link Orquestracao} relativos à interação
 * humana: suspensão em {@code PENDENTE_DE_INTERACAO} e retomada via aprovação.
 */
class OrquestracaoInteracaoHumanaTest {

    private static final String ETAPA_1 = "etapa-1";
    private static final String ETAPA_2 = "etapa-2";

    private Orquestracao criarOrquestracaoComDuasEtapas() {
        Etapa etapa1 = new Etapa(ETAPA_1, 0, StatusEtapa.PENDENTE, new Controle(0, 3), new RespostaEtapa(null));
        Etapa etapa2 = new Etapa(ETAPA_2, 1, StatusEtapa.AGUARDANDO, new Controle(0, 3), new RespostaEtapa(null));
        return new Orquestracao("id-1", "orq", 0, StatusOrquestracao.PENDENTE,
                Instant.now(), Instant.now(), 0L, List.of(etapa1, etapa2));
    }

    @Test
    void suspenderPorInteracao_marcaEtapaPendenteDeInteracao_semAvancarNemFinalizarOrquestracao() {
        Orquestracao orquestracao = criarOrquestracaoComDuasEtapas();

        orquestracao.suspenderPorInteracao(ETAPA_1, "resposta-parcial");

        Etapa etapa1 = orquestracao.etapaAtual(ETAPA_1);
        Etapa etapa2 = orquestracao.etapaAtual(ETAPA_2);

        assertThat(etapa1.getStatus()).isEqualTo(StatusEtapa.PENDENTE_DE_INTERACAO);
        assertThat(etapa1.getCallback().getResponse()).isEqualTo("resposta-parcial");
        // A proxima etapa NAO deve ser transicionada nem a orquestracao finalizada.
        assertThat(etapa2.getStatus()).isEqualTo(StatusEtapa.AGUARDANDO);
        assertThat(orquestracao.getStatus()).isEqualTo(StatusOrquestracao.EM_EXECUCAO);
        assertThat(orquestracao.getStatus()).isNotEqualTo(StatusOrquestracao.ERRO);
        assertThat(orquestracao.getStatus()).isNotEqualTo(StatusOrquestracao.CONCLUIDA);
    }

    @Test
    void retomarPorInteracao_transicionaDePendenteDeInteracaoParaPendente_eRegistraAprovacao() {
        Orquestracao orquestracao = criarOrquestracaoComDuasEtapas();
        orquestracao.suspenderPorInteracao(ETAPA_1, "resposta-parcial");

        RegistroInteracao registro = new RegistroInteracao("analista", Instant.now(), "ok");
        orquestracao.retomarPorInteracao(ETAPA_1, registro);

        Etapa etapa1 = orquestracao.etapaAtual(ETAPA_1);
        assertThat(etapa1.getStatus()).isEqualTo(StatusEtapa.PENDENTE);
        assertThat(etapa1.getInteracao()).isEqualTo(registro);
    }

    @Test
    void retomarPorInteracao_lancaExcecao_quandoEtapaNaoEstaAguardandoInteracao() {
        Orquestracao orquestracao = criarOrquestracaoComDuasEtapas();
        // ETAPA_1 esta PENDENTE (nunca foi suspensa) — transicao invalida.
        RegistroInteracao registro = new RegistroInteracao("analista", Instant.now(), null);

        assertThatThrownBy(() -> orquestracao.retomarPorInteracao(ETAPA_1, registro))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void retomarPorInteracao_lancaExcecao_quandoEtapaJaConcluida() {
        Orquestracao orquestracao = criarOrquestracaoComDuasEtapas();
        orquestracao.suspenderPorInteracao(ETAPA_1, "r1");
        orquestracao.retomarPorInteracao(ETAPA_1, new RegistroInteracao("a", Instant.now(), null));
        // Simula a etapa concluindo apos a retomada (fluxo normal do motor).
        orquestracao.concluirEtapaEAvancar(ETAPA_1, "final");

        assertThatThrownBy(() -> orquestracao.retomarPorInteracao(ETAPA_1,
                new RegistroInteracao("b", Instant.now(), null)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void aguardaInteracao_refleteOEstadoDaEtapa() {
        Orquestracao orquestracao = criarOrquestracaoComDuasEtapas();
        Etapa etapa1 = orquestracao.etapaAtual(ETAPA_1);
        assertThat(etapa1.aguardaInteracao()).isFalse();

        orquestracao.suspenderPorInteracao(ETAPA_1, null);
        assertThat(orquestracao.etapaAtual(ETAPA_1).aguardaInteracao()).isTrue();
    }
}
