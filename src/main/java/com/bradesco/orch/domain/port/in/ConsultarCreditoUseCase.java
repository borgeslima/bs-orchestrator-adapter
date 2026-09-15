package com.bradesco.orch.domain.port.in;

import com.bradesco.orch.domain.entity.Credito;

import java.util.Optional;

/**
 * Porta de entrada: consulta o crédito de negócio (e seu histórico) pelo id.
 */
public interface ConsultarCreditoUseCase {

    Optional<Credito> buscarPorId(String creditoId);
}
