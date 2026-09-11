package com.bradesco.orch.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repositório Spring Data para as operações CRUD simples do documento.
 */
public interface OrquestracaoSpringDataRepository extends MongoRepository<OrquestracaoDocument, String> {
}
