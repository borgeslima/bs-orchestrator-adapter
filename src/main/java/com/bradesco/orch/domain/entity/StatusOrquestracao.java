package com.bradesco.orch.domain.entity;

/**
 * Estados possíveis de uma orquestração (máquina de estados raiz).
 */
public enum StatusOrquestracao {
    PENDENTE,
    EM_EXECUCAO,
    CONCLUIDA,
    ERRO
}
