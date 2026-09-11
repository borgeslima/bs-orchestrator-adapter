package com.bradesco.orch.application;

import com.bradesco.orch.domain.entity.Orquestracao;
import com.bradesco.orch.domain.port.in.ConsultarOrquestracaoUseCase;
import com.bradesco.orch.domain.port.out.OrquestracaoRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Consulta o estado de uma orquestração no MongoDB (fonte da verdade), incluindo
 * suas etapas. Apenas leitura.
 */
@Service
public class ConsultarOrquestracaoService implements ConsultarOrquestracaoUseCase {

    private final OrquestracaoRepository repository;

    public ConsultarOrquestracaoService(OrquestracaoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Orquestracao> buscarPorId(String orquestracaoId) {
        return repository.buscarPorId(orquestracaoId);
    }
}
