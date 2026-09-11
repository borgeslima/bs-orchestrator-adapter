package com.bradesco.orch.domain.port.out;

/**
 * Contrato leve transportado pelo Service Bus entre etapas. Carrega apenas o
 * necessário para o consumer recuperar o estado no MongoDB — nunca o documento
 * completo da orquestração.
 */
public record MensagemEtapa(String orquestracaoId, String etapa, String correlationId) {
}
