package com.bradesco.orch.domain.port.out;

import java.util.Optional;

/**
 * Porta de saída para localizar o {@link EtapaProcessor} correspondente ao nome
 * canônico recebido na mensagem.
 */
public interface EtapaProcessorRegistry {

    Optional<EtapaProcessor<?, ?>> localizar(String nome);
}
