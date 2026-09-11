package com.bradesco.orch.domain.port.in;

/**
 * Comando de entrada para aprovar (retomar) uma etapa suspensa em
 * {@code PENDENTE_DE_INTERACAO}.
 *
 * @param orquestracaoId id da orquestração
 * @param etapa          nome canônico da etapa a retomar
 * @param aprovadoPor    identificador de quem aprovou (opcional)
 * @param observacao     observação livre da aprovação (opcional)
 * @param correlationId  id de rastreamento ponta a ponta (opcional; gerado se ausente)
 */
public record AprovarEtapaComando(String orquestracaoId, String etapa, String aprovadoPor,
                                   String observacao, String correlationId) {
}
