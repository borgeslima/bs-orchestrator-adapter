package com.bradesco.orch.domain.entity;

/**
 * Estados possíveis de uma etapa. Apenas a primeira etapa inicia como
 * {@link #PENDENTE}; as demais iniciam como {@link #AGUARDANDO}.
 */
public enum StatusEtapa {
    AGUARDANDO,
    PENDENTE,
    EM_EXECUCAO,
    CONCLUIDA,
    ERRO
}
