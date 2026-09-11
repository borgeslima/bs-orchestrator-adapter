# Plano de Implementação — Orquestrador de APIs baseado em etapas e Service Bus

- [ ] 1. Criar a fundação de domínio (enums e entidades) no pacote `com.bradesco.orch.domain.entity`
  - Criar os enums `StatusOrquestracao` (`PENDENTE`, `EM_EXECUCAO`, `CONCLUIDA`, `ERRO`) e `StatusEtapa` (`AGUARDANDO`, `PENDENTE`, `EM_EXECUCAO`, `CONCLUIDA`, `ERRO`).
  - Criar as entidades puras (sem framework) `Controle` (com `tentativasRealizadas` e `limiteRetentativas`), `RespostaEtapa` (encapsula `callback.response`) e `Etapa` (`name`, `order`, `status`, `controle`, `callback`).
  - Definir as 4 etapas concretas da `baas:cap-giro` em ordem fixa como constantes de nome canônico, na ordem: OFERTA (`baas:etapa:cap-giro:oferta`), ELEGIBILIDADE (`baas:etapa:cap-giro:elegibilidade`), SIMULACAO (`baas:etapa:cap-giro:simulacao`) e FORMALIZACAO (`baas:etapa:cap-giro:formalizacao`).
  - Criar o agregado `Orquestracao` (`id`, `name`, `order`, `status`, `dataCriacao`, `dataAtualizacao`, `version`, `etapas`) com os campos e construtores/fábrica básicos, sem os métodos de transição ainda.
  - _Requisitos: 1.1, 1.2, 1.3, 4.1, 9.2_

- [ ] 2. Implementar o motor de transição no domínio (métodos de negócio do agregado)
  - Implementar em `Etapa` os métodos `podeExecutar()` (true quando `PENDENTE`) e `atingiuLimite()`; implementar em `Controle` `incrementar()` e `atingiuLimite()` (`tentativasRealizadas >= limiteRetentativas`).
  - Implementar em `Orquestracao` os métodos `etapaAtual(nome)`, `proximaEtapa(nomeAtual)` (por `order`), `ehUltima(nome)`, `concluirEtapaEAvancar(nome, response)` (conclui etapa atual, avança próxima `AGUARDANDO`->`PENDENTE` ou conclui a orquestração) e `marcarErro(nome)` (etapa e orquestração em `ERRO`).
  - Escrever testes unitários (JUnit) cobrindo inicialização (primeira etapa `PENDENTE`, demais `AGUARDANDO`), `proximaEtapa`, `ehUltima`, `concluirEtapaEAvancar`, `marcarErro`, `podeExecutar` e `atingiuLimite`.
  - _Requisitos: 1.3, 4.1, 4.2, 4.5, 5.1, 5.2, 6.1, 6.4_

- [ ] 3. Definir as portas de entrada (use cases) em `com.bradesco.orch.domain.port.in`
  - Criar `IniciarOrquestracaoUseCase` (`String iniciar(IniciarOrquestracaoComando)`) e o comando `IniciarOrquestracaoComando`.
  - Criar `ProcessarEtapaUseCase` (`ResultadoProcessamento processar(ProcessarEtapaComando)`) e o comando `ProcessarEtapaComando` (`orquestracaoId`, `etapa`, `correlationId`).
  - Criar o enum de domínio `ResultadoProcessamento` (`SUCESSO`, `DUPLICIDADE`, `RETENTAR`, `ERRO_FINAL`, `PROCESSOR_NAO_ENCONTRADO`) para manter o domínio livre de tipos do Service Bus.
  - _Requisitos: 1.1, 2.1, 9.3_

- [ ] 4. Definir as portas de saída (ports) em `com.bradesco.orch.domain.port.out`
  - Criar `OrquestracaoRepository` com `salvar`, `buscarPorId`, `transicionarEtapaParaEmExecucao(orquestracaoId, etapa)` e `atualizar`.
  - Criar `MensagemPublisher` (`publicar(MensagemEtapa)`) e o contrato `MensagemEtapa` (record com `orquestracaoId`, `etapa`, `correlationId`).
  - Criar a porta `EtapaProcessor<I, O>` (`name()`, `execute(input)`, `callback(response)`) e a porta `EtapaProcessorRegistry` (`Optional<EtapaProcessor<?, ?>> localizar(nome)`).
  - _Requisitos: 2.3, 4.3, 8.1, 8.2, 9.1, 9.3, 10.2, 10.4_

