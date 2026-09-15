package com.bradesco.orch.adapter.out.policy;

import com.bradesco.orch.domain.port.out.InteracaoPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;

/**
 * Implementação simples da {@link InteracaoPolicy}, baseada em uma lista de nomes
 * de etapas configurada via propriedade externalizada
 * {@code orch.etapas.interacao-humana} (nomes canônicos separados por vírgula).
 *
 * <p>Qualquer etapa cujo nome esteja nessa lista é suspensa em
 * {@code PENDENTE_DE_INTERACAO} após executar com sucesso, em vez de concluir e
 * avançar automaticamente. Por padrão, nenhuma etapa exige interação (lista
 * vazia) — comportamento 100% automático, compatível com o fluxo existente.</p>
 */
@Component
public class PropriedadesInteracaoPolicy implements InteracaoPolicy {

    private final Set<String> etapasComInteracao;

    public PropriedadesInteracaoPolicy(
            @Value("${orch.etapas.interacao-humana:}") String etapasConfiguradas) {
        this.etapasComInteracao = parse(etapasConfiguradas);
    }

    private static Set<String> parse(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean requerInteracao(String etapa, Object resposta) {
        return etapasComInteracao.contains(etapa);
    }
}
