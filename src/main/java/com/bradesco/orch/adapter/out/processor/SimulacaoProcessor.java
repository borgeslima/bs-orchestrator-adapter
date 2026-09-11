package com.bradesco.orch.adapter.out.processor;

import com.bradesco.orch.domain.entity.EtapasCapGiro;
import org.springframework.stereotype.Component;

/** Processor da etapa SIMULACAO. */
@Component
public class SimulacaoProcessor extends AbstractEtapaProcessor {

    public SimulacaoProcessor(MockEtapaHttpClient httpClient) {
        super(httpClient);
    }

    @Override
    public String name() {
        return EtapasCapGiro.SIMULACAO;
    }
}
