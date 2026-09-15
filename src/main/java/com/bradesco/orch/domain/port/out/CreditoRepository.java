package com.bradesco.orch.domain.port.out;

import com.bradesco.orch.domain.entity.Credito;
import com.bradesco.orch.domain.entity.HistoricoCredito;

import java.util.Optional;

/**
 * Porta de saída para persistência do {@link Credito} (domínio de negócio),
 * separada do estado técnico da orquestração.
 */
public interface CreditoRepository {

    Credito salvar(Credito credito);

    Optional<Credito> buscarPorId(String id);

    /**
     * Adiciona uma nova entrada ao {@code historico} do crédito informado (append),
     * sem reescrever as entradas anteriores.
     */
    void registrarHistorico(String creditoId, HistoricoCredito entrada);
}
