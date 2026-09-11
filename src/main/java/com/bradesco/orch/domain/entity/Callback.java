package com.bradesco.orch.domain.entity;

/**
 * Interface funcional de tratamento de sucesso/erro de uma chamada a API externa.
 *
 * <p>Reaproveitada internamente pelos processors concretos (adapter/out) junto de
 * {@link ApiStep}. Não confundir com o sub-documento persistido {@code callback.response},
 * que é modelado por {@code com.bradesco.orch.domain.entity.RespostaEtapa}.</p>
 */
public interface Callback<T> {

    void onSuccess(T response);

    void onError(Exception error);
}
