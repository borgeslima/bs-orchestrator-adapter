package com.bradesco.orch.domain.port.out;

import java.util.Optional;

/**
 * Porta de saída para localizar o {@link ManipuladorEntradaEtapa} de uma etapa
 * (quando existir). Etapas sem manipulador registrado recebem o input bruto sem
 * transformação.
 */
public interface ManipuladorEntradaRegistry {

    Optional<ManipuladorEntradaEtapa> localizar(String etapa);
}
