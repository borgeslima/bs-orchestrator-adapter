# Requisitos — Orquestrador de APIs baseado em etapas e Service Bus

## Introdução

Esta feature implementa um orquestrador de APIs assíncrono e orientado a eventos, composto por uma sequência ordenada de etapas. A execução transita obrigatoriamente pelo Azure Service Bus entre as etapas, garantindo que nenhum loop síncrono execute a orquestração de ponta a ponta e que o fluxo completo não permaneça em memória durante toda a execução.

O estado da orquestração e de suas etapas é mantido no MongoDB Atlas (máquina de estados), enquanto o Service Bus transporta os comandos/eventos de execução. Cada etapa é uma unidade independente, o que habilita retry, retomada, escalabilidade horizontal e recuperação após falhas.

A orquestração de referência desta POC é a `baas:cap-giro`, composta por exatamente 4 etapas, nesta ordem: **OFERTA**, **ELEGIBILIDADE**, **SIMULACAO** e **FORMALIZACAO**, com os nomes canônicos:

- `baas:etapa:cap-giro:oferta`
- `baas:etapa:cap-giro:elegibilidade`
- `baas:etapa:cap-giro:simulacao`
- `baas:etapa:cap-giro:formalizacao`

Ao executar, cada etapa chama uma API externa (mock). Nesta POC, as 4 etapas utilizam o **mesmo** endpoint mock: `POST https://free.mockerapi.com/mock/141caf88-d77d-4ac5-b2d1-1a232d7e3d09`, cujo request e response seguem o contrato `{ "id": "<uuid>" }`. O resultado retornado é persistido em `etapas[].callback.response`.

O sistema é construído em Java 21 + Spring Boot + Maven, no pacote base `com.bradesco.orch`, seguindo arquitetura hexagonal (Ports & Adapters). A persistência usa Spring Data MongoDB (cluster MongoDB Atlas) e a mensageria usa Spring Cloud Azure (Azure Service Bus). A connection string do MongoDB é externalizada via variável de ambiente `MONGODB_URI` (com a URI do cluster Atlas fornecida apenas como default de desenvolvimento) e o database default é `baas-orch`; a aplicação não deve manter credenciais fixas hardcoded em código-fonte. Já existem no domínio as abstrações iniciais `ApiStep` e `Callback` em `com.bradesco.orch.domain.entity`.

### Glossário

- **Orquestração**: documento raiz que representa uma execução ponta a ponta (ex.: `baas:cap-giro`), contendo uma lista ordenada de etapas. Na POC, a `baas:cap-giro` possui exatamente 4 etapas: OFERTA, ELEGIBILIDADE, SIMULACAO e FORMALIZACAO.
- **Etapa (Step)**: unidade de trabalho identificada por nome canônico (`baas:etapa:cap-giro:oferta`, `baas:etapa:cap-giro:elegibilidade`, `baas:etapa:cap-giro:simulacao`, `baas:etapa:cap-giro:formalizacao`), com estado e controle de tentativas próprios.
- **Orchestrator**: componente responsável por controlar a transição entre etapas.
- **Step Processor**: componente que executa a regra de negócio específica de uma etapa.
- **Registry de etapas**: mecanismo que localiza um processor pelo seu nome único.
- **Callback**: tratamento da resposta da etapa, cujo resultado é persistido em `etapas[].callback.response`.
- **API externa da etapa (mock)**: endpoint HTTP acionado por cada etapa durante a execução. Na POC, as 4 etapas usam o mesmo endpoint mock (`POST https://free.mockerapi.com/mock/141caf88-d77d-4ac5-b2d1-1a232d7e3d09`) com contrato de request e response `{ "id": "<uuid>" }`.
- **correlationId**: identificador que permite rastreamento ponta a ponta.

### Contratos de referência

Documento persistido no MongoDB:

