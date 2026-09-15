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
     * Exemplo do ponto de extensão com <b>DTO tipado</b> (sem manipulador): o input
     * (callback da simulação) é convertido em {@link SimulacaoCallback} e o request
     * é montado a partir dele, com segurança de tipo. Se a etapa também tivesse um
     * manipulador registrado, o input já viria transformado — por isso, ao optar
     * pelo DTO aqui, não se registra um manipulador para esta etapa.
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
