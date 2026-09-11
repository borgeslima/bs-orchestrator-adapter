package com.bradesco.orch.adapter.in.rest;

import com.bradesco.orch.domain.entity.Etapa;
import com.bradesco.orch.domain.entity.Orquestracao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Representação de leitura de uma orquestração e suas etapas para a API REST.
 * Não expõe a entidade de domínio diretamente.
 */
@Schema(description = "Estado de uma orquestracao de cap-giro e suas etapas")
public record OrquestracaoResponse(
        @Schema(description = "Id da orquestracao", example = "665f1c2a9b4e2a0012a3b4c5")
        String orquestracaoId,
        @Schema(description = "Nome da orquestracao", example = "cap-giro")
        String name,
        @Schema(description = "Ordem/sequencia da orquestracao", example = "0")
        int order,
        @Schema(description = "Status atual da orquestracao", example = "EM_ANDAMENTO")
        String status,
        @Schema(description = "Data de criacao da orquestracao")
        Instant dataCriacao,
        @Schema(description = "Data da ultima atualizacao da orquestracao")
        Instant dataAtualizacao,
        @Schema(description = "Etapas da orquestracao")
        List<EtapaResponseView> etapas
) {

    public static OrquestracaoResponse de(Orquestracao o) {
        List<EtapaResponseView> etapas = o.getEtapas() == null ? List.of()
                : o.getEtapas().stream().map(EtapaResponseView::de).toList();
        return new OrquestracaoResponse(
                o.getId(),
                o.getName(),
                o.getOrder(),
                o.getStatus() != null ? o.getStatus().name() : null,
                o.getDataCriacao(),
                o.getDataAtualizacao(),
                etapas
        );
    }

    /** View de uma etapa, incluindo controle de tentativas e o resultado do callback. */
    @Schema(description = "Etapa de uma orquestracao, incluindo controle de tentativas e o callback")
    public record EtapaResponseView(
            @Schema(description = "Nome da etapa", example = "oferta")
            String name,
            @Schema(description = "Ordem/sequencia da etapa", example = "1")
            int order,
            @Schema(description = "Status atual da etapa", example = "CONCLUIDA")
            String status,
            @Schema(description = "Quantidade de tentativas ja realizadas", example = "1")
            int tentativasRealizadas,
            @Schema(description = "Limite de retentativas configurado", example = "3")
            int limiteRetentativas,
            @Schema(description = "Resposta retornada pelo callback da etapa (payload livre)")
            Object callbackResponse
    ) {
        public static EtapaResponseView de(Etapa e) {
            int tentativas = e.getControle() != null ? e.getControle().getTentativasRealizadas() : 0;
            int limite = e.getControle() != null ? e.getControle().getLimiteRetentativas() : 0;
            Object response = e.getCallback() != null ? e.getCallback().getResponse() : null;
            return new EtapaResponseView(
                    e.getName(),
                    e.getOrder(),
                    e.getStatus() != null ? e.getStatus().name() : null,
                    tentativas,
                    limite,
                    response
            );
        }
    }
}
