package com.bradesco.orch.adapter.out.persistence;

import lombok.Getter;
import lombok.Setter;

/**
 * Sub-documento de uma etapa dentro do documento de orquestração.
 */
@Getter
@Setter
public class EtapaDocument {

    private String name;
    private int order;
    private String status;
    private ControleDocument controle;
    private RespostaEtapaDocument callback;

    public EtapaDocument() {
    }
}
