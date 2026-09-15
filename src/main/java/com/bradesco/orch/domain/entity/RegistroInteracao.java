package com.bradesco.orch.domain.entity;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

/**
 * Registro da interação que retomou uma etapa suspensa em
 * {@link StatusEtapa#PENDENTE_DE_INTERACAO}. Persistido junto à etapa para
 * suportar a idempotência do endpoint de aprovação e sinalizar ao motor que a
 * etapa já foi retomada (não deve suspender novamente).
 *
 * <p>Registra apenas a <b>origem</b> da interação ({@link #tipo}) e o
 * <b>momento</b> em que ocorreu ({@link #aprovadoEm}). Puro domínio, sem framework.</p>
 */
@Getter
@Setter
public class RegistroInteracao {

    /** Origem da interação (humana ou máquina). */
    private TipoInteracao tipo;

    /** Momento em que a interação foi registrada. */
    private Instant aprovadoEm;

    public RegistroInteracao() {
    }

    public RegistroInteracao(TipoInteracao tipo, Instant aprovadoEm) {
        this.tipo = tipo;
        this.aprovadoEm = aprovadoEm;
    }
}
