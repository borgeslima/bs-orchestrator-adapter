package com.bradesco.orch.domain.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * Controle de tentativas de uma etapa (sub-documento {@code controle}).
 *
 * <p>Puro domínio, sem framework. Reflete os campos persistidos
 * {@code controle.tentativas_realizadas} e {@code controle.limite_retentativas}.</p>
 */
@Getter
@Setter
public class Controle {

    private int tentativasRealizadas;
    private int limiteRetentativas;

    public Controle() {
    }

    public Controle(int tentativasRealizadas, int limiteRetentativas) {
        this.tentativasRealizadas = tentativasRealizadas;
        this.limiteRetentativas = limiteRetentativas;
    }

    /** Incrementa o contador de tentativas realizadas. */
    public void incrementar() {
        this.tentativasRealizadas++;
    }

    /** {@code true} quando o número de tentativas atinge (ou ultrapassa) o limite. */
    public boolean atingiuLimite() {
        return tentativasRealizadas >= limiteRetentativas;
    }
}
