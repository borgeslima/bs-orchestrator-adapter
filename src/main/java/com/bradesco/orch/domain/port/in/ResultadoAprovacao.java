package com.bradesco.orch.domain.port.in;

/**
 * Resultado de domínio da aprovação de uma etapa suspensa por interação
 * humana. Mantém o domínio livre de tipos HTTP: o adapter REST traduz este
 * resultado no código de status apropriado.
 */
public enum ResultadoAprovacao {
    /** Etapa retomada com sucesso: transicionou de PENDENTE_DE_INTERACAO para PENDENTE. */
    APROVADA,
    /**
     * Aprovação idempotente: a etapa já não estava mais {@code PENDENTE_DE_INTERACAO}
     * (já aprovada, em execução ou concluída). Nenhuma nova execução é disparada.
     */
    JA_PROCESSADA,
    /** Orquestração ou etapa inexistente. */
    NAO_ENCONTRADA
}
