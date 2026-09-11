package com.bradesco.orch.adapter.out.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Documento MongoDB da coleção {@code orquestracoes}. Optimistic locking via
 * {@link Version}. O domínio não conhece esta classe.
 */
@Getter
@Setter
@Document("orquestracoes")
public class OrquestracaoDocument {

    @Id
    private String id;
    private String name;
    private int order;
    private String status;
    private Instant dataCriacao;
    private Instant dataAtualizacao;

    @Version
    private Long version;

    private List<EtapaDocument> etapas;

    public OrquestracaoDocument() {
    }
}
