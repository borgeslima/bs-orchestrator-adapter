# Requisitos — Configurar o Swagger (OpenAPI) do projeto

## Introdução

Esta feature adiciona documentação interativa de API ao serviço **orch-poc** por meio da especificação **OpenAPI 3** e da interface **Swagger UI**. O objetivo é permitir que desenvolvedores, times integradores e QA explorem e testem os endpoints REST existentes diretamente pelo navegador, além de disponibilizar o contrato da API em formato JSON para consumo por ferramentas (geração de clients, validação de contrato, importação em API Gateways/Postman).

O projeto utiliza **Java 21**, **Spring Boot 4.0.x** (parent `4.0.9-SNAPSHOT`), **Spring 7**, **Maven**, pacote base `com.bradesco.orch` e segue **Arquitetura Hexagonal (Ports & Adapters)**. A aplicação sobe na porta **8081** (`server.port`), com persistência em **MongoDB** e mensageria via **Azure Service Bus**.

A API REST já existe no adapter de entrada `com.bradesco.orch.adapter.in.rest`, exposta pelo `CapGiroController` (base path `/cap-giro`):

- `POST /cap-giro` — inicia a orquestração de forma assíncrona. Corpo opcional `IniciarOrquestracaoRequest { correlationId? }`. Resposta **202 Accepted** com `IniciarOrquestracaoResponse { orquestracaoId }`.
- `GET /cap-giro/{orquestracaoId}` — retorna `OrquestracaoResponse` (dados + etapas) com **200 OK**, ou **404 Not Found** se a orquestração não existir.
- `GET /cap-giro/{orquestracaoId}/etapas` — retorna a lista de `OrquestracaoResponse.EtapaResponseView` com **200 OK**, ou **404 Not Found** se a orquestração não existir.

A biblioteca padrão para OpenAPI em Spring Boot 3+ é o **springdoc-openapi** (starter `springdoc-openapi-starter-webmvc-ui`), que expõe a Swagger UI e o documento `/v3/api-docs`. **Atenção:** versões antigas do springdoc não inicializam sob Spring Boot 4 / Spring 7; portanto, a versão adotada deve ser explicitamente compatível com essa stack.

Restrição transversal: a implementação **não pode violar a Arquitetura Hexagonal** nem exigir alterações na camada de domínio (`com.bradesco.orch.domain`). Anotações OpenAPI, quando necessárias, devem residir apenas nos adapters de entrada REST e seus DTOs em `adapter/in/rest`.

## Requisitos

### Requisito 1 — Disponibilizar a Swagger UI no navegador

**User Story:** Como desenvolvedor integrador, quero acessar uma interface web do Swagger, para explorar e testar os endpoints da API sem precisar de ferramentas externas.

#### Critérios de Aceite

1. WHEN a aplicação estiver em execução e um usuário acessar a rota da Swagger UI pelo navegador THEN o sistema SHALL renderizar a interface Swagger UI com a lista de endpoints da API.
2. WHEN a Swagger UI for carregada THEN o sistema SHALL disponibilizar a rota padrão `/swagger-ui.html` e/ou `/swagger-ui/index.html` como ponto de acesso.
3. WHEN um usuário executar uma requisição de teste ("Try it out") pela Swagger UI THEN o sistema SHALL enviar a chamada real ao endpoint correspondente e exibir a resposta HTTP (status e corpo).
4. IF a Swagger UI estiver habilitada THEN o sistema SHALL servi-la a partir da própria aplicação na porta configurada (`server.port`, padrão 8081) sem exigir serviço externo adicional.

### Requisito 2 — Expor o documento OpenAPI em JSON

**User Story:** Como consumidor de ferramentas de API, quero obter o contrato OpenAPI em JSON, para gerar clients, validar contratos e importar a definição em outras ferramentas.

#### Critérios de Aceite

1. WHEN a aplicação estiver em execução e um cliente requisitar a rota do documento OpenAPI THEN o sistema SHALL retornar o documento OpenAPI 3 em formato JSON com **200 OK**.
2. WHEN o documento OpenAPI for solicitado THEN o sistema SHALL disponibilizá-lo na rota padrão `/v3/api-docs`.
3. WHEN o documento OpenAPI for gerado THEN o sistema SHALL descrever todos os endpoints REST atualmente expostos pela aplicação, seus métodos HTTP, parâmetros, corpos de requisição/resposta e códigos de status.
4. WHERE o consumidor solicitar o formato YAML (rota `/v3/api-docs.yaml`) THEN o sistema SHALL retornar o mesmo contrato em YAML equivalente.

### Requisito 3 — Documentar automaticamente os endpoints do CapGiroController

**User Story:** Como desenvolvedor, quero que os endpoints já existentes do `CapGiroController` sejam documentados automaticamente, para que o contrato reflita fielmente a API sem manutenção manual duplicada.

#### Critérios de Aceite

