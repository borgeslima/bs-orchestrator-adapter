package com.bradesco.orch.domain.port.out;

import java.util.Map;

/**
 * Porta de saída que transforma o <b>input</b> de uma etapa antes de sua
 * execução. É o "manipulador de campos" declarado por código: recebe o payload
 * bruto (tipicamente o {@code callback.response} da etapa anterior, como mapa
 * genérico) e devolve o mapa que será entregue ao processor.
 *
 * <p>Permite seleção, renomeação e enriquecimento de campos sem que o motor de
 * orquestração conheça o formato de negócio — cada etapa que precisa fornece a
 * sua implementação, indexada por {@link #etapa()}.</p>
 */
public interface ManipuladorEntradaEtapa {

    /** Nome canônico da etapa cujo input este manipulador transforma. */
    String etapa();

    /**
     * Transforma o input bruto no input efetivo da etapa.
     *
     * @param entradaBruta payload de origem (ex.: callback da etapa anterior);
     *                     pode ser {@code null} ou vazio
     * @return mapa com os campos que a etapa deve receber (nunca {@code null})
     */
    Map<String, Object> manipular(Map<String, Object> entradaBruta);
}
