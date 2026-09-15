package com.bradesco.orch.adapter.out.persistence;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

/**
 * Sub-documento {@code interacao} com o registro da interação que retomou uma
 * etapa suspensa em {@code PENDENTE_DE_INTERACAO}. Guarda apenas a origem
 * ({@code tipo}: {@code humana}/{@code maquina}) e o momento ({@code aprovadoEm}).
 */
@Getter
@Setter
public class RegistroInteracaoDocument {

    private String tipo;
    private Instant aprovadoEm;

    public RegistroInteracaoDocument() {
    }

    public RegistroInteracaoDocument(String tipo, Instant aprovadoEm) {
        this.tipo = tipo;
        this.aprovadoEm = aprovadoEm;
    }
}
