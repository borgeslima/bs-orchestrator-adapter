package com.bradesco.orch.adapter.out.processor;

import com.bradesco.orch.domain.entity.EtapasCapGiro;
import org.springframework.stereotype.Component;

/** Processor da etapa ELEGIBILIDADE. */
@Component
public class ElegibilidadeProcessor extends AbstractEtapaProcessor {

    public ElegibilidadeProcessor(MockEtapaHttpClient httpClient) {
        super(httpClient);
    }

    @Override
    public String name() {
        return EtapasCapGiro.ELEGIBILIDADE;
    }
}
