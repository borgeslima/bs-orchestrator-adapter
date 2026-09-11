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
}