- [ ] 5. Implementar `IniciarOrquestracaoService` em `com.bradesco.orch.application`
  - Implementar `IniciarOrquestracaoUseCase`: montar o agregado `Orquestracao` (status `PENDENTE`) com exatamente as 4 etapas concretas na ordem fixa OFERTA (`baas:etapa:cap-giro:oferta`, `order` 0), ELEGIBILIDADE (`baas:etapa:cap-giro:elegibilidade`, `order` 1), SIMULACAO (`baas:etapa:cap-giro:simulacao`, `order` 2) e FORMALIZACAO (`baas:etapa:cap-giro:formalizacao`, `order` 3), definindo a primeira (OFERTA) como `PENDENTE` e as demais como `AGUARDANDO`.
  - Persistir a orquestração via `OrquestracaoRepository.salvar` e só então publicar a mensagem da primeira etapa (`baas:etapa:cap-giro:oferta`) via `MensagemPublisher`.
  - Garantir que, em falha de persistência, a exceção seja propagada sem publicar mensagem.
  - Escrever testes unitários (Mockito) verificando a montagem das 4 etapas na ordem correta (primeira `PENDENTE`, demais `AGUARDANDO`), a ordem persistir->publicar, o conteúdo da `MensagemEtapa` da primeira etapa e a ausência de publicação quando `salvar` falha.
  - _Requisitos: 1.1, 1.2, 1.3, 1.4, 1.6, 9.2_

- [ ] 6. Implementar `ProcessarEtapaService` em `com.bradesco.orch.application` (motor de transição + idempotência + retry)
  - Implementar `ProcessarEtapaUseCase`: buscar orquestração por id; localizar a etapa pelo nome; localizar o processor no registry (retornar `PROCESSOR_NAO_ENCONTRADO` e marcar etapa/orquestração `ERRO` quando ausente).
  - Aplicar guarda de idempotência por estado (`CONCLUIDA`/`EM_EXECUCAO` -> `DUPLICIDADE`; `PENDENTE` -> prossegue) e a transição atômica `PENDENTE`->`EM_EXECUCAO` via `transicionarEtapaParaEmExecucao` (tratar retorno `false` como `DUPLICIDADE`).
  - Executar `execute(input)` + `callback(response)`; em sucesso persistir `callback.response`, concluir etapa e avançar/publicar próxima (ou concluir orquestração na última); em falha aplicar retry (`tentativas < limite` -> volta `PENDENTE` + `RETENTAR`; limite atingido -> `ERRO`/`ERRO` + `ERRO_FINAL`).
  - Escrever testes unitários (Mockito) cobrindo sucesso, avanço, última etapa, processor não encontrado, duplicidade (`CONCLUIDA`/`EM_EXECUCAO`/perdeu a corrida), retry e limite atingido.
  - _Requisitos: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 3.1, 3.2, 3.3, 4.1, 4.2, 4.3, 4.4, 5.1, 5.2, 5.3, 6.1, 6.2, 6.3, 6.4, 7.1, 7.2, 7.3, 7.4, 7.5, 8.3, 8.4, 10.2_

- [ ] 7. Implementar o adapter de persistência em `com.bradesco.orch.adapter.out.persistence`
  - Criar `OrquestracaoDocument` (`@Document("orquestracoes")`, `@Version Long version`, sub-documentos de etapa/controle/callback) e `OrquestracaoMongoMapper` (conversão domínio <-> documento).
  - Implementar `MongoOrquestracaoRepository implements OrquestracaoRepository`: `salvar`/`buscarPorId`/`atualizar` via Spring Data, e `transicionarEtapaParaEmExecucao` com update condicional atômico via `MongoTemplate` (filtro por `etapas.name` + `etapas.status = PENDENTE`, `set` para `EM_EXECUCAO`, `inc` de tentativas e `version`, `currentDate` em `dataAtualizacao`), retornando `true` apenas quando `modifiedCount == 1`.
  - Traduzir `OptimisticLockingFailureException`/conflito de concorrência em falha/duplicidade sem sobrescrever o estado.
  - Garantir que o `EtapaResponse` (`{ id }`) retornado pela etapa seja persistido no sub-documento `etapas[].callback.response`, sem manter connection string ou credenciais hardcoded (conexão a cargo do Spring a partir de `MONGODB_URI`/`MONGODB_DATABASE`).
  - _Requisitos: 1.1, 3.1, 3.2, 3.3, 8.1, 8.2, 8.3, 8.4, 9.2, 11.4, 12.3, 12.4_

