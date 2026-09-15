package com.bradesco.orch.adapter.out.processor;

import com.bradesco.orch.domain.entity.ApiStep;
import com.bradesco.orch.domain.entity.Callback;
import com.bradesco.orch.domain.port.out.FalhaDefinitivaEtapaException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/**
 * Cliente HTTP compartilhado que encapsula o {@code POST} no endpoint mock
 * (configurado em {@code orch.etapas.endpoint-base}). É o único ponto que conhece
 * o formato de request/response do mock ({@code { "id": "<uuid>" }}).
 *
 * <p>Reutiliza internamente as abstrações {@link ApiStep} + {@link Callback} para
 * modelar a mecânica de sucesso/erro da chamada. Um {@code 400 Bad Request} é
 * traduzido em {@link EtapaBadRequestException} carregando o corpo do erro, para
 * que o motor o persista no callback da etapa sem retentar.</p>
 */
@Component
public class MockEtapaHttpClient {

    private final RestClient etapasRestClient;
    private final ApiStep<EtapaRequest, EtapaResponse> apiStep;

    public MockEtapaHttpClient(RestClient etapasRestClient) {
        this.etapasRestClient = etapasRestClient;
        this.apiStep = (input, callback) -> {
            try {
                EtapaResponse resposta = etapasRestClient.post()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(input)
                        .retrieve()
                        .body(EtapaResponse.class);
                callback.onSuccess(resposta);
                return resposta;
            } catch (HttpClientErrorException.BadRequest badRequest) {
                // 400 e erro determinístico: nao retentar. Extrai o corpo do erro
                // (JSON como Map, ou texto puro) para persistir no callback da etapa.
                Object corpo = extrairCorpo(badRequest);
                FalhaDefinitivaEtapaException erro = new FalhaDefinitivaEtapaException(
                        "API externa respondeu 400 Bad Request", corpo);
                callback.onError(erro);
                throw erro;
            } catch (RuntimeException e) {
                callback.onError(e);
                throw e;
            }
        };
    }

    /**
     * Faz o POST no endpoint mock com body {@code { id }} e mapeia a resposta
     * {@code { id }} para {@link EtapaResponse}.
     */
    public EtapaResponse chamar(EtapaRequest request) {
        return apiStep.execute(request, new Callback<>() {
            @Override
            public void onSuccess(EtapaResponse response) {
                // no-op: mecanica de sucesso da chamada de API
            }

            @Override
            public void onError(Exception error) {
                // no-op: erro propagado pela excecao
            }
        });
    }

    /**
     * Extrai o corpo do erro 400. Tenta desserializar como JSON (mapeado para
     * {@code Map}); em último caso, devolve o texto bruto da resposta.
     */
    private Object extrairCorpo(HttpClientErrorException badRequest) {
        try {
            Object json = badRequest.getResponseBodyAs(java.util.Map.class);
            if (json != null) {
                return json;
            }
        } catch (RuntimeException ignorado) {
            // corpo nao e JSON; cai para o texto bruto
        }
        String texto = badRequest.getResponseBodyAsString();
        return (texto != null && !texto.isBlank()) ? texto : "400 Bad Request";
    }
}
