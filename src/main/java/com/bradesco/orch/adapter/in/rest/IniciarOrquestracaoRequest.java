package com.bradesco.orch.adapter.in.rest;

import com.bradesco.orch.domain.port.in.IniciarOrquestracaoComando;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Corpo da requisição {@code POST /cap-giro}. Na POC o {@code correlationId} é
 * opcional (o service gera um se ausente).
 */
@Schema(description = "Corpo (opcional) para iniciar uma orquestracao de cap-giro")
public record IniciarOrquestracaoRequest(
        @Schema(description = "Identificador de correlacao (opcional; gerado se ausente)",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        String correlationId) {

    public IniciarOrquestracaoComando toComando() {
        return new IniciarOrquestracaoComando(correlationId);
    }
}
