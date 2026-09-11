package com.bradesco.orch.domain.port.out;

import com.bradesco.orch.domain.entity.Orquestracao;

import java.util.Optional;

/**
 * Porta de saída para persistência do estado da orquestração (fonte da verdade).
 */
public interface OrquestracaoRepository {

    Orquestracao salvar(Orquestracao orquestracao);

    Optional<Orquestracao> buscarPorId(String id);

    /**
     * Transição atômica condicional {@code PENDENTE -> EM_EXECUCAO} de uma etapa,
     * incrementando {@code controle.tentativas_realizadas} e {@code version} na
     * mesma operação. Só altera se a etapa ainda estiver {@code PENDENTE}.
     *
     * @return {@code true} apenas quando exatamente um documento foi alterado
     *         (o concorrente que venceu a corrida); {@code false} em duplicidade.
     */
    boolean transicionarEtapaParaEmExecucao(String orquestracaoId, String etapa);

    /**
     * Persiste o estado atualizado da orquestração (callback/status/tentativas),
     * aplicando optimistic locking por {@code version}.
     */
    void atualizar(Orquestracao orquestracao);
}
