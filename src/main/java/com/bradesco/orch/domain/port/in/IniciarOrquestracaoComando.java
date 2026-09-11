package com.bradesco.orch.domain.port.in;

/**
 * Comando de entrada para iniciar uma orquestração {@code baas:cap-giro}.
 *
 * <p>Na POC, o payload de negócio é opcional; o {@code correlationId} permite
 * rastreamento ponta a ponta. Se ausente, o service gera um.</p>
 */
public record IniciarOrquestracaoComando(String correlationId) {
}
