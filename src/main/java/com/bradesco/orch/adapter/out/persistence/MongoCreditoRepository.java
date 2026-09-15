package com.bradesco.orch.adapter.out.persistence;

import com.bradesco.orch.domain.entity.Credito;
import com.bradesco.orch.domain.entity.HistoricoCredito;
import com.bradesco.orch.domain.port.out.CreditoRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Implementação da porta {@link CreditoRepository} sobre MongoDB (coleção
 * {@code creditos}). O CRUD usa Spring Data; o registro de histórico usa
 * {@link MongoTemplate} com {@code $push} atômico, preservando as entradas
 * anteriores sem reescrever o documento inteiro.
 */
@Repository
public class MongoCreditoRepository implements CreditoRepository {

    private final CreditoSpringDataRepository springDataRepository;
    private final MongoTemplate mongoTemplate;
    private final CreditoMongoMapper mapper;

    public MongoCreditoRepository(CreditoSpringDataRepository springDataRepository,
                                  MongoTemplate mongoTemplate,
                                  CreditoMongoMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mongoTemplate = mongoTemplate;
        this.mapper = mapper;
    }

    @Override
    public Credito salvar(Credito credito) {
        CreditoDocument salvo = springDataRepository.save(mapper.paraDocumento(credito));
        return mapper.paraDominio(salvo);
    }

    @Override
    public Optional<Credito> buscarPorId(String id) {
        return springDataRepository.findById(id).map(mapper::paraDominio);
    }

    @Override
    public void registrarHistorico(String creditoId, HistoricoCredito entrada) {
        Query q = new Query(Criteria.where("_id").is(creditoId));
        Update u = new Update().push("historico", mapper.paraDocumento(entrada));
        mongoTemplate.updateFirst(q, u, CreditoDocument.class);
    }
}
