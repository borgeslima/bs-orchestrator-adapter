package com.bradesco.orch.domain.entity;

/**
 * Abstração de I/O de uma chamada a API externa com callback de sucesso/erro.
 *
 * <p>Reaproveitada como detalhe interno de implementação dos processors concretos
 * (adapter/out), que usam {@code ApiStep} + {@code RestClient} para invocar a API
 * externa (mock) e reagir via {@link Callback}. Não é conhecida pelo motor de
 * orquestração — a porta que o domínio enxerga é
 * {@code com.bradesco.orch.domain.port.out.EtapaProcessor}.</p>
 */
public interface ApiStep<I, O> {
    O execute(I input, Callback<O> callback);
}