- [ ] 8. Implementar o adapter de mensageria (publisher) em `com.bradesco.orch.adapter.out.messaging`
  - Criar `ServiceBusMensagemPublisher implements MensagemPublisher` usando `ServiceBusTemplate`/`StreamBridge` (Spring Cloud Azure), serializando e enviando apenas `{ orquestracaoId, etapa, correlationId }`.
  - Escrever teste unitário (Mockito) validando que somente os três campos são publicados (nunca o documento completo).
  - _Requisitos: 4.3, 4.4, 9.1, 9.3, 9.5_

- [ ] 9. Implementar os processors das etapas em `com.bradesco.orch.adapter.out.processor`
  - Criar os records imutáveis `EtapaRequest(String id)` (body enviado no `POST`: `{ "id": "<uuid>" }`) e `EtapaResponse(String id)` (body recebido: `{ "id": "<uuid>" }`).
  - Criar `MockEtapaHttpClient` (`@Component`) que faz `POST` no endpoint configurado (`orch.etapas.endpoint-base`) com body `{ id }` e desserializa a resposta `{ id }` em `EtapaResponse`, usando o bean `RestClient` `etapasRestClient` injetado (podendo reutilizar internamente `ApiStep<EtapaRequest,EtapaResponse>` + `Callback<EtapaResponse>` para a mecânica de sucesso/erro).
  - Criar `AbstractEtapaProcessor implements EtapaProcessor<EtapaRequest, EtapaResponse>` concentrando o comportamento comum: `execute(input)` monta o `EtapaRequest` e delega ao `MockEtapaHttpClient.chamar(...)`; `callback(response)` normaliza o `EtapaResponse` a ser persistido em `etapas[].callback.response`; mantendo apenas `name()` como método abstrato.
  - Criar os 4 processors concretos como `@Component` estendendo `AbstractEtapaProcessor`, cada um definindo somente `name()`: `OfertaProcessor` (`baas:etapa:cap-giro:oferta`), `ElegibilidadeProcessor` (`baas:etapa:cap-giro:elegibilidade`), `SimulacaoProcessor` (`baas:etapa:cap-giro:simulacao`) e `FormalizacaoProcessor` (`baas:etapa:cap-giro:formalizacao`).
  - Escrever testes unitários verificando o `name()` canônico de cada um dos 4 processors.
  - Escrever teste unitário do `MockEtapaHttpClient` (mock do `RestClient`/servidor) validando o `POST` do body `{ id }` no endpoint configurado e o mapeamento da resposta `{ id }` para `EtapaResponse`, garantindo que esse `EtapaResponse` é o resultado persistível em `etapas[].callback.response`.
  - _Requisitos: 2.3, 2.6, 3.1, 10.4, 11.1, 11.2, 11.3, 11.4, 11.5_

- [ ] 10. Implementar o registry de processors em `com.bradesco.orch.adapter.out.processor`
  - Criar `SpringEtapaProcessorRegistry implements EtapaProcessorRegistry`, recebendo `List<EtapaProcessor<?, ?>>` por injeção e indexando por `name()` em um `Map`.
  - Falhar na inicialização quando houver nomes duplicados (erro de configuração).
  - Escrever testes unitários cobrindo `localizar` por nome existente/inexistente e a falha na inicialização com nomes duplicados.
  - _Requisitos: 10.1, 10.2, 10.3, 10.4_

