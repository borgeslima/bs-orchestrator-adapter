package com.bradesco.orch.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Configuração explícita do MongoDB. Cria o {@link MongoClient} diretamente a
 * partir da {@code spring.data.mongodb.uri}, evitando que outra autoconfiguração
 * (ex.: Spring Cloud Azure) monte um client apontando para {@code localhost:27017}.
 *
 * <p>A URI é externalizada (Atlas apenas como default de dev) — sem credenciais
 * hardcoded no código.</p>
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {

    @Bean
    @Primary
    public MongoClient mongoClient(@Value("${spring.data.mongodb.uri}") String uri) {
        ConnectionString connectionString = new ConnectionString(uri);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        return MongoClients.create(settings);
    }
}
