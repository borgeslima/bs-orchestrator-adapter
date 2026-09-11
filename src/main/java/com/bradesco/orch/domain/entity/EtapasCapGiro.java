package com.bradesco.orch.domain.entity;

/**
 * Nomes canônicos e ordem fixa das 4 etapas concretas da orquestração
 * {@code baas:cap-giro} (POC). A ordem é imutável: OFERTA (0), ELEGIBILIDADE (1),
 * SIMULACAO (2), FORMALIZACAO (3).
 */
public final class EtapasCapGiro {

    private EtapasCapGiro() {
    }

    /** Nome da orquestração de referência da POC. */
    public static final String NOME_ORQUESTRACAO = "baas:cap-giro";

    public static final String OFERTA = "baas:etapa:cap-giro:oferta";
    public static final String ELEGIBILIDADE = "baas:etapa:cap-giro:elegibilidade";
    public static final String SIMULACAO = "baas:etapa:cap-giro:simulacao";
    public static final String FORMALIZACAO = "baas:etapa:cap-giro:formalizacao";

    public static final int ORDEM_OFERTA = 0;
    public static final int ORDEM_ELEGIBILIDADE = 1;
    public static final int ORDEM_SIMULACAO = 2;
    public static final int ORDEM_FORMALIZACAO = 3;

    /** Limite default de retentativas por etapa nesta POC. */
    public static final int LIMITE_RETENTATIVAS_PADRAO = 3;

    /** Nomes na ordem canônica de execução. */
    public static final String[] NOMES_ORDENADOS = {OFERTA, ELEGIBILIDADE, SIMULACAO, FORMALIZACAO};
}
