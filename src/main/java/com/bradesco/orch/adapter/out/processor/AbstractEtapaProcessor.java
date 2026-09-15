package com.bradesco.orch.adapter.out.processor;

import com.bradesco.orch.domain.port.out.EtapaProcessor;

import java.util.Map;
import java.util.UUID;

/**
 * Classe base dos processors de etapa. Concentra o comportamento comum: em
 * {@link #execute(Object)} monta o {@link EtapaRequest} a partir do input
 * recebido e delega ao {@link MockEtapaHttpClient} (POST no endpoint mock); em
 * {@link #callback(EtapaResponse)} normaliza o resultado.
 *
 * <p>O input é {@code Object} porque o motor de orquestração é genérico: pode
 * chegar {@code null} (execução normal), um {@link EtapaRequest} ou um
 * {@link Map} com dados de negócio de uma retomada por interação (ex.: contendo
 * {@code idSimulacao}). O único ponto de variação entre as etapas é o
 * {@link #name()}.</p>
 */
public abstract class AbstractEtapaProcessor implements EtapaProcessor<Object, EtapaResponse> {

    protected final MockEtapaHttpClient httpClient;

    protected AbstractEtapaProcessor(MockEtapaHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public abstract String name();

    @Override
    public EtapaResponse execute(Object input) {
        return httpClient.chamar(montarRequest(input));
    }

    /**
     * Monta o {@link EtapaRequest} do POST a partir do input genérico. Se nenhum
     * id utilizável for encontrado, gera um UUID para satisfazer o contrato
     * {@code { "id": "<uuid>" }} da API externa.
     */
    private EtapaRequest montarRequest(Object input) {
        if (input instanceof EtapaRequest req && req.id() != null) {
            return req;
        }
        if (input instanceof Map<?, ?> dados) {
            Object id = dados.containsKey("idSimulacao") ? dados.get("idSimulacao") : dados.get("id");
            if (id != null) {
                return new EtapaRequest(id.toString());
            }
        }
        return new EtapaRequest(UUID.randomUUID().toString());
    }

    @Override
    public void callback(EtapaResponse response) {
        // Resultado que o motor persiste em etapas[].callback.response.
        // Na POC não há normalização adicional.
    }
}