```
{
  "_id": "<UUID>",
  "name": "baas:cap-giro",
  "order": <int>,
  "status": "PENDENTE | EM_EXECUCAO | CONCLUIDA | ERRO",
  "dataCriacao": "<timestamp>",
  "dataAtualizacao": "<timestamp>",
  "etapas": [
    {
      "name": "baas:etapa:cap-giro:oferta",
      "status": "AGUARDANDO | PENDENTE | EM_EXECUCAO | CONCLUIDA | ERRO",
      "controle": { "tentativas_realizadas": <int>, "limite_retentativas": <int> },
      "callback": { "response": <objeto> }
    }
  ]
}
```

Mensagem do Service Bus:

```
{ "orquestracaoId": "<UUID>", "etapa": "<nome-da-etapa>", "correlationId": "<id>" }
```

Interface das etapas:

```
interface EtapaProcessor<I, O> {
    String name();
    O execute(I input);
    void callback(O response);
}
```

Etapas concretas da orquestração `baas:cap-giro` (ordem fixa):

```
1. baas:etapa:cap-giro:oferta         (OFERTA)
2. baas:etapa:cap-giro:elegibilidade  (ELEGIBILIDADE)
3. baas:etapa:cap-giro:simulacao      (SIMULACAO)
4. baas:etapa:cap-giro:formalizacao   (FORMALIZACAO)
```

Contrato da API externa da etapa (mock, comum às 4 etapas nesta POC):

```
POST https://free.mockerapi.com/mock/141caf88-d77d-4ac5-b2d1-1a232d7e3d09

Request body:
{ "id": "<uuid>" }

Response body:
{ "id": "<uuid>" }
```

Configuração de persistência (MongoDB Atlas):

```
# Externalizado via variável de ambiente; sem credenciais hardcoded em código-fonte.
spring.data.mongodb.uri=${MONGODB_URI:<uri-atlas-somente-como-default-de-dev>}
spring.data.mongodb.database=${MONGODB_DATABASE:baas-orch}
```

### Estados

- **Orquestração**: `PENDENTE`, `EM_EXECUCAO`, `CONCLUIDA`, `ERRO`.
- **Etapa**: `AGUARDANDO`, `PENDENTE`, `EM_EXECUCAO`, `CONCLUIDA`, `ERRO`.
- Apenas a primeira etapa inicia como `PENDENTE`; as demais iniciam como `AGUARDANDO`.

---

## Requisitos

### Requisito 1 — Inicialização da orquestração (CA01)

**User story:** Como consumidor da API de início, quero disparar uma orquestração via requisição HTTP e receber uma confirmação imediata, para não precisar aguardar de forma síncrona a conclusão de todas as etapas.

**Critérios de aceite:**

1. WHEN a API de início recebe uma requisição `POST /cap-giro` válida THEN the system SHALL criar um documento de orquestração no MongoDB com `_id` (UUID), `name`, `order`, `status`, `dataCriacao`, `dataAtualizacao` e a lista de `etapas`.
2. WHEN o documento de orquestração é criado THEN the system SHALL definir o `status` da orquestração como `PENDENTE`.
3. WHEN as etapas da orquestração são persistidas THEN the system SHALL definir a primeira etapa como `PENDENTE` e todas as demais etapas como `AGUARDANDO`.
4. WHEN a orquestração e suas etapas são persistidas com sucesso THEN the system SHALL publicar uma mensagem no Service Bus apontando para a primeira etapa (ex.: `baas:etapa:cap-giro:oferta`) contendo `orquestracaoId`, `etapa` e `correlationId`.
5. WHEN a mensagem de início é publicada com sucesso THEN the system SHALL retornar `202 Accepted` com o corpo `{ orquestracaoId }` sem aguardar a conclusão das etapas.
6. IF a criação da orquestração ou a persistência no MongoDB falhar THEN the system SHALL não publicar a mensagem de início e SHALL retornar um erro ao consumidor.

---

### Requisito 2 — Acionamento e execução de etapa (CA02)

