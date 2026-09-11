package com.bradesco.orch.domain.entity;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

/**
 * Registro da interação/aprovação humana que retomou uma etapa suspensa em
 * {@link StatusEtapa#PENDENTE_DE_INTERACAO}. Persistido junto à etapa para
 * auditoria e para suportar a idempotência do endpoint de aprovação.
 *
 * <p>Puro domínio, sem framework.</p>
 */
@Getter
@Setter
public class RegistroInteracao {

    /** Identificador de quem aprovou (usuário/sistema), quando informado. */
    private String aprovadoPor;

    /** Momento em que a aprovação foi registrada. */
    private Instant aprovadoEm;

    /** Observação livre registrada junto à aprovação (opcional). */
    private String observacao;

    public RegistroInteracao() {
    }

    public RegistroInteracao(String aprovadoPor, Instant aprovadoEm, String observacao) {
        this.aprovadoPor = aprovadoPor;
        this.aprovadoEm = aprovadoEm;
        this.observacao = observacao;
    }
}
