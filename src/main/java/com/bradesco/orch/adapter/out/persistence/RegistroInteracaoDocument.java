package com.bradesco.orch.adapter.out.persistence;

import java.time.Instant;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Sub-documento {@code interacao} com o registro da interação que retomou uma
 * etapa suspensa em {@code PENDENTE_DE_INTERACAO}. Guarda a origem
 * ({@code tipo}: {@code humana}/{@code maquina}), o momento ({@code aprovadoEm})
 * e, opcionalmente, os dados de negócio da retomada ({@code dados}).
 */
@Getter
@Setter
public class RegistroInteracaoDocument {

    private String tipo;
    private Instant aprovadoEm;
    private Map<String, Object> dados;

    public RegistroInteracaoDocument() {
    }

    public RegistroInteracaoDocument(String tipo, Instant aprovadoEm) {
        this.tipo = tipo;
        this.aprovadoEm = aprovadoEm;
    }

    public RegistroInteracaoDocument(String tipo, Instant aprovadoEm, Map<String, Object> dados) {
        this.tipo = tipo;
        this.aprovadoEm = aprovadoEm;
        this.dados = dados;
    }
}
