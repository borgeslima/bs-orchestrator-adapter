package com.bradesco.orch.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repositório Spring Data para as operações CRUD do documento de crédito.
 */
public interface CreditoSpringDataRepository extends MongoRepository<CreditoDocument, String> {
}
