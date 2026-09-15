package com.bradesco.orch.domain.port.out;

/**
 * Sinaliza uma falha <b>definitiva</b> (não transitória) na execução de uma etapa
 * — por exemplo, quando a API externa responde {@code 400 Bad Request}. Não deve
 * ser retentada.
 *
 * <p>Carrega o {@link #getCorpoErro() corpo do erro} para que o motor de
 * orquestração o persista em {@code etapas[].callback.response}. É uma exceção de
 * domínio: os adapters (out) a lançam e o serviço de aplicação a interpreta, sem
 * que o motor conheça tipos HTTP.</p>
 */
public class FalhaDefinitivaEtapaException extends RuntimeException {

    /** Corpo (payload) do erro retornado pela dependência externa. */
    private final transient Object corpoErro;

    public FalhaDefinitivaEtapaException(String mensagem, Object corpoErro) {
        super(mensagem);
        this.corpoErro = corpoErro;
    }

    public Object getCorpoErro() {
        return corpoErro;
    }
}
