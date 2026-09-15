package com.bradesco.orch.domain.port.out;

/**
 * Porta de saída dos processors de etapa. É a interface que o motor de
 * orquestração conhece e que o registry indexa por {@link #name()}.
 *
 * @param <I> tipo do input de execução
 * @param <O> tipo do resultado, persistido em {@code etapas[].callback.response}
 */
public interface EtapaProcessor<I, O> {

    /** Nome canônico único da etapa (chave no registry). */
    String name();

    /** Executa a lógica da etapa (na POC, aciona a API externa mock). */
    O execute(I input);

    /** Normaliza/trata o resultado que será persistido como {@code callback.response}. */
    void callback(O response);

    /**
     * Indica se esta etapa depende do resultado ({@code callback.response}) da
     * <b>etapa imediatamente anterior</b>. Quando {@code true}, o motor entrega
     * esse callback como {@code input} de {@link #execute(Object)}; quando
     * {@code false} (padrão), o input é {@code null}.
     *
     * <p>Mantém o encadeamento explícito e no controle de cada processor: só quem
     * declara a dependência recebe o dado da etapa anterior.</p>
     */
    default boolean dependeDaEtapaAnterior() {
        return false;
    }
}
