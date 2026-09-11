package com.bradesco.orch.domain.port.in;

/**
 * Porta de entrada: aprova (retoma) uma etapa suspensa por interação humana,
 * disparando a continuação da orquestração pela mesma máquina de estados —
 * não cria uma nova orquestração nem executa a etapa de forma síncrona.
 */
public interface AprovarEtapaUseCase {

    ResultadoAprovacao aprovar(AprovarEtapaComando comando);
}
