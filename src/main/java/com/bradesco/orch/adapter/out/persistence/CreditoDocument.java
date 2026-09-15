package com.bradesco.orch.adapter.out.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Documento MongoDB da coleção {@code creditos} (domínio de negócio). O domínio
 * não conhece esta classe.
 */
@Getter
@Setter
@Document("creditos")
public class CreditoDocument {

    @Id
    private String id;
    private List<HistoricoCreditoDocument> historico;

    public CreditoDocument() {
    }
}
