package com.bradesco.orch.domain.port.in;

import com.bradesco.orch.domain.entity.Orquestracao;

import java.util.Optional;

/**
 * Porta de entrada: consulta o estado de uma orquestração (e suas etapas) pelo id.
 */
public interface ConsultarOrquestracaoUseCase {

    Optional<Orquestracao> buscarPorId(String orquestracaoId);
}