- [ ] 11. Implementar o adapter REST de entrada em `com.bradesco.orch.adapter.in.rest`
  - Criar os DTOs `IniciarOrquestracaoRequest` (com `toComando()`) e `IniciarOrquestracaoResponse` (`{ orquestracaoId }`).
  - Criar `CapGiroController` com `POST /cap-giro` delegando ao `IniciarOrquestracaoUseCase` e respondendo `202 Accepted` com `{ orquestracaoId }`, sem aguardar as etapas; propagar erro do use case como resposta de erro.
  - _Requisitos: 1.1, 1.5, 1.6, 9.3_

- [ ] 12. Implementar o listener de Service Bus em `com.bradesco.orch.adapter.in.messaging`
  - Criar `EtapaServiceBusListener` que consome a fila/tópico, desserializa a `MensagemEtapa`, monta o `ProcessarEtapaComando` e delega ao `ProcessarEtapaUseCase`.
  - Mapear o `ResultadoProcessamento` para a ação de mensageria correta: `SUCESSO`/`DUPLICIDADE`/`PROCESSOR_NAO_ENCONTRADO`/`ERRO_FINAL` -> ACK (com dead-letter em `ERRO_FINAL`); `RETENTAR` -> abandonar/relançar para reentrega do Service Bus.
  - Escrever teste unitário (Mockito) do mapeamento resultado -> ACK/retry/dead-letter.
  - _Requisitos: 2.1, 2.4, 6.2, 6.3, 6.4, 6.5, 7.3, 9.4_

- [ ] 13. Criar as classes de configuração em `com.bradesco.orch.config`
  - Criar `MongoConfig` habilitando apenas a auditoria (`dataCriacao`/`dataAtualizacao`) e o suporte a `@Version`, sem montar connection string manualmente nem embutir credenciais hardcoded (a conexão fica a cargo do Spring Boot a partir das propriedades externalizadas).
  - Criar `JacksonConfig` (`ObjectMapper` com JavaTime/`Instant` para mensagens e `callback.response`).
  - Criar `RestClientConfig` expondo o bean `etapasRestClient` com `baseUrl` = `${orch.etapas.endpoint-base}`, usado pelos processors via `MockEtapaHttpClient`, mantendo a URL do mock fora do código.
  - Criar `ServiceBusConfig` (binder de consumo/publicação, nomes de fila/tópico, política de retry/`maxDeliveryCount` e dead-letter).
  - Validar/referenciar o `application.properties` já existente no projeto (não recriá-lo), confirmando que `spring.data.mongodb.uri` usa `${MONGODB_URI:...}`, `spring.data.mongodb.database` usa `${MONGODB_DATABASE:baas-orch}` e `orch.etapas.endpoint-base` usa `${ETAPAS_ENDPOINT:...}`, todos externalizados por variável de ambiente e sem credenciais fixas no código-fonte Java.
  - _Requisitos: 3.2, 6.5, 9.1, 9.3, 11.1, 11.2, 12.1, 12.2, 12.3_

- [ ] 14. Fazer o wiring da aplicação e validar a montagem do contexto
  - Registrar/expor os beans (services de application, adapters out, registry, controller, listener, configs) e garantir a injeção correta das ports pelas implementações.
  - Escrever um teste de carregamento de contexto Spring que confirme a subida da aplicação com todos os beans resolvidos, incluindo o registry populado pelos quatro processors.
  - _Requisitos: 10.1, 10.3_

- [ ] 15. Escrever testes de integração do repositório MongoDB
  - Configurar Testcontainers MongoDB (ou Mongo embutido) e validar o `OrquestracaoMongoMapper`, o versionamento `@Version` e o `salvar`/`buscarPorId`/`atualizar`.
  - Testar o update condicional atômico de `transicionarEtapaParaEmExecucao`, incluindo execuções concorrentes sobre a mesma etapa em que apenas uma transição `PENDENTE`->`EM_EXECUCAO` vence e a persistência de `tentativas_realizadas` e do `EtapaResponse` (`{ id }`) em `etapas[].callback.response`.
  - _Requisitos: 3.1, 3.2, 3.3, 6.1, 7.5, 8.1, 8.2, 8.3, 8.4, 11.4, 12.4_

- [ ] 16. Escrever teste de contrato da API REST (Spring REST Docs + MockMvc)
  - Documentar/validar `POST /cap-giro` retornando `202 Accepted` com o corpo `{ orquestracaoId }`, com o use case mockado.
  - _Requisitos: 1.1, 1.5_
