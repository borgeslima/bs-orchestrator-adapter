package com.bradesco.orch.domain.entity;

/**
 * Mapa do vocabulário de <b>negócio</b> do cap-giro: dada uma etapa técnica da
 * orquestração, define a {@link SituacaoCredito} e a descrição que devem ser
 * registradas no {@code historico} do {@link Credito}.
 *
 * <p>Isola a tradução "etapa técnica -> linguagem de negócio" em um único ponto,
 * mantendo os serviços de aplicação livres de strings espalhadas.</p>
 */
public final class HistoricoNegocioCapGiro {

    private HistoricoNegocioCapGiro() {
    }

    /** Descrição da primeira entrada do histórico, gravada ao iniciar o crédito. */
    public static final String DESCRICAO_INICIAL = "Solicitação de oferta iniciada";

    /**
     * Situação de negócio correspondente à conclusão da etapa informada. A última
     * etapa ({@code FORMALIZACAO}) conclui o crédito; as demais o mantêm em
     * andamento.
     */
    public static SituacaoCredito situacaoAoConcluir(String etapa) {
        if (EtapasCapGiro.FORMALIZACAO.equals(etapa)) {
            return SituacaoCredito.CONCLUIDA;
        }
        return SituacaoCredito.EM_ANDAMENTO;
    }

    /** Descrição de negócio correspondente à conclusão da etapa informada. */
    public static String descricaoAoConcluir(String etapa) {
        if (EtapasCapGiro.OFERTA.equals(etapa)) {
            return "Oferta processada";
        }
        if (EtapasCapGiro.ELEGIBILIDADE.equals(etapa)) {
            return "Elegibilidade avaliada";
        }
        if (EtapasCapGiro.SIMULACAO.equals(etapa)) {
            return "Simulação concluída";
        }
        if (EtapasCapGiro.FORMALIZACAO.equals(etapa)) {
            return "Crédito formalizado";
        }
        return "Etapa " + etapa + " concluída";
    }
}
