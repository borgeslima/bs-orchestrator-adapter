package com.bradesco.orch.domain.port.in;

/**
 * Porta de entrada: processa uma etapa acionada por mensagem do Service Bus.
 */
public interface ProcessarEtapaUseCase {

    ResultadoProcessamento processar(ProcessarEtapaComando comando);
}
