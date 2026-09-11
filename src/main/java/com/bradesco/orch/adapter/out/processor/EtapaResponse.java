package com.bradesco.orch.adapter.out.processor;

/**
 * Body recebido da API externa (mock): {@code { "id": "<uuid>" }}. É o resultado
 * da etapa, persistido em {@code etapas[].callback.response}.
 */
public record EtapaResponse(String id) {
}
