package com.bradesco.orch.adapter.out.processor;

import com.bradesco.orch.domain.port.out.EtapaProcessor;

import java.util.UUID;

/**
 * Classe base dos processors de etapa. Concentra o comportamento comum: em
 * {@link #execute(EtapaRequest)} delega ao {@link MockEtapaHttpClient} (POST no
 * endpoint mock) e em {@link #callback(EtapaResponse)} normaliza o resultado.
 *
 * <p>O único ponto de variação entre as etapas é o {@link #name()}.</p>
 */
public abstract class AbstractEtapaProcessor implements EtapaProcessor<EtapaRequest, EtapaResponse> {

    protected final MockEtapaHttpClient httpClient;

    protected AbstractEtapaProcessor(MockEtapaHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public abstract String name();

    @Override
    public EtapaResponse execute(EtapaRequest input) {
        // O motor de orquestração é genérico e não conhece o contrato do mock,
        // então o request é montado aqui (adapter). Se nenhum input for fornecido,
        // gera um id para satisfazer o contrato { "id": "<uuid>" } da API externa.
        EtapaRequest request = (input != null && input.id() != null)
                ? input
                : new EtapaRequest(UUID.randomUUID().toString());
        return httpClient.chamar(request);
    }

    @Override
    public void callback(EtapaResponse response) {
        // Resultado que o motor persiste em etapas[].callback.response.
        // Na POC não há normalização adicional.
    }
}
