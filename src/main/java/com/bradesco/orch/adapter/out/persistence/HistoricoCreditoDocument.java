package com.bradesco.orch.adapter.out.persistence;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

/**
 * Sub-documento de uma entrada do {@code historico} do crédito.
 */
@Getter
@Setter
public class HistoricoCreditoDocument {

    private String situacao;
    private String descricao;
    private Instant dataHoraCriacao;
    private Instant dataHoraAtualizacao;

    public HistoricoCreditoDocument() {
    }

    public HistoricoCreditoDocument(String situacao, String descricao,
                                    Instant dataHoraCriacao, Instant dataHoraAtualizacao) {
        this.situacao = situacao;
        this.descricao = descricao;
        this.dataHoraCriacao = dataHoraCriacao;
        this.dataHoraAtualizacao = dataHoraAtualizacao;
    }
}
