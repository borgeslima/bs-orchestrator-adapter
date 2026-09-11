package com.bradesco.orch.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resposta de {@code POST /cap-giro/{orquestracaoId}/etapas/{etapa}/aprovar}.
 */
@Schema(description = "Resultado da aprovacao de uma etapa suspensa por interacao humana")
public record AprovarEtapaResponse(
        @Schema(description = "Id da orquestracao")
        String orquestracaoId,
        @Schema(description = "Nome canonico da etapa aprovada", example = "baas:etapa:cap-giro:oferta")
        String etapa,
        @Schema(description = "Resultado da aprovacao", example = "APROVADA")
        String resultado) {
}
