package com.bradesco.orch.domain.port.out;

import com.bradesco.orch.domain.entity.Orquestracao;
import com.bradesco.orch.domain.entity.RegistroInteracao;

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
     * Transição atômica condicional {@code PENDENTE_DE_INTERACAO -> PENDENTE} de
     * uma etapa, gravando o {@link RegistroInteracao} da aprovação na mesma
     * operação. Só altera se a etapa ainda estiver {@code PENDENTE_DE_INTERACAO} —
     * garante a idempotência do endpoint de aprovação: uma segunda chamada para a
     * mesma etapa (já aprovada/em execução/concluída) não altera nada.
     *
     * @return {@code true} apenas quando exatamente um documento foi alterado
     *         (a aprovação que efetivamente retomou a etapa); {@code false} caso
     *         a etapa já não esteja mais {@code PENDENTE_DE_INTERACAO}.
     */
    boolean transicionarEtapaDeInteracaoParaPendente(String orquestracaoId, String etapa,
                                                      RegistroInteracao registroInteracao);

    /**
     * Persiste o estado atualizado da orquestração (callback/status/tentativas),
     * aplicando optimistic locking por {@code version}.
     */
    void atualizar(Orquestracao orquestracao);
}
