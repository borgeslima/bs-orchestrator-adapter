package com.bradesco.orch.domain.entity;

/**
 * Origem da interação que retomou uma etapa suspensa em
 * {@link StatusEtapa#PENDENTE_DE_INTERACAO}.
 *
 * <ul>
 *   <li>{@link #HUMANA} — aprovação disparada por um ator humano (ex.: endpoint REST).</li>
 *   <li>{@link #MAQUINA} — retomada disparada por um sistema/automação.</li>
 * </ul>
 */
public enum TipoInteracao {
    HUMANA,
    MAQUINA
}
