package com.bradesco.orch.domain.port.out;

/**
 * Porta de saída que decide, após a execução bem-sucedida de uma etapa, se ela
 * deve ser <b>suspensa</b> aguardando uma interação ({@code PENDENTE_DE_INTERACAO})
 * em vez de concluir e avançar automaticamente para a próxima etapa.
 *
 * <p>A interação pode ter origem humana ou de máquina — a política apenas indica
 * a necessidade de suspensão; a origem é registrada no momento da retomada.
 * Mantém o motor de orquestração (application) livre de conhecer a fonte da
 * decisão (configuração estática, regra de negócio, flag por etapa, etc.) — a
 * implementação concreta vive nos adapters.</p>
 */
public interface InteracaoPolicy {

    /**
     * @param etapa    nome canônico da etapa que acabou de executar com sucesso
     * @param resposta resultado retornado pelo processor da etapa
     * @return {@code true} se a etapa deve ser suspensa aguardando uma interação
     *         em vez de concluir automaticamente
     */
    boolean requerInteracao(String etapa, Object resposta);
}
