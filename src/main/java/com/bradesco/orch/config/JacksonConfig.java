package com.bradesco.orch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do {@link ObjectMapper} usado na (de)serialização das mensagens do
 * Service Bus e do {@code callback.response}, com suporte a tipos de data/hora
 * ({@code Instant}).
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }
}
