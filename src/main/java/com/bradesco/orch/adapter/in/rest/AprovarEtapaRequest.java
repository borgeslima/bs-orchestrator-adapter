package com.bradesco.orch.adapter.in.rest;

import com.bradesco.orch.domain.entity.TipoInteracao;
import com.bradesco.orch.domain.port.in.AprovarEtapaComando;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Corpo (opcional) de {@code POST /cap-giro/{orquestracaoId}/etapas/{etapa}/aprovar}.
 */
@Schema(description = "Corpo opcional para aprovar (retomar) uma etapa PENDENTE_DE_INTERACAO")
public record AprovarEtapaRequest(
        @Schema(description = "Origem da interacao: HUMANA ou MAQUINA (opcional; assume HUMANA se ausente)",
                example = "HUMANA")
        TipoInteracao tipo,
        @Schema(description = "Identificador de correlacao (opcional; gerado se ausente)",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        String correlationId) {

    public AprovarEtapaComando toComando(String orquestracaoId, String etapa) {
        return new AprovarEtapaComando(orquestracaoId, etapa, tipo, correlationId);
    }
}