**User story:** Como orquestrador, quero que cada etapa seja acionada por uma mensagem do Service Bus e execute sua lógica de forma isolada, para que o fluxo permaneça orientado a eventos e sem loop síncrono.

**Critérios de aceite:**

1. WHEN o consumer recebe uma mensagem do Service Bus THEN the system SHALL usar o `orquestracaoId` para recuperar o documento de orquestração no MongoDB.
2. WHEN o documento de orquestração é recuperado THEN the system SHALL localizar a etapa correspondente ao campo `etapa` da mensagem.
3. WHEN a etapa é localizada THEN the system SHALL localizar, no registry de etapas, o processor registrado pelo nome canônico recebido (`baas:etapa:cap-giro:oferta`, `baas:etapa:cap-giro:elegibilidade`, `baas:etapa:cap-giro:simulacao`, `baas:etapa:cap-giro:formalizacao`).
4. IF nenhum processor estiver registrado para o nome da etapa recebida THEN the system SHALL tratar como erro de processamento e SHALL não avançar o fluxo.
5. WHEN o estado da etapa permite execução THEN the system SHALL incrementar `controle.tentativas_realizadas` antes de executar o processor.
6. WHEN o processor é executado THEN the system SHALL invocar `execute(input)`, que SHALL fazer `POST` no endpoint mock configurado com body `{ "id": "<uuid>" }`, e, em seguida, processar o resultado por meio de `callback(response)`.
7. WHILE a etapa está sendo processada THEN the system SHALL manter o estado no MongoDB e SHALL não executar as demais etapas em um loop síncrono na mesma execução.

---

### Requisito 3 — Persistência do resultado da etapa (CA07)

**User story:** Como operador da plataforma, quero que o resultado de cada etapa seja persistido, para auditar e rastrear a execução da orquestração.

**Critérios de aceite:**

1. WHEN o processor de uma etapa conclui com sucesso THEN the system SHALL persistir o resultado em `etapas[].callback.response` no documento da orquestração no MongoDB.
2. WHEN o estado de uma etapa ou da orquestração é alterado THEN the system SHALL atualizar o campo `dataAtualizacao` da orquestração.
3. WHEN uma etapa é executada THEN the system SHALL persistir o valor atualizado de `controle.tentativas_realizadas`.

---

### Requisito 4 — Transição para a próxima etapa via Service Bus (CA03)

**User story:** Como arquiteto da solução, quero que o avanço entre etapas ocorra estritamente de forma assíncrona pelo Service Bus, para garantir desacoplamento, escalabilidade horizontal e retomada após falhas.

**Critérios de aceite:**

1. WHEN uma etapa é concluída com sucesso THEN the system SHALL definir o `status` da etapa atual como `CONCLUIDA`.
2. WHEN a etapa atual é concluída e existe uma próxima etapa THEN the system SHALL definir o `status` da próxima etapa como `PENDENTE`.
3. WHEN a próxima etapa é definida como `PENDENTE` THEN the system SHALL publicar uma nova mensagem no Service Bus apontando para essa próxima etapa, contendo `orquestracaoId`, `etapa` e `correlationId`.
4. IF uma etapa precisa avançar para a próxima THEN the system SHALL fazê-lo exclusivamente publicando no Service Bus e SHALL nunca chamar diretamente a próxima etapa.
5. WHEN uma próxima etapa em `AGUARDANDO` tem sua etapa anterior concluída THEN the system SHALL transicioná-la para `PENDENTE`.
6. WHEN uma etapa recebe sua mensagem e passa a ser processada THEN the system SHALL transicioná-la de `PENDENTE` para `EM_EXECUCAO`.

---

### Requisito 5 — Finalização da orquestração (CA04)

**User story:** Como consumidor da orquestração, quero que a orquestração seja marcada como concluída quando a última etapa finaliza, para saber que o fluxo terminou com sucesso.

**Critérios de aceite:**

