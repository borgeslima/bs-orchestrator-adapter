package com.bradesco.orch.adapter.out.manipulador;

import com.bradesco.orch.domain.port.out.ManipuladorEntradaEtapa;
import com.bradesco.orch.domain.port.out.ManipuladorEntradaRegistry;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registry que indexa os {@link ManipuladorEntradaEtapa} por
 * {@link ManipuladorEntradaEtapa#etapa()}. Falha na inicialização se dois
 * manipuladores declararem a mesma etapa (erro de configuração).
 */
@Component
public class SpringManipuladorEntradaRegistry implements ManipuladorEntradaRegistry {

    private final Map<String, ManipuladorEntradaEtapa> porEtapa = new HashMap<>();

    public SpringManipuladorEntradaRegistry(List<ManipuladorEntradaEtapa> manipuladores) {
        for (ManipuladorEntradaEtapa m : manipuladores) {
            ManipuladorEntradaEtapa anterior = porEtapa.putIfAbsent(m.etapa(), m);
            if (anterior != null) {
                throw new IllegalStateException(
                        "Mais de um ManipuladorEntradaEtapa registrado para a etapa: " + m.etapa());
            }
        }
    }

    @Override
    public Optional<ManipuladorEntradaEtapa> localizar(String etapa) {
        return Optional.ofNullable(porEtapa.get(etapa));
    }
}
