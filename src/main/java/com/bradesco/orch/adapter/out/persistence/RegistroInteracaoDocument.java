package com.bradesco.orch.adapter.out.persistence;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

/**
 * Sub-documento {@code interacao} com o registro da aprovação humana que
 * retomou uma etapa suspensa em {@code PENDENTE_DE_INTERACAO}.
 */
@Getter
@Setter
public class RegistroInteracaoDocument {

    private String aprovadoPor;
    private Instant aprovadoEm;
    private String observacao;

    public RegistroInteracaoDocument() {
    }

    public RegistroInteracaoDocument(String aprovadoPor, Instant aprovadoEm, String observacao) {
        this.aprovadoPor = aprovadoPor;
        this.aprovadoEm = aprovadoEm;
        this.observacao = observacao;
    }
}