1. WHEN uma etapa é concluída e não existe próxima etapa THEN the system SHALL definir o `status` da última etapa como `CONCLUIDA`.
2. WHEN a última etapa é concluída THEN the system SHALL definir o `status` da orquestração como `CONCLUIDA`.
3. WHEN a orquestração é finalizada THEN the system SHALL não publicar novas mensagens no Service Bus para essa orquestração.

---

### Requisito 6 — Retry e controle de tentativas (CA05)

**User story:** Como responsável pela confiabilidade, quero que cada etapa controle suas próprias tentativas em conjunto com a política de retry do Service Bus, para reprocessar falhas transitórias sem ultrapassar o limite definido pela aplicação.

**Critérios de aceite:**

1. WHEN uma etapa é processada THEN the system SHALL incrementar `controle.tentativas_realizadas` a cada tentativa.
2. WHILE `controle.tentativas_realizadas < controle.limite_retentativas` THEN the system SHALL permitir o reprocessamento da etapa.
3. WHEN a execução de uma etapa falha e o limite de retentativas ainda não foi atingido THEN the system SHALL permitir uma nova tentativa de processamento.
4. WHEN `controle.tentativas_realizadas` atinge `controle.limite_retentativas` e a etapa continua falhando THEN the system SHALL definir o `status` da etapa como `ERRO` e o `status` da orquestração como `ERRO`.
5. WHEN a política de retry do Service Bus é acionada THEN the system SHALL atuar em conjunto com o controle de tentativas no MongoDB para não ultrapassar o limite definido pela aplicação.

---

### Requisito 7 — Idempotência do processamento (CA06)

**User story:** Como operador da mensageria, quero que o processamento de etapas seja idempotente, para que a entrega repetida da mesma mensagem não cause execução duplicada.

**Critérios de aceite:**

1. WHEN uma mensagem é recebida THEN the system SHALL validar o estado atual da etapa no MongoDB antes de processá-la.
2. IF o estado da etapa é `CONCLUIDA` THEN the system SHALL não executar a etapa novamente.
3. IF o estado da etapa é `EM_EXECUCAO` THEN the system SHALL tratar a mensagem como duplicidade e SHALL não iniciar um novo processamento.
4. IF o estado da etapa é `PENDENTE` THEN the system SHALL executar a etapa.
5. WHEN a mesma mensagem chega mais de uma vez THEN the system SHALL garantir que o resultado final permaneça consistente com uma única execução bem-sucedida.

---

### Requisito 8 — Concorrência e transição atômica de estado

**User story:** Como arquiteto da solução, quero controle de concorrência sobre o documento da orquestração, para evitar processamento duplicado simultâneo quando múltiplas instâncias consomem a mesma mensagem.

**Critérios de aceite:**

1. WHEN o documento de orquestração é atualizado THEN the system SHALL aplicar controle de concorrência (optimistic locking / versionamento / update condicional no MongoDB).
2. WHEN uma etapa transiciona de `PENDENTE` para `EM_EXECUCAO` THEN the system SHALL realizar a transição de forma atômica.
3. IF duas execuções concorrentes tentam transicionar a mesma etapa de `PENDENTE` para `EM_EXECUCAO` THEN the system SHALL permitir que apenas uma execução prossiga e SHALL impedir o processamento duplo simultâneo.
4. IF uma atualização condicional do documento falhar por conflito de concorrência THEN the system SHALL não sobrescrever o estado atual e SHALL tratar a operação como duplicidade.

---

### Requisito 9 — Regra fundamental de transporte e estado

**User story:** Como arquiteto da solução, quero que toda transição entre etapas ocorra pelo Service Bus e que o estado viva no MongoDB, para preservar o modelo orientado a eventos e a recuperação após falhas.

**Critérios de aceite:**

