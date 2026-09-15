package com.bradesco.orch.domain.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Agregado de negócio do capital de giro. Representa o crédito propriamente dito
 * — separado do motor de orquestração ({@link Orquestracao}) — e mantém o
 * {@code historico} da jornada. Compartilha o {@code id} com a orquestração que o
 * conduz (relação 1:1 na POC).
 *
 * <p>Puro domínio, sem framework. Concentra a regra de <b>append</b> de novas
 * entradas no histórico.</p>
 */
@Getter
@Setter
public class Credito {

    private String id;
    private List<HistoricoCredito> historico;

    public Credito() {
    }

    public Credito(String id, List<HistoricoCredito> historico) {
        this.id = id;
        this.historico = historico;
    }

    /**
     * Cria um crédito recém-iniciado com a primeira entrada de histórico
     * ({@code PENDENTE}), usando o mesmo momento para criação e atualização.
     */
    public static Credito iniciar(String id, String descricao, Instant momento) {
        List<HistoricoCredito> historico = new ArrayList<>();
        historico.add(new HistoricoCredito(SituacaoCredito.PENDENTE, descricao, momento, momento));
        return new Credito(id, historico);
    }

    /**
     * Adiciona uma nova entrada ao histórico (append-only). {@code dataHoraCriacao}
     * e {@code dataHoraAtualizacao} recebem o mesmo momento no instante do registro.
     */
    public void registrar(SituacaoCredito situacao, String descricao, Instant momento) {
        if (historico == null) {
            historico = new ArrayList<>();
        }
        historico.add(new HistoricoCredito(situacao, descricao, momento, momento));
    }
}
