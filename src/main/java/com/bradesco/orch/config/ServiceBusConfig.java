package com.bradesco.orch.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do Azure Service Bus. Cria o {@link ServiceBusSenderClient} para a
 * fila de orquestração usado pelo publisher. A connection string é externalizada
 * (emulador por padrão) e nunca fica hardcoded no código.
 */
@Configuration
public class ServiceBusConfig {

    @Bean
    public ServiceBusClientBuilder serviceBusClientBuilder(
            @Value("${spring.cloud.azure.servicebus.connection-string}") String connectionString) {
        return new ServiceBusClientBuilder().connectionString(connectionString);
    }

    @Bean
    public ServiceBusSenderClient serviceBusSenderClient(
            ServiceBusClientBuilder builder,
            @Value("${orch.servicebus.fila}") String fila) {
        return builder.sender().queueName(fila).buildClient();
    }
}
