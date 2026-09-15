package com.bradesco.orch.domain.entity;

import java.time.Instant;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Registro da interação que retomou uma etapa suspensa em
 * {@link StatusEtapa#PENDENTE_DE_INTERACAO}. Persistido junto à etapa para
 * suportar a idempotência do endpoint de aprovação e sinalizar ao motor que a
 * etapa já foi retomada (não deve suspender novamente).
 *
 * <p>Registra a <b>origem</b> da interação ({@link #tipo}), o <b>momento</b> em
 * que ocorreu ({@link #aprovadoEm}) e, opcionalmente, os <b>dados de negócio</b>
 * fornecidos na retomada ({@link #dados}) — por exemplo o {@code idSimulacao}
 * escolhido, que a etapa seguinte precisa para prosseguir. Puro domínio, sem
 * framework.</p>
 */
@Getter
@Setter
public class RegistroInteracao {

    /** Origem da interação (humana ou máquina). */
    private TipoInteracao tipo;

    /** Momento em que a interação foi registrada. */
    private Instant aprovadoEm;

    /**
     * Dados de negócio fornecidos na retomada (payload livre, opcional). Mantém o
     * motor genérico: o negócio decide o que trafegar (ex.: {@code idSimulacao}).
     */
    private Map<String, Object> dados;

    public RegistroInteracao() {
    }

    public RegistroInteracao(TipoInteracao tipo, Instant aprovadoEm) {
        this.tipo = tipo;
        this.aprovadoEm = aprovadoEm;
    }

    public RegistroInteracao(TipoInteracao tipo, Instant aprovadoEm, Map<String, Object> dados) {
        this.tipo = tipo;
        this.aprovadoEm = aprovadoEm;
        this.dados = dados;
    }
}
