package com.bradesco.orch.adapter.out.messaging;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.bradesco.orch.domain.port.out.MensagemEtapa;
import com.bradesco.orch.domain.port.out.MensagemPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Publica no Azure Service Bus apenas o contrato leve
 * {@code { orquestracaoId, etapa, correlationId }} — nunca o documento completo.
 */
@Component
public class ServiceBusMensagemPublisher implements MensagemPublisher {

    private static final Logger log = LoggerFactory.getLogger(ServiceBusMensagemPublisher.class);

    private final ServiceBusSenderClient senderClient;
    private final ObjectMapper objectMapper;

    public ServiceBusMensagemPublisher(ServiceBusSenderClient senderClient, ObjectMapper objectMapper) {
        this.senderClient = senderClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publicar(MensagemEtapa mensagem) {
        try {
            String corpo = objectMapper.writeValueAsString(mensagem);
            ServiceBusMessage sbMessage = new ServiceBusMessage(corpo);
            if (mensagem.correlationId() != null) {
                sbMessage.setCorrelationId(mensagem.correlationId());
            }
            senderClient.sendMessage(sbMessage);
            log.debug("Mensagem publicada: {}", corpo);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar MensagemEtapa", e);
        }
    }
}
