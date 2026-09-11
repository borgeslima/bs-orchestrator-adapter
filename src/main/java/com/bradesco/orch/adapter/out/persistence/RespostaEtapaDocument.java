package com.bradesco.orch.adapter.out.persistence;

import lombok.Getter;
import lombok.Setter;

/**
 * Sub-documento {@code callback} com o campo {@code response}.
 */
@Getter
@Setter
public class RespostaEtapaDocument {

    private Object response;

    public RespostaEtapaDocument() {
    }

    public RespostaEtapaDocument(Object response) {
        this.response = response;
    }
}
