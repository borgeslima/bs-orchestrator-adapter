package com.bradesco.orch.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração dos metadados institucionais da API (seção {@code info} do
 * documento OpenAPI). Reside no pacote de configuração/infra, sem qualquer
 * referência à camada de domínio, preservando a Arquitetura Hexagonal.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orchOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("orch-poc API")
                        .description("API de orquestracao de capital de giro (cap-giro). "
                                + "Inicia orquestracoes assincronas e consulta o estado/etapas.")
                        .version("v1")
                        .contact(new Contact()
                                .name("Time Orquestracao - Bradesco")
                                .email("time-orquestracao@bradesco.com.br")));
    }
}
