package com.bradesco.orch.adapter.out.processor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO tipado do {@code callback.response} produzido pela etapa de <b>simulação</b>.
 * É o contrato que a etapa seguinte (formalização) consome quando prefere um DTO
 * em vez do mapa genérico.
 *
 * <p>Vive no adapter de processor (detalhe de integração), não no domínio.
 * {@code @JsonIgnoreProperties(ignoreUnknown = true)} torna a conversão tolerante
 * a campos extras que o callback possa carregar.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SimulacaoCallback(String id) {
}
