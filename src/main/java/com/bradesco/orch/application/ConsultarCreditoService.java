package com.bradesco.orch.application;

import com.bradesco.orch.domain.entity.Credito;
import com.bradesco.orch.domain.port.in.ConsultarCreditoUseCase;
import com.bradesco.orch.domain.port.out.CreditoRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Consulta o crédito de negócio no MongoDB (coleção {@code creditos}), incluindo
 * seu histórico. Apenas leitura.
 */
@Service
public class ConsultarCreditoService implements ConsultarCreditoUseCase {

    private final CreditoRepository creditoRepository;

    public ConsultarCreditoService(CreditoRepository creditoRepository) {
        this.creditoRepository = creditoRepository;
    }

    @Override
    public Optional<Credito> buscarPorId(String creditoId) {
        return creditoRepository.buscarPorId(creditoId);
    }
}
