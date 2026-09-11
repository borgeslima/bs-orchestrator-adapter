package com.bradesco.orch.adapter.out.processor;

import com.bradesco.orch.domain.entity.EtapasCapGiro;
import org.springframework.stereotype.Component;

/** Processor da etapa OFERTA. */
@Component
public class OfertaProcessor extends AbstractEtapaProcessor {

    public OfertaProcessor(MockEtapaHttpClient httpClient) {
        super(httpClient);
    }

    @Override
    public String name() {
        return EtapasCapGiro.OFERTA;
    }
}
