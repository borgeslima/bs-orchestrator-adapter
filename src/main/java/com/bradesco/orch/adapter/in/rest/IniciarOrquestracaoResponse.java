package com.bradesco.orch.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resposta de {@code POST /cap-giro}: identificador da orquestração criada.
 */
@Schema(description = "Resposta com o identificador da orquestracao criada")
public record IniciarOrquestracaoResponse(
        @Schema(description = "Id da orquestracao criada",
                example = "665f1c2a9b4e2a0012a3b4c5")
        String orquestracaoId) {
}
