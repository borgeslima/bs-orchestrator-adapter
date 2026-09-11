package com.bradesco.orch.adapter.out.processor;

import com.bradesco.orch.domain.entity.EtapasCapGiro;
import org.springframework.stereotype.Component;

/** Processor da etapa FORMALIZACAO. */
@Component
public class FormalizacaoProcessor extends AbstractEtapaProcessor {

    public FormalizacaoProcessor(MockEtapaHttpClient httpClient) {
        super(httpClient);
    }

    @Override
    public String name() {
        return EtapasCapGiro.FORMALIZACAO;
    }
}
