package com.bradesco.orch.adapter.in.messaging;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.bradesco.orch.domain.port.in.ProcessarEtapaComando;
import com.bradesco.orch.domain.port.in.ProcessarEtapaUseCase;
import com.bradesco.orch.domain.port.in.ResultadoProcessamento;
import com.bradesco.orch.domain.port.out.MensagemEtapa;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Adapter de entrada de mensageria. Consome a fila do Service Bus, delega ao
 * {@link ProcessarEtapaUseCase} e traduz o {@link ResultadoProcessamento} de
 * domínio em complete/abandon/dead-letter, mantendo o domínio livre de tipos Azure.
 */
@Component
public class EtapaServiceBusListener {

    private static final Logger log = LoggerFactory.getLogger(EtapaServiceBusListener.class);

    private final ServiceBusClientBuilder clientBuilder;
    private final ProcessarEtapaUseCase processarEtapa;
    private final ObjectMapper objectMapper;
    private final String fila;

    private ServiceBusProcessorClient processorClient;

    public EtapaServiceBusListener(ServiceBusClientBuilder clientBuilder,
                                   ProcessarEtapaUseCase processarEtapa,
                                   ObjectMapper objectMapper,
                                   @Value("${orch.servicebus.fila}") String fila) {
        this.clientBuilder = clientBuilder;
        this.processarEtapa = processarEtapa;
        this.objectMapper = objectMapper;
        this.fila = fila;
    }

    @PostConstruct
    public void iniciar() {
        this.processorClient = clientBuilder
                .processor()
                .queueName(fila)
                .disableAutoComplete()
                .processMessage(this::onMessage)
                .processError(ctx -> log.error("Erro no Service Bus: {}", ctx.getException().getMessage()))
                .buildProcessorClient();
        this.processorClient.start();
        log.info("Listener do Service Bus iniciado na fila '{}'", fila);
    }

    @PreDestroy
    public void parar() {
        if (processorClient != null) {
            processorClient.close();
        }
    }

    void onMessage(com.azure.messaging.servicebus.ServiceBusReceivedMessageContext context) {
        ServiceBusReceivedMessage msg = context.getMessage();
        try {
            MensagemEtapa mensagem = objectMapper.readValue(msg.getBody().toString(), MensagemEtapa.class);
            ProcessarEtapaComando comando = new ProcessarEtapaComando(
                    mensagem.orquestracaoId(), mensagem.etapa(), mensagem.correlationId());

            ResultadoProcessamento resultado = processarEtapa.processar(comando);

            switch (resultado) {
                case SUCESSO, DUPLICIDADE, PROCESSOR_NAO_ENCONTRADO -> context.complete();
                case ERRO_FINAL -> context.deadLetter();
                case RETENTAR -> context.abandon();
            }
            log.debug("Mensagem processada com resultado {}", resultado);
        } catch (Exception e) {
            // Falha inesperada (ex.: desserialização): abandona para reentrega.
            log.error("Falha ao processar mensagem, abandonando para reentrega", e);
            context.abandon();
        }
    }
}
