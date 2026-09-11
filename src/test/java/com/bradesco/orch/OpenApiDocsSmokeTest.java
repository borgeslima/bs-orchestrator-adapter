package com.bradesco.orch;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.bradesco.orch.adapter.in.messaging.EtapaServiceBusListener;
import com.mongodb.client.MongoClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de fumaca do documento OpenAPI: garante que {@code GET /v3/api-docs}
 * retorna 200 e descreve os endpoints do CapGiroController.
 *
 * <p>Os beans de saida que conectam a infraestrutura externa (MongoDB e Azure
 * Service Bus) sao substituidos por mocks, e o listener do Service Bus tambem e
 * mockado para nao disparar sua conexao em {@code @PostConstruct}. Assim o
 * contexto sobe sem infra externa.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocsSmokeTest {

    @Autowired
    MockMvc mockMvc;

    // Evita conexao real com MongoDB (bean @Primary criado por MongoConfig).
    @MockitoBean
    MongoClient mongoClient;

    // Evita conexao real com o Azure Service Bus.
    @MockitoBean
    ServiceBusClientBuilder serviceBusClientBuilder;

    @MockitoBean
    ServiceBusSenderClient serviceBusSenderClient;

    // Impede que o @PostConstruct do listener tente conectar ao Service Bus.
    @MockitoBean
    EtapaServiceBusListener etapaServiceBusListener;

    @Test
    void apiDocsExposeCapGiroPaths() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/cap-giro']").exists())
                .andExpect(jsonPath("$.paths['/cap-giro/{orquestracaoId}']").exists());
    }
}
