package com.bradesco.orch.domain.port.in;

/**
 * Porta de entrada: inicia uma orquestração e retorna o {@code orquestracaoId}.
 */
public interface IniciarOrquestracaoUseCase {

    /**
     * Cria a orquestração (persistindo o estado) e publica a mensagem da primeira
     * etapa. Retorna o id da orquestração criada.
     */
    String iniciar(IniciarOrquestracaoComando comando);
}
