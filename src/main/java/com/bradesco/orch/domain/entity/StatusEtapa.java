package com.bradesco.orch.domain.entity;

/**
 * Estados possíveis de uma etapa. Apenas a primeira etapa inicia como
 * {@link #PENDENTE}; as demais iniciam como {@link #AGUARDANDO}.
 *
 * <p>{@link #PENDENTE_DE_INTERACAO} representa uma <b>pausa</b> da orquestração
 * em um ponto que exige ação humana. Não é erro nem finalização: a etapa
 * permanece suspensa até que o endpoint de aprovação a retome.</p>
 */
public enum StatusEtapa {
    AGUARDANDO,
    PENDENTE,
    EM_EXECUCAO,
    /** Suspensa aguardando interação humana (ver {@code POST .../aprovar}). */
    PENDENTE_DE_INTERACAO,
    CONCLUIDA,
    ERRO
}
