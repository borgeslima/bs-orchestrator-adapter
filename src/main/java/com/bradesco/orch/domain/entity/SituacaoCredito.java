package com.bradesco.orch.domain.entity;

/**
 * Situações de negócio de um {@link Credito} (capital de giro), registradas no
 * {@code historico}. Diferente de {@link StatusOrquestracao}/{@link StatusEtapa},
 * que são estados técnicos do motor de orquestração — aqui o vocabulário é do
 * domínio de negócio.
 */
public enum SituacaoCredito {
    PENDENTE,
    EM_ANDAMENTO,
    CONCLUIDA,
    ERRO
}
