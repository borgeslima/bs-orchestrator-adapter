package com.bradesco.orch.adapter.out.persistence;

import com.bradesco.orch.domain.entity.Credito;
import com.bradesco.orch.domain.entity.HistoricoCredito;
import com.bradesco.orch.domain.entity.SituacaoCredito;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Conversão bidirecional entre o agregado de negócio {@link Credito} e o
 * {@link CreditoDocument} de persistência. Mantém o domínio livre de anotações Mongo.
 */
@Component
public class CreditoMongoMapper {

    public CreditoDocument paraDocumento(Credito c) {
        CreditoDocument doc = new CreditoDocument();
        doc.setId(c.getId());
        doc.setHistorico(paraHistoricoDocumento(c.getHistorico()));
        return doc;
    }

    public Credito paraDominio(CreditoDocument doc) {
        return new Credito(doc.getId(), paraHistoricoDominio(doc.getHistorico()));
    }

    public HistoricoCreditoDocument paraDocumento(HistoricoCredito h) {
        return new HistoricoCreditoDocument(
                h.getSituacao() != null ? h.getSituacao().name() : null,
                h.getDescricao(),
                h.getDataHoraCriacao(),
                h.getDataHoraAtualizacao());
    }

    private List<HistoricoCreditoDocument> paraHistoricoDocumento(List<HistoricoCredito> historico) {
        List<HistoricoCreditoDocument> resultado = new ArrayList<>();
        if (historico == null) {
            return resultado;
        }
        for (HistoricoCredito h : historico) {
            resultado.add(paraDocumento(h));
        }
        return resultado;
    }

    private List<HistoricoCredito> paraHistoricoDominio(List<HistoricoCreditoDocument> historico) {
        List<HistoricoCredito> resultado = new ArrayList<>();
        if (historico == null) {
            return resultado;
        }
        for (HistoricoCreditoDocument h : historico) {
            resultado.add(new HistoricoCredito(
                    h.getSituacao() != null ? SituacaoCredito.valueOf(h.getSituacao()) : null,
                    h.getDescricao(),
                    h.getDataHoraCriacao(),
                    h.getDataHoraAtualizacao()));
        }
        return resultado;
    }
}
