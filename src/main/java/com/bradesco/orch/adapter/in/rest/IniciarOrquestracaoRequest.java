package com.bradesco.orch.adapter.in.rest;

import com.bradesco.orch.domain.port.in.IniciarOrquestracaoComando;

/**
 * Corpo da requisição {@code POST /cap-giro}. Na POC o {@code correlationId} é
 * opcional (o service gera um se ausente).
 */
public record IniciarOrquestracaoRequest(String correlationId) {

    public IniciarOrquestracaoComando toComando() {
        return new IniciarOrquestracaoComando(correlationId);
    }
}
