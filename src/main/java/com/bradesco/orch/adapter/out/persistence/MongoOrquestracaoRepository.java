package com.bradesco.orch.adapter.out.persistence;

import com.bradesco.orch.domain.entity.Orquestracao;
import com.bradesco.orch.domain.entity.StatusEtapa;
import com.bradesco.orch.domain.port.out.OrquestracaoRepository;
import com.mongodb.client.result.UpdateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Implementação da porta {@link OrquestracaoRepository} sobre MongoDB.
 *
 * <p>Usa {@link MongoTemplate} para a transição atômica condicional
 * {@code PENDENTE -> EM_EXECUCAO} e o Spring Data para o CRUD. Conflitos de
 * optimistic locking são traduzidos em duplicidade sem sobrescrever o estado.</p>
 */
@Repository
public class MongoOrquestracaoRepository implements OrquestracaoRepository {

    private static final Logger log = LoggerFactory.getLogger(MongoOrquestracaoRepository.class);

    private final OrquestracaoSpringDataRepository springDataRepository;
    private final MongoTemplate mongoTemplate;
    private final OrquestracaoMongoMapper mapper;

    public MongoOrquestracaoRepository(OrquestracaoSpringDataRepository springDataRepository,
                                       MongoTemplate mongoTemplate,
                                       OrquestracaoMongoMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mongoTemplate = mongoTemplate;
        this.mapper = mapper;
    }

    @Override
    public Orquestracao salvar(Orquestracao orquestracao) {
        OrquestracaoDocument salvo = springDataRepository.save(mapper.paraDocumento(orquestracao));
        return mapper.paraDominio(salvo);
    }

    @Override
    public Optional<Orquestracao> buscarPorId(String id) {
        return springDataRepository.findById(id).map(mapper::paraDominio);
    }

    @Override
    public boolean transicionarEtapaParaEmExecucao(String orquestracaoId, String etapa) {
        Query q = new Query(Criteria.where("_id").is(orquestracaoId)
                .and("etapas").elemMatch(Criteria.where("name").is(etapa)
                        .and("status").is(StatusEtapa.PENDENTE.name())));
        Update u = new Update()
                .set("etapas.$.status", StatusEtapa.EM_EXECUCAO.name())
                .inc("etapas.$.controle.tentativas_realizadas", 1)
                .inc("version", 1)
                .currentDate("dataAtualizacao");

        UpdateResult res = mongoTemplate.updateFirst(q, u, OrquestracaoDocument.class);
        boolean venceu = res.getModifiedCount() == 1L;
        if (!venceu) {
            log.debug("Transicao atomica nao aplicada (etapa {} ja fora de PENDENTE)", etapa);
        }
        return venceu;
    }

    @Override
    public void atualizar(Orquestracao orquestracao) {
        try {
            springDataRepository.save(mapper.paraDocumento(orquestracao));
        } catch (OptimisticLockingFailureException conflito) {
            // Conflito de concorrencia: outra replica ja atualizou o documento.
            // Nao sobrescreve o estado; trata como duplicidade silenciosa.
            log.info("Conflito de optimistic locking ao atualizar orquestracao {} -> duplicidade",
                    orquestracao.getId());
        }
    }
}
