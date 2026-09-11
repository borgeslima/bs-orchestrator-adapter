package com.bradesco.orch.adapter.in.rest;

import com.bradesco.orch.domain.port.in.ConsultarOrquestracaoUseCase;
import com.bradesco.orch.domain.port.in.IniciarOrquestracaoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adapter de entrada REST. Inicia a orquestração e responde {@code 202 Accepted}
 * sem aguardar a conclusão das etapas (execução assíncrona via Service Bus).
 * Também expõe consultas de leitura do estado da orquestração e suas etapas.
 */
@Tag(name = "Cap Giro", description = "Orquestracao de capital de giro (cap-giro)")
@RestController
@RequestMapping("/cap-giro")
public class CapGiroController {

    private final IniciarOrquestracaoUseCase iniciarOrquestracao;
    private final ConsultarOrquestracaoUseCase consultarOrquestracao;

    public CapGiroController(IniciarOrquestracaoUseCase iniciarOrquestracao,
                             ConsultarOrquestracaoUseCase consultarOrquestracao) {
        this.iniciarOrquestracao = iniciarOrquestracao;
        this.consultarOrquestracao = consultarOrquestracao;
    }

    @Operation(summary = "Inicia uma orquestracao (assincrona)",
            description = "Cria e dispara uma orquestracao de cap-giro; nao aguarda a conclusao das etapas.")
    @ApiResponse(responseCode = "202", description = "Orquestracao aceita para processamento")
    @PostMapping
    public ResponseEntity<IniciarOrquestracaoResponse> iniciar(
            @RequestBody(required = false) IniciarOrquestracaoRequest request) {
        IniciarOrquestracaoRequest req = request != null ? request : new IniciarOrquestracaoRequest(null);
        String orquestracaoId = iniciarOrquestracao.iniciar(req.toComando());
        return ResponseEntity.accepted().body(new IniciarOrquestracaoResponse(orquestracaoId));
    }

    /** Retorna a orquestração completa (dados + etapas) ou 404 se não existir. */
    @Operation(summary = "Consulta a orquestracao completa (dados + etapas)")
    @ApiResponse(responseCode = "200", description = "Orquestracao encontrada")
    @ApiResponse(responseCode = "404", description = "Orquestracao nao encontrada")
    @GetMapping("/{orquestracaoId}")
    public ResponseEntity<OrquestracaoResponse> consultar(@PathVariable String orquestracaoId) {
        return consultarOrquestracao.buscarPorId(orquestracaoId)
                .map(OrquestracaoResponse::de)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Retorna apenas as etapas da orquestração, ou 404 se ela não existir. */
    @Operation(summary = "Consulta apenas as etapas da orquestracao")
    @ApiResponse(responseCode = "200", description = "Etapas encontradas")
    @ApiResponse(responseCode = "404", description = "Orquestracao nao encontrada")
    @GetMapping("/{orquestracaoId}/etapas")
    public ResponseEntity<java.util.List<OrquestracaoResponse.EtapaResponseView>> consultarEtapas(
            @PathVariable String orquestracaoId) {
        return consultarOrquestracao.buscarPorId(orquestracaoId)
                .map(OrquestracaoResponse::de)
                .map(OrquestracaoResponse::etapas)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
