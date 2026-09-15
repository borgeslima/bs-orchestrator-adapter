package com.bradesco.orch.adapter.out.manipulador;

import com.bradesco.orch.domain.port.out.ManipuladorEntradaEtapa;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Base utilitária para {@link ManipuladorEntradaEtapa} declarados por código.
 * Oferece um pequeno DSL de manipulação de campos, aplicado em ordem sobre o
 * mapa de entrada:
 *
 * <ul>
 *   <li>{@link #selecionar(String, String)} — copia um campo da origem para o
 *       destino (renomeando quando os nomes diferem). Suporta caminho aninhado
 *       na origem (ex.: {@code "dados.simulacao.id"}).</li>
 *   <li>{@link #enriquecer(String, Object)} — adiciona um valor fixo no destino.</li>
 *   <li>{@link #derivar(String, Function)} — adiciona um valor calculado a partir
 *       do mapa de entrada bruto.</li>
 * </ul>
 *
 * <p>Nada de negócio vaza para o motor: cada etapa concreta estende esta base,
 * declara sua {@link #etapa()} e as regras no construtor.</p>
 */
public abstract class AbstractManipuladorEntrada implements ManipuladorEntradaEtapa {

    /** Uma operação de transformação: recebe origem (bruta) e escreve no destino. */
    private interface Regra {
        void aplicar(Map<String, Object> origem, Map<String, Object> destino);
    }

    private final List<Regra> regras = new ArrayList<>();

    /** Copia {@code caminhoOrigem} (aninhado por '.') para {@code campoDestino}. */
    protected AbstractManipuladorEntrada selecionar(String caminhoOrigem, String campoDestino) {
        regras.add((origem, destino) -> {
            Object valor = extrair(origem, caminhoOrigem);
            if (valor != null) {
                destino.put(campoDestino, valor);
            }
        });
        return this;
    }

    /** Copia o campo mantendo o mesmo nome na origem e no destino. */
    protected AbstractManipuladorEntrada selecionar(String campo) {
        return selecionar(campo, campo);
    }

    /** Adiciona um valor fixo ao destino. */
    protected AbstractManipuladorEntrada enriquecer(String campoDestino, Object valorFixo) {
        regras.add((origem, destino) -> destino.put(campoDestino, valorFixo));
        return this;
    }

    /** Adiciona um valor calculado a partir do mapa de entrada bruto. */
    protected AbstractManipuladorEntrada derivar(String campoDestino,
                                                 Function<Map<String, Object>, Object> calculo) {
        regras.add((origem, destino) -> destino.put(campoDestino, calculo.apply(origem)));
        return this;
    }

    @Override
    public Map<String, Object> manipular(Map<String, Object> entradaBruta) {
        Map<String, Object> origem = (entradaBruta != null) ? entradaBruta : Map.of();
        Map<String, Object> destino = new LinkedHashMap<>();
        for (Regra regra : regras) {
            regra.aplicar(origem, destino);
        }
        return destino;
    }

    /** Resolve um caminho aninhado ({@code "a.b.c"}) dentro de mapas encadeados. */
    @SuppressWarnings("unchecked")
    private static Object extrair(Map<String, Object> origem, String caminho) {
        String[] partes = caminho.split("\\.");
        Object atual = origem;
        for (String parte : partes) {
            if (!(atual instanceof Map<?, ?> mapa)) {
                return null;
            }
            atual = ((Map<String, Object>) mapa).get(parte);
            if (atual == null) {
                return null;
            }
        }
        return atual;
    }
}