1. WHEN o documento OpenAPI for gerado THEN o sistema SHALL incluir o endpoint `POST /cap-giro` com corpo de requisição opcional `IniciarOrquestracaoRequest { correlationId }` e resposta **202 Accepted** com o schema `IniciarOrquestracaoResponse { orquestracaoId }`.
2. WHEN o documento OpenAPI for gerado THEN o sistema SHALL incluir o endpoint `GET /cap-giro/{orquestracaoId}` com o parâmetro de caminho `orquestracaoId`, resposta **200 OK** com o schema `OrquestracaoResponse` (incluindo a lista de `EtapaResponseView`) e resposta **404 Not Found**.
3. WHEN o documento OpenAPI for gerado THEN o sistema SHALL incluir o endpoint `GET /cap-giro/{orquestracaoId}/etapas` com o parâmetro de caminho `orquestracaoId`, resposta **200 OK** com uma lista do schema `EtapaResponseView` e resposta **404 Not Found**.
4. WHEN os schemas de request/response forem gerados THEN o sistema SHALL refletir os campos reais dos DTOs (`IniciarOrquestracaoRequest`, `IniciarOrquestracaoResponse`, `OrquestracaoResponse` e `OrquestracaoResponse.EtapaResponseView`) sem expor as entidades de domínio.
5. IF um novo endpoint REST for adicionado a um adapter de entrada THEN o sistema SHALL passar a documentá-lo automaticamente na próxima geração do documento, sem configuração manual por endpoint.

### Requisito 4 — Metadados configuráveis da API

**User Story:** Como responsável técnico pela API, quero definir título, descrição, versão e contato da API, para que a documentação apresente informações institucionais corretas.

#### Critérios de Aceite

1. WHEN o documento OpenAPI for gerado THEN o sistema SHALL apresentar título, descrição e versão da API definidos pela configuração do projeto.
2. WHEN os metadados forem configurados THEN o sistema SHALL incluir informações de contato (por exemplo, nome do time e e-mail) na seção `info` do documento OpenAPI.
3. IF nenhum metadado personalizado for fornecido THEN o sistema SHALL aplicar valores padrão coerentes com o projeto (por exemplo, título "orch-poc API" e uma versão padrão) sem falhar a inicialização.
4. WHERE os metadados forem definidos THEN o sistema SHALL permitir sua alteração sem exigir mudanças na camada de domínio.

### Requisito 5 — Compatibilidade com Spring Boot 4 / Spring 7

**User Story:** Como desenvolvedor da plataforma, quero que a dependência de OpenAPI seja compatível com Spring Boot 4 e Spring 7, para que a aplicação inicialize corretamente e a documentação funcione na stack atual.

#### Critérios de Aceite

1. WHEN a dependência de OpenAPI (springdoc-openapi) for adicionada ao `pom.xml` THEN o sistema SHALL utilizar uma versão explicitamente compatível com Spring Boot 4.0.x / Spring 7.
2. WHEN a aplicação for iniciada com a dependência incluída THEN o sistema SHALL inicializar sem erros de compatibilidade (por exemplo, falhas de contexto ou `NoSuchMethodError`/`ClassNotFoundException` relacionados ao springdoc).
3. WHEN a stack tecnológica (Java 21, Spring Boot 4.0.x, Spring 7, Maven) estiver em uso THEN o sistema SHALL manter a Swagger UI e o endpoint `/v3/api-docs` operacionais.
4. IF a versão do springdoc utilizada não suportar a stack THEN o sistema SHALL ter a versão ajustada para uma release compatível, evitando o uso de versões destinadas apenas a Spring Boot 2/3.

### Requisito 6 — Configuração externalizável e habilitação por ambiente

**User Story:** Como operador de ambientes, quero controlar caminhos e a ativação do Swagger por ambiente, para expor a documentação em ambientes de desenvolvimento e desabilitá-la onde não for desejada.

#### Critérios de Aceite

1. WHEN os caminhos da Swagger UI e do documento OpenAPI precisarem ser ajustados THEN o sistema SHALL permitir sua configuração via `application.properties` (por exemplo, `springdoc.swagger-ui.path` e `springdoc.api-docs.path`).
2. WHEN a propriedade de habilitação estiver definida como falsa para um ambiente THEN o sistema SHALL desabilitar a Swagger UI e/ou o endpoint `/v3/api-docs` nesse ambiente (por exemplo, `springdoc.swagger-ui.enabled` e `springdoc.api-docs.enabled`).
3. IF nenhuma configuração de caminho for fornecida THEN o sistema SHALL utilizar os caminhos padrão (`/swagger-ui.html` e `/v3/api-docs`).
4. WHERE perfis do Spring (`spring.profiles.active`) forem utilizados THEN o sistema SHALL permitir configurações distintas de habilitação por perfil/ambiente sem recompilação.

### Requisito 7 — Preservar a Arquitetura Hexagonal e o domínio

**User Story:** Como arquiteto de software, quero que a documentação OpenAPI não acople o domínio a frameworks de documentação, para preservar o isolamento do domínio na Arquitetura Hexagonal.

#### Critérios de Aceite

1. WHEN a documentação OpenAPI for configurada THEN o sistema SHALL manter a camada de domínio (`com.bradesco.orch.domain`) livre de dependências e anotações do springdoc/OpenAPI.
2. IF anotações OpenAPI (por exemplo, `@Operation`, `@Schema`, `@Tag`) forem utilizadas THEN o sistema SHALL restringi-las aos adapters de entrada REST e seus DTOs em `com.bradesco.orch.adapter.in.rest`.
3. WHEN a configuração de metadados/documentação for adicionada THEN o sistema SHALL posicioná-la em uma classe de configuração no adapter/infra (por exemplo, `adapter.in.rest` ou pacote de configuração), sem introduzir dependências de framework web na camada de domínio.
4. WHEN a feature for concluída THEN o sistema SHALL preservar a direção de dependências da Arquitetura Hexagonal (adapters dependem do domínio, nunca o contrário).
