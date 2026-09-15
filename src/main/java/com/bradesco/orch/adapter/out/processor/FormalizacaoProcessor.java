package com.bradesco.orch.adapter.out.processor;

import com.bradesco.orch.domain.entity.EtapasCapGiro;
import org.springframework.stereotype.Component;

/** Processor da etapa FORMALIZACAO. Depende do resultado da etapa anterior (SIMULACAO). */
@Component
public class FormalizacaoProcessor extends AbstractEtapaProcessor {

    public FormalizacaoProcessor(MockEtapaHttpClient httpClient) {
        super(httpClient);
    }

    @Override
    public String name() {
        return EtapasCapGiro.FORMALIZACAO;
    }

    /**
     * A formalização usa o resultado da simulação (etapa imediatamente anterior):
     * o motor entrega o {@code callback.response} da simulação como input.
     */
    @Override
    public boolean dependeDaEtapaAnterior() {
        return true;
    }

    /**
     * Ponto de extensão com <b>DTO tipado</b>: o input (callback da simulação) é
     * convertido em {@link SimulacaoCallback} e o request é montado a partir dele,
     * com segurança de tipo.
     */
    @Override
    protected EtapaRequest montarRequest(Object input) {
        SimulacaoCallback simulacao = converter(input, SimulacaoCallback.class);
        if (simulacao != null && simulacao.id() != null) {
            return new EtapaRequest(simulacao.id());
        }
        // Sem dado utilizável: cai no comportamento padrão (gera UUID, etc.).
        return super.montarRequest(input);
    }
}
