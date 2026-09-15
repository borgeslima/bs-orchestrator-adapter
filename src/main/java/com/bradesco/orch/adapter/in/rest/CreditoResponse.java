package com.bradesco.orch.adapter.in.rest;

import com.bradesco.orch.domain.entity.Credito;
import com.bradesco.orch.domain.entity.HistoricoCredito;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Representação de leitura de um crédito de negócio (cap-giro) e seu histórico
 * para a API REST. Não expõe a entidade de domínio diretamente.
 */
@Schema(description = "Credito de negocio (cap-giro) e seu historico")
public record CreditoResponse(
        @Schema(description = "Id do credito (mesmo id da orquestracao)",
                example = "665f1c2a9b4e2a0012a3b4c5")
        String id,
        @Schema(description = "Historico de situacoes do credito")
        List<HistoricoView> historico
) {

    public static CreditoResponse de(Credito c) {
        List<HistoricoView> historico = c.getHistorico() == null ? List.of()
                : c.getHistorico().stream().map(HistoricoView::de).toList();
        return new CreditoResponse(c.getId(), historico);
    }

    /** View de uma entrada do histórico do crédito. */
    @Schema(description = "Entrada do historico do credito")
    public record HistoricoView(
            @Schema(description = "Situacao de negocio", example = "EM_ANDAMENTO")
            String situacao,
            @Schema(description = "Descricao da situacao", example = "Oferta processada")
            String descricao,
            @Schema(description = "Momento de criacao da entrada")
            Instant dataHoraCriacao,
            @Schema(description = "Momento da ultima atualizacao da entrada")
            Instant dataHoraAtualizacao
    ) {
        public static HistoricoView de(HistoricoCredito h) {
            return new HistoricoView(
                    h.getSituacao() != null ? h.getSituacao().name() : null,
                    h.getDescricao(),
                    h.getDataHoraCriacao(),
                    h.getDataHoraAtualizacao());
        }
    }
}
