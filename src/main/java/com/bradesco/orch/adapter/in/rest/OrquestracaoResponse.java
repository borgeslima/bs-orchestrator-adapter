package com.bradesco.orch.adapter.in.rest;

import com.bradesco.orch.domain.entity.Etapa;
import com.bradesco.orch.domain.entity.Orquestracao;

import java.time.Instant;
import java.util.List;

/**
 * Representação de leitura de uma orquestração e suas etapas para a API REST.
 * Não expõe a entidade de domínio diretamente.
 */
public record OrquestracaoResponse(
        String orquestracaoId,
        String name,
        int order,
        String status,
        Instant dataCriacao,
        Instant dataAtualizacao,
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
    public record EtapaResponseView(
            String name,
            int order,
            String status,
            int tentativasRealizadas,
            int limiteRetentativas,
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
