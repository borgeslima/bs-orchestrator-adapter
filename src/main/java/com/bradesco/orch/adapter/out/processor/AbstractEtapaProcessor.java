package com.bradesco.orch.adapter.out.processor;

import com.bradesco.orch.domain.port.out.EtapaProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Classe base dos processors de etapa. Concentra o comportamento comum: em
 * {@link #execute(Object)} monta o {@link EtapaRequest} a partir do input
 * recebido, delega ao {@link MockEtapaHttpClient} (POST no endpoint mock) e
 * devolve o resultado como <b>mapa genérico</b> ({@code { "id": ... }}).
 *
 * <p>O motor de orquestração é agnóstico: o callback trafega entre etapas como
 * {@code Map<String,Object>}, sem que o motor conheça tipos de negócio.</p>
 *
 * <p><b>Ponto de extensão (DTO tipado):</b> um processor concreto que prefira
 * trabalhar com um DTO em vez do mapa genérico pode sobrescrever
 * {@link #montarRequest(Object)}, converter o input com {@link #converter(Object, Class)}
 * e decidir o que enviar no POST. Quem não sobrescreve mantém o comportamento
 * genérico (procura {@code idSimulacao}/{@code id} no mapa, ou gera um UUID).</p>
 */
public abstract class AbstractEtapaProcessor implements EtapaProcessor<Object, Map<String, Object>> {

    /** Conversor interno Map <-> DTO. Sem estado de negócio; suporta tipos de data/hora. */
    private static final ObjectMapper CONVERSOR = new ObjectMapper().registerModule(new JavaTimeModule());

    protected final MockEtapaHttpClient httpClient;

    protected AbstractEtapaProcessor(MockEtapaHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public abstract String name();

    @Override
    public Map<String, Object> execute(Object input) {
        EtapaResponse resposta = httpClient.chamar(montarRequest(input));
        return paraMapa(resposta);
    }

    /**
     * Monta o {@link EtapaRequest} do POST a partir do input genérico. Implementação
     * padrão agnóstica: reaproveita um {@link EtapaRequest}, ou procura
     * {@code idSimulacao}/{@code id} num {@link Map}, ou gera um UUID.
     *
     * <p>Processors que trabalham com DTO devem <b>sobrescrever</b> este método,
     * tipar o input via {@link #converter(Object, Class)} e montar o request a
     * partir do DTO.</p>
     */
    protected EtapaRequest montarRequest(Object input) {
        if (input instanceof EtapaRequest req && req.id() != null) {
            return req;
        }
        if (input instanceof Map<?, ?> dados) {
            Object id = dados.containsKey("idSimulacao") ? dados.get("idSimulacao") : dados.get("id");
            if (id != null) {
                return new EtapaRequest(id.toString());
            }
        }
        return new EtapaRequest(UUID.randomUUID().toString());
    }

    /**
     * Converte o input genérico (tipicamente um {@code Map} — o callback da etapa
     * anterior) em um DTO tipado. Útil para processors que preferem segurança de
     * tipo em vez de acessar o mapa por chave. Retorna {@code null} se o input for
     * {@code null}.
     */
    protected <T> T converter(Object input, Class<T> tipo) {
        if (input == null) {
            return null;
        }
        return CONVERSOR.convertValue(input, tipo);
    }

    /** Converte a resposta do mock em mapa genérico (callback trafegado). */
    private Map<String, Object> paraMapa(EtapaResponse resposta) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        if (resposta != null) {
            mapa.put("id", resposta.id());
        }
        return mapa;
    }

    @Override
    public void callback(Map<String, Object> response) {
        // Resultado que o motor persiste em etapas[].callback.response.
        // Na POC não há normalização adicional.
    }
}