1. WHEN ocorre qualquer transição entre etapas THEN the system SHALL realizá-la por meio do Service Bus.
2. WHEN o estado da orquestração ou de uma etapa muda THEN the system SHALL persistir esse estado no MongoDB como fonte da verdade.
3. WHEN uma mensagem é publicada no Service Bus THEN the system SHALL incluir apenas `orquestracaoId`, `etapa` e `correlationId` e SHALL não incluir o documento completo da orquestração.
4. WHEN o consumer processa uma etapa THEN the system SHALL recuperar o estado necessário a partir do MongoDB usando o `orquestracaoId`.
5. WHILE a orquestração está em andamento THEN the system SHALL não manter o fluxo completo em memória durante toda a execução.

---

### Requisito 10 — Registry de etapas (Step Processors)

**User story:** Como desenvolvedor de etapas, quero registrar processors por nome único, para que o consumer localize dinamicamente a implementação correspondente à mensagem recebida.

**Critérios de aceite:**

1. WHEN a aplicação inicializa THEN the system SHALL registrar cada implementação de processor pelo seu nome único retornado por `name()`.
2. WHEN o consumer recebe o campo `etapa` de uma mensagem THEN the system SHALL usar esse nome para localizar o processor correspondente no registry.
3. IF dois processors declararem o mesmo nome THEN the system SHALL sinalizar erro de configuração na inicialização.
4. WHEN um processor é localizado THEN the system SHALL executá-lo respeitando a interface `EtapaProcessor<I, O>` (`name()`, `execute(input)`, `callback(response)`).

---

### Requisito 11 — Integração com API externa da etapa (mock)

**User story:** Como orquestrador, quero que cada etapa acione uma API externa (mock) ao executar, para que o resultado da chamada seja usado como resultado da etapa e persistido para auditoria.

**Critérios de aceite:**

1. WHEN uma etapa executa (`execute(input)`) THEN the system SHALL fazer um `POST` no endpoint mock configurado (`orch.etapas.endpoint-base`, default `https://free.mockerapi.com/mock/141caf88-d77d-4ac5-b2d1-1a232d7e3d09`) com o body `{ "id": "<uuid>" }`.
2. WHEN as 4 etapas (`oferta`, `elegibilidade`, `simulacao`, `formalizacao`) executam nesta POC THEN the system SHALL utilizar o mesmo endpoint mock configurado para todas elas.
3. WHEN a API externa responde com sucesso THEN the system SHALL usar a resposta `{ "id": "<uuid>" }` como resultado (`response`) da etapa.
4. WHEN o resultado da etapa é obtido THEN the system SHALL persistir esse resultado em `etapas[].callback.response` no documento da orquestração no MongoDB.
5. WHEN request e response são trocados com a API externa THEN the system SHALL respeitar o contrato `{ "id": "<uuid>" }` tanto no envio quanto no recebimento.
6. IF a chamada à API externa falhar THEN the system SHALL tratar como falha de execução da etapa, respeitando o controle de tentativas e a política de retry definidos no Requisito 6.

---

### Requisito 12 — Configuração e persistência (MongoDB)

**User story:** Como responsável pela segurança e operação, quero que a connection string do MongoDB seja externalizada e que o estado seja persistido no MongoDB, para evitar credenciais em código-fonte e manter uma fonte da verdade confiável.

**Critérios de aceite:**

1. WHEN a aplicação inicializa THEN the system SHALL obter a connection string do MongoDB a partir da variável de ambiente `MONGODB_URI` (via `application.properties`), utilizando a URI do cluster Atlas apenas como default de desenvolvimento.
2. WHEN a aplicação inicializa THEN the system SHALL utilizar o database configurado, com default `baas-orch` (variável `MONGODB_DATABASE`).
3. WHEN o código-fonte é versionado THEN the system SHALL não conter credenciais fixas hardcoded, mantendo a URI e as credenciais externalizadas em configuração/variável de ambiente.
4. WHEN o estado da orquestração ou de uma etapa muda THEN the system SHALL persistir esse estado no MongoDB como fonte da verdade, conforme já definido no Requisito 9.
