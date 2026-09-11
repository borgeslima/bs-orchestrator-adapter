package com.bradesco.orch.domain.entity;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import lombok.Getter;
import lombok.Setter;

/**
 * Agregado raiz da orquestração. Concentra o <b>motor de transição</b> de estados
 * (qual é a próxima etapa, se é a última, avançar/concluir, marcar erro), mantendo
 * a regra de negócio testável sem infraestrutura. Puro domínio, sem framework.
 */
@Getter
@Setter
public class Orquestracao {

    private String id;
    private String name;
    private int order;
    private StatusOrquestracao status;
    private Instant dataCriacao;
    private Instant dataAtualizacao;
    private Long version;
    private List<Etapa> etapas;

    public Orquestracao() {
    }

    public Orquestracao(String id, String name, int order, StatusOrquestracao status,
                        Instant dataCriacao, Instant dataAtualizacao, Long version, List<Etapa> etapas) {
        this.id = id;
        this.name = name;
        this.order = order;
        this.status = status;
        this.dataCriacao = dataCriacao;
        this.dataAtualizacao = dataAtualizacao;
        this.version = version;
        this.etapas = etapas;
    }

    // ---------------------------------------------------------------------
    // Motor de transição (regras de negócio)
    // ---------------------------------------------------------------------

    /** Localiza a etapa pelo nome canônico. */
    public Etapa etapaAtual(String nome) {
        if (etapas == null) {
            return null;
        }
        return etapas.stream()
                .filter(e -> e.getName().equals(nome))
                .findFirst()
                .orElse(null);
    }

    /**
     * Retorna a próxima etapa (por {@code order}) em relação à etapa informada,
     * ou vazio se a informada for a última ou inexistente.
     */
    public Optional<Etapa> proximaEtapa(String nomeAtual) {
        Etapa atual = etapaAtual(nomeAtual);
        if (atual == null || etapas == null) {
            return Optional.empty();
        }
        return etapas.stream()
                .filter(e -> e.getOrder() > atual.getOrder())
                .min(Comparator.comparingInt(Etapa::getOrder));
    }

    /** {@code true} quando não existe etapa posterior à informada. */
    public boolean ehUltima(String nome) {
        return proximaEtapa(nome).isEmpty();
    }

    /**
     * Conclui a etapa informada (grava {@code callback.response} e marca
     * {@code CONCLUIDA}) e avança o fluxo: se houver próxima etapa
     * {@code AGUARDANDO}, transiciona-a para {@code PENDENTE} e mantém a
     * orquestração em {@code EM_EXECUCAO}; se for a última, marca a orquestração
     * como {@code CONCLUIDA}.
     *
     * @return a próxima etapa que passou a {@code PENDENTE}, ou vazio se era a última.
     */
    public Optional<Etapa> concluirEtapaEAvancar(String nome, Object response) {
        Etapa atual = exigirEtapa(nome);
        atual.setStatus(StatusEtapa.CONCLUIDA);
        if (atual.getCallback() == null) {
            atual.setCallback(new RespostaEtapa(response));
        } else {
            atual.getCallback().setResponse(response);
        }

        Optional<Etapa> proxima = proximaEtapa(nome);
        if (proxima.isPresent()) {
            Etapa prox = proxima.get();
            if (prox.getStatus() == StatusEtapa.AGUARDANDO) {
                prox.setStatus(StatusEtapa.PENDENTE);
            }
            this.status = StatusOrquestracao.EM_EXECUCAO;
        } else {
            this.status = StatusOrquestracao.CONCLUIDA;
        }
        return proxima;
    }

    /** Marca a etapa informada e a orquestração como {@code ERRO}. */
    public void marcarErro(String nome) {
        Etapa atual = exigirEtapa(nome);
        atual.setStatus(StatusEtapa.ERRO);
        this.status = StatusOrquestracao.ERRO;
    }

    private Etapa exigirEtapa(String nome) {
        Etapa atual = etapaAtual(nome);
        if (atual == null) {
            throw new IllegalArgumentException("Etapa não encontrada na orquestração: " + nome);
        }
        return atual;
    }
}
