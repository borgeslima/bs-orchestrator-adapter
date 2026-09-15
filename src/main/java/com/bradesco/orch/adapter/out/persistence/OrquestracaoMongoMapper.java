package com.bradesco.orch.adapter.out.persistence;

import com.bradesco.orch.domain.entity.Controle;
import com.bradesco.orch.domain.entity.Etapa;
import com.bradesco.orch.domain.entity.Orquestracao;
import com.bradesco.orch.domain.entity.RegistroInteracao;
import com.bradesco.orch.domain.entity.RespostaEtapa;
import com.bradesco.orch.domain.entity.StatusEtapa;
import com.bradesco.orch.domain.entity.StatusOrquestracao;
import com.bradesco.orch.domain.entity.TipoInteracao;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Conversão bidirecional entre o agregado de domínio {@link Orquestracao} e o
 * {@link OrquestracaoDocument} de persistência. Mantém o domínio livre de
 * anotações Mongo.
 */
@Component
public class OrquestracaoMongoMapper {

    public OrquestracaoDocument paraDocumento(Orquestracao o) {
        OrquestracaoDocument doc = new OrquestracaoDocument();
        doc.setId(o.getId());
        doc.setName(o.getName());
        doc.setOrder(o.getOrder());
        doc.setStatus(o.getStatus() != null ? o.getStatus().name() : null);
        doc.setDataCriacao(o.getDataCriacao());
        doc.setDataAtualizacao(o.getDataAtualizacao());
        doc.setVersion(o.getVersion());
        doc.setEtapas(paraEtapasDocumento(o.getEtapas()));
        return doc;
    }

    public Orquestracao paraDominio(OrquestracaoDocument doc) {
        return new Orquestracao(
                doc.getId(),
                doc.getName(),
                doc.getOrder(),
                doc.getStatus() != null ? StatusOrquestracao.valueOf(doc.getStatus()) : null,
                doc.getDataCriacao(),
                doc.getDataAtualizacao(),
                doc.getVersion(),
                paraEtapasDominio(doc.getEtapas())
        );
    }

    private List<EtapaDocument> paraEtapasDocumento(List<Etapa> etapas) {
        if (etapas == null) {
            return new ArrayList<>();
        }
        List<EtapaDocument> resultado = new ArrayList<>(etapas.size());
        for (Etapa e : etapas) {
            EtapaDocument ed = new EtapaDocument();
            ed.setName(e.getName());
            ed.setOrder(e.getOrder());
            ed.setStatus(e.getStatus() != null ? e.getStatus().name() : null);
            if (e.getControle() != null) {
                ed.setControle(new ControleDocument(
                        e.getControle().getTentativasRealizadas(),
                        e.getControle().getLimiteRetentativas()));
            }
            Object response = (e.getCallback() != null) ? e.getCallback().getResponse() : null;
            ed.setCallback(new RespostaEtapaDocument(response));
            if (e.getInteracao() != null) {
                RegistroInteracao ri = e.getInteracao();
                String tipo = ri.getTipo() != null ? ri.getTipo().name().toLowerCase() : null;
                ed.setInteracao(new RegistroInteracaoDocument(tipo, ri.getAprovadoEm()));
            }
            resultado.add(ed);
        }
        return resultado;
    }

    private List<Etapa> paraEtapasDominio(List<EtapaDocument> etapas) {
        List<Etapa> resultado = new ArrayList<>();
        if (etapas == null) {
            return resultado;
        }
        for (EtapaDocument ed : etapas) {
            Controle controle = (ed.getControle() != null)
                    ? new Controle(ed.getControle().getTentativasRealizadas(), ed.getControle().getLimiteRetentativas())
                    : new Controle();
            Object response = (ed.getCallback() != null) ? ed.getCallback().getResponse() : null;
            RegistroInteracao interacao = null;
            if (ed.getInteracao() != null) {
                RegistroInteracaoDocument rid = ed.getInteracao();
                TipoInteracao tipo = rid.getTipo() != null
                        ? TipoInteracao.valueOf(rid.getTipo().toUpperCase())
                        : null;
                interacao = new RegistroInteracao(tipo, rid.getAprovadoEm());
            }
            resultado.add(new Etapa(
                    ed.getName(),
                    ed.getOrder(),
                    ed.getStatus() != null ? StatusEtapa.valueOf(ed.getStatus()) : null,
                    controle,
                    new RespostaEtapa(response),
                    interacao
            ));
        }
        return resultado;
    }
}
