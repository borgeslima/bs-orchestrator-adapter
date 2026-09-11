package com.bradesco.orch.adapter.out.processor;

/**
 * Body enviado no POST à API externa (mock): {@code { "id": "<uuid>" }}.
 */
public record EtapaRequest(String id) {
}
