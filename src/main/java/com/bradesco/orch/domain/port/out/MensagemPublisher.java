package com.bradesco.orch.domain.port.out;

/**
 * Porta de saída para publicação de mensagens de transição de etapa no Service Bus.
 */
public interface MensagemPublisher {

    void publicar(MensagemEtapa mensagem);
}
