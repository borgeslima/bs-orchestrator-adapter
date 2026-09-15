package com.bradesco.orch.domain.port.in;

import com.bradesco.orch.domain.entity.TipoInteracao;

import java.util.Map;

/**
 * Comando de entrada para aprovar (retomar) uma etapa suspensa em
 * {@code PENDENTE_DE_INTERACAO}.
 *
 * @param orquestracaoId id da orquestração
 * @param etapa          nome canônico da etapa a retomar
 * @param tipo           origem da interação (humana/máquina); assume {@code HUMANA} se ausente
 * @param dados          dados de negócio da retomada (opcional; ex.: {@code idSimulacao})
 * @param correlationId  id de rastreamento ponta a ponta (opcional; gerado se ausente)
 */
public record AprovarEtapaComando(String orquestracaoId, String etapa, TipoInteracao tipo,
                                   Map<String, Object> dados, String correlationId) {
}
