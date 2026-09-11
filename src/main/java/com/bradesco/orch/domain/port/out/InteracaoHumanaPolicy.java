package com.bradesco.orch.domain.port.out;

/**
 * Porta de saída que decide, após a execução bem-sucedida de uma etapa, se ela
 * deve ser <b>suspensa</b> aguardando interação humana ({@code PENDENTE_DE_INTERACAO})
 * em vez de concluir e avançar automaticamente para a próxima etapa.
 *
 * <p>Mantém o motor de orquestração (application) livre de conhecer a origem da
 * decisão (configuração estática, regra de negócio, flag por etapa, etc.) — a
 * implementação concreta vive nos adapters.</p>
 */
public interface InteracaoHumanaPolicy {

    /**
     * @param etapa    nome canônico da etapa que acabou de executar com sucesso
     * @param resposta resultado retornado pelo processor da etapa
     * @return {@code true} se a etapa deve ser suspensa aguardando aprovação
     *         humana em vez de concluir automaticamente
     */
    boolean requerInteracao(String etapa, Object resposta);
}
