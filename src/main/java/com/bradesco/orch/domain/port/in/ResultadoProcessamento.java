package com.bradesco.orch.domain.port.in;

/**
 * Resultado de domínio do processamento de uma etapa. Mantém o domínio livre de
 * tipos do Service Bus: o adapter de mensageria traduz este resultado em
 * ACK/retry/dead-letter.
 */
public enum ResultadoProcessamento {
    /** Etapa executada com sucesso (avançou ou concluiu a orquestração). */
    SUCESSO,
    /** Mensagem duplicada (etapa já CONCLUIDA/EM_EXECUCAO ou perdeu a corrida). */
    DUPLICIDADE,
    /** Falha transitória com tentativas abaixo do limite: deve ser reentregue. */
    RETENTAR,
    /** Falha definitiva (limite de tentativas atingido). */
    ERRO_FINAL,
    /** Nenhum processor registrado para o nome da etapa. */
    PROCESSOR_NAO_ENCONTRADO
}
