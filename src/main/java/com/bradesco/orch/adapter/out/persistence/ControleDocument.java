package com.bradesco.orch.adapter.out.persistence;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.Setter;

/**
 * Sub-documento {@code controle} com os nomes de campo do contrato persistido.
 */
@Getter
@Setter
public class ControleDocument {

    @Field("tentativas_realizadas")
    private int tentativasRealizadas;

    @Field("limite_retentativas")
    private int limiteRetentativas;

    public ControleDocument() {
    }

    public ControleDocument(int tentativasRealizadas, int limiteRetentativas) {
        this.tentativasRealizadas = tentativasRealizadas;
        this.limiteRetentativas = limiteRetentativas;
    }
}
