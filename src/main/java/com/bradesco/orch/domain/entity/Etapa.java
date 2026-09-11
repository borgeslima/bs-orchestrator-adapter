package com.bradesco.orch.domain.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * Unidade de trabalho da orquestração, identificada por nome canônico, com estado
 * e controle de tentativas próprios. Puro domínio, sem framework.
 */
@Getter
@Setter
public class Etapa {

    private String name;
    private int order;
    private StatusEtapa status;
    private Controle controle;
    private RespostaEtapa callback;

    public Etapa() {
    }

    public Etapa(String name, int order, StatusEtapa status, Controle controle, RespostaEtapa callback) {
        this.name = name;
        this.order = order;
        this.status = status;
        this.controle = controle;
        this.callback = callback;
    }

    /** {@code true} quando a etapa está apta a ser executada ({@code PENDENTE}). */
    public boolean podeExecutar() {
        return status == StatusEtapa.PENDENTE;
    }

    /** {@code true} quando o controle de tentativas atingiu o limite configurado. */
    public boolean atingiuLimite() {
        return controle != null && controle.atingiuLimite();
    }
}
