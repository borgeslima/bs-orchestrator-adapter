package com.bradesco.orch.domain.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * Sub-documento {@code callback.response}: encapsula o resultado retornado pela
 * execução da etapa (o {@code EtapaResponse} da API externa) que é persistido em
 * {@code etapas[].callback.response}.
 *
 * <p>Modelado com nome distinto de {@link Callback} (interface de I/O) para evitar
 * colisão de conceitos, mantendo o contrato persistido {@code callback} intacto.</p>
 */
@Getter
@Setter
public class RespostaEtapa {

    private Object response;

    public RespostaEtapa() {
    }

    public RespostaEtapa(Object response) {
        this.response = response;
    }
}
