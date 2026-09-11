package com.bradesco.orch.domain.port.in;

/**
 * Comando de entrada para processar uma etapa de uma orquestração, derivado da
 * mensagem recebida do Service Bus.
 */
public record ProcessarEtapaComando(String orquestracaoId, String etapa, String correlationId) {
}
