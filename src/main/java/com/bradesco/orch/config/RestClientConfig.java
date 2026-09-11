package com.bradesco.orch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Expõe o {@link RestClient} usado pelos processors (via {@code MockEtapaHttpClient})
 * para chamar a API externa das etapas. A URL base é externalizada em
 * {@code orch.etapas.endpoint-base}, mantendo o endpoint fora do código.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient etapasRestClient(@Value("${orch.etapas.endpoint-base}") String endpointBase) {
        return RestClient.builder().baseUrl(endpointBase).build();
    }
}
