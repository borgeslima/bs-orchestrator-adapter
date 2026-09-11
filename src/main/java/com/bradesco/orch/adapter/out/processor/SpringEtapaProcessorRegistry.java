package com.bradesco.orch.adapter.out.processor;

import com.bradesco.orch.domain.port.out.EtapaProcessor;
import com.bradesco.orch.domain.port.out.EtapaProcessorRegistry;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registry que indexa os {@link EtapaProcessor} por {@link EtapaProcessor#name()}.
 * Falha na inicialização se dois processors declararem o mesmo nome (erro de
 * configuração).
 */
@Component
public class SpringEtapaProcessorRegistry implements EtapaProcessorRegistry {

    private final Map<String, EtapaProcessor<?, ?>> porNome = new HashMap<>();

    public SpringEtapaProcessorRegistry(List<EtapaProcessor<?, ?>> processors) {
        for (EtapaProcessor<?, ?> processor : processors) {
            EtapaProcessor<?, ?> anterior = porNome.putIfAbsent(processor.name(), processor);
            if (anterior != null) {
                throw new IllegalStateException(
                        "Mais de um EtapaProcessor registrado com o nome: " + processor.name());
            }
        }
    }

    @Override
    public Optional<EtapaProcessor<?, ?>> localizar(String nome) {
        return Optional.ofNullable(porNome.get(nome));
    }
}
