package com.bradesco.orch.domain.entity;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

/**
 * Entrada do histórico de negócio de um {@link Credito}. Cada avanço relevante da
 * jornada (início e conclusão de etapas) adiciona uma nova entrada, mantendo o
 * histórico append-only para fins de auditoria.
 *
 * <p>Puro domínio, sem framework.</p>
 */
@Getter
@Setter
public class HistoricoCredito {

    private SituacaoCredito situacao;
    private String descricao;
    private Instant dataHoraCriacao;
    private Instant dataHoraAtualizacao;

    public HistoricoCredito() {
    }

    public HistoricoCredito(SituacaoCredito situacao, String descricao,
                            Instant dataHoraCriacao, Instant dataHoraAtualizacao) {
        this.situacao = situacao;
        this.descricao = descricao;
        this.dataHoraCriacao = dataHoraCriacao;
        this.dataHoraAtualizacao = dataHoraAtualizacao;
    }
}
