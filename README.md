# Commerce API

API REST de e-commerce com multi-loja, catálogo público, gestão de estoque com reserva, ciclo de vida de pedidos e integração de pagamento por webhooks assinados.

Projeto de portfólio que demonstra modelagem de domínio, Spring Boot, Spring Security com JWT, JPA/Hibernate, tratamento de erros, documentação OpenAPI e testes.

**Stack:** Java 25 · Spring Boot 4 · Spring Data JPA · Spring Security · Flyway · PostgreSQL · Springdoc OpenAPI · JUnit 5 + Mockito + AssertJ · Gradle

---

## Funcionalidades

- **Autenticação e autorização** com JWT e 3 papéis: `CUSTOMER`, `STOREOWNER` e `ADMIN` (BCrypt + `@PreAuthorize`).
- **Catálogo público** com busca, filtros (nome, categoria, loja, faixa de preço) e paginação.
- **Multi-loja**: dono cria sua loja e gerencia produtos e estoque.
- **Estoque com reserva**: ao criar o pedido o estoque é reservado; no pagamento a reserva é confirmada; cancelar um pedido pendente libera a reserva e cancelar um pedido pago repõe o estoque físico.
- **Ciclo de vida do pedido**: `CREATED → PAID → SHIPPED → DELIVERED`, com cancelamento em `CREATED` e cancelamento com estorno em `PAID`.
- **Pagamento por webhook assinado** (HMAC-SHA256), com endpoint de simulação para testes locais.
- **Agenda de endereços** do cliente, com snapshot do endereço de entrega no pedido (value object embutido).
- **Documentação interativa** via Swagger UI.
- **235 testes** automatizados (unitários, de controller e de integração com Testcontainers).

---

## Como executar

Pré-requisitos: Docker (o JDK 25+ é necessário apenas para rodar a aplicação via Gradle).

Suba a stack completa (banco + aplicação em container), que sobe com o **perfil `prod`**:

```bash
docker compose up -d
```

A aplicação fica em `http://localhost:8080` (Swagger em `http://localhost:8080/swagger-ui.html`). Na primeira execução, o Flyway aplica as migrations e o schema é criado automaticamente. Em modo container **não** há seed de demonstração (perfil `prod`), e a aplicação só inicia com as variáveis `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION`, `PAYMENT_WEBHOOK_SECRET` e `PAYMENT_CHECKOUT_URL` definidas (fail-fast). Os valores no `docker-compose.yml` são de demonstração — troque antes de subir em ambiente real.

Para desenvolvimento (com hot reload), rode apenas o banco no Docker e a aplicação via Gradle (perfil dev, com seed):

```bash
docker compose up -d postgres
./gradlew bootRun
```

### Perfil de demonstração

No **perfil dev (padrão, via `gradlew bootRun`)** a aplicação cria de forma automática:

| Recurso | Credencial / conteúdo |
|---|---|
| Admin | `admin@admin.com` / `Admin123!` |
| Categorias | 12 categorias padrão (Eletrônicos, Roupas, ...) |

> **Aviso:** credenciais de demonstração são criadas pelo `AdminInitializer`. Em ambiente real, desative com `app.admin.auto-create=false` e configure senhas via variáveis de ambiente. No perfil `prod` (ex.: container do docker compose) os seeds não são criados.

### Pontos de entrada

| URL | Descrição |
|---|---|
| `http://localhost:8080/swagger-ui.html` | Swagger UI |
| `http://localhost:8080/v3/api-docs` | OpenAPI JSON |

---

## Perfis de execução

A aplicação tem dois perfis; o **padrão (dev)** é usado quando nenhum perfil é informado.

| Perfil | Quando usar | Banco de dados |
|---|---|---|
| `(padrão)` | Desenvolvimento local (`bootRun`) | PostgreSQL local em `localhost:5432` (credenciais em `application.yaml`) |
| `prod` | Produção / deploy | PostgreSQL via variáveis de ambiente (obrigatórias) |

### Como ativar um perfil

**1. Variável de ambiente** (padrão em deploys):

```bash
# Linux/macOS
SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun

# Windows (PowerShell)
$env:SPRING_PROFILES_ACTIVE="prod"; .\gradlew.bat bootRun
```

**2. Argumento de linha de comando** (tem precedência sobre a variável de ambiente):

```bash
./gradlew bootRun --args='--spring.profiles.active=prod'
```

**3. IntelliJ IDEA:** *Run/Debug Configurations* → *Environment variables*: `SPRING_PROFILES_ACTIVE=prod` (ou *VM options*: `-Dspring.profiles.active=prod`).

Para conferir o perfil ativo, veja a primeira linha do log de inicialização:

```
The following 1 profile is active: "prod"
```

> O arquivo do perfil **mescla** com o `application.yaml` base — ele sobrescreve apenas o que precisa. No `prod`, o datasource e os segredos vêm de env vars (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION`, `PAYMENT_WEBHOOK_SECRET`, `PAYMENT_CHECKOUT_URL`) e a aplicação **não inicia** se alguma estiver faltando (fail-fast proposital).

---

## Configuração

Todas as propriedades possuem padrões sensatos para ambiente local (`src/main/resources/application.yaml`) e podem ser sobrescritas por **variáveis de ambiente**:

| Propriedade | Padrão | Descrição |
|---|---|---|
| `JWT_SECRET` | (valor dev) | Segredo base64 da assinatura do JWT |
| `JWT_EXPIRATION` | `86400000` | Validade do token em ms |
| `PAYMENT_WEBHOOK_SECRET` | `webhook-secret-simulado` | Segredo usado na verificação da assinatura do webhook |
| `PAYMENT_CHECKOUT_URL` | `http://localhost:8080/checkout` | URL de checkout exposta no payment |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:5173` | Origens de frontend permitidas, separadas por vírgula |
| `APP_ADMIN_AUTO_CREATE` | `true` | Cria admin de demonstração |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/ecommerce` | Conexão do banco |

Exemplo:

```bash
export JWT_SECRET="sua-chave-base64-aqui"
export APP_ADMIN_AUTO_CREATE=false
./gradlew bootRun
```

---

## Visão geral da API

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/auth/register` | Público | Cadastro de `CUSTOMER`/`STOREOWNER` |
| POST | `/auth/login` | Público | Login → JWT |
| GET / PUT | `/users/me` | Autenticado | Ver/atualizar perfil |
| CRUD | `/users/me/addresses` | Autenticado | Agenda de endereços |
| GET | `/products` | Público | Catálogo (busca + filtros + paginação) |
| GET | `/products/{id}` | Público | Detalhes públicos do produto |
| POST | `/orders` | `CUSTOMER` | Criar pedido (reserva estoque) |
| GET | `/orders/{id}` | `CUSTOMER` | Detalhes do próprio pedido |
| PUT | `/orders/{id}/cancel` | `CUSTOMER` | Cancelar (libera reserva ou estorna pagamento) |
| GET | `/orders/me` | `CUSTOMER` | Meus pedidos |
| POST | `/payments` | `CUSTOMER` | Criar pagamento (PIX/CREDIT_CARD/BOLETO) |
| POST | `/payments/{id}/simulate-callback` | `CUSTOMER` | Simular resposta da gateway |
| GET | `/payments/{id}` | `CUSTOMER` | Status do pagamento |
| POST | `/webhooks/payments` | Público (assinado) | Evento da gateway de pagamento |
| CRUD | `/stores` | `STOREOWNER`/`ADMIN` | Gestão da loja |
| CRUD | `/stores/my/products` | `STOREOWNER` | Gestão de produtos |
| GET | `/stores/my/products/{id}/details` | `STOREOWNER` | Detalhes de um produto da própria loja |
| PUT | `/stores/my/products/{id}/stock/add` / `.../stock/remove` | `STOREOWNER` | Movimentação de estoque |
| GET / PUT | `/stores/my/orders` + `/ship` `/deliver` | `STOREOWNER` | Pedidos da loja |
| CRUD | `/admin/categories` | `ADMIN` | Gestão de categorias |
| PUT | `/admin/orders/{id}/confirm` `/ship` `/deliver` | `ADMIN` | Avançar ciclo do pedido |

### Exemplo de fluxo completo

```bash
# 1. Cadastrar um dono de loja
curl -s -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Loja do Zé","email":"loja@email.com","password":"Senha123","role":"STOREOWNER"}'

# 2. Login
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"loja@email.com","password":"Senha123"}' | jq -r .token)

# 3. Criar loja
curl -s -X POST http://localhost:8080/stores \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"Loja do Zé","email":"contato@loja.com"}'
```

O fluxo completo (criar loja → produto → cliente comprar → pagar → expedir) está documentado no Swagger UI.

---

## Como o pagamento (webhook) funciona

1. O `CUSTOMER` cria um pedido; o estoque é **reservado** e o pedido fica `CREATED`.
2. O `CUSTOMER` cria um pagamento (`PENDING`) vinculado ao pedido.
   - ao criar uma nova tentativa, pagamentos anteriores ainda `PENDING` para o mesmo pedido passam para `CANCELED`;
   - tentativas `FAILED`, `CANCELED` e `REFUNDED` permanecem registradas como histórico.
3. A "gateway" notifica o status **via webhook** em `POST /webhooks/payments`:
   - o corpo é assinado com **HMAC-SHA256** usando `PAYMENT_WEBHOOK_SECRET`;
   - a assinatura viaja no header `Stripe-Signature`; a API verifica com comparação em tempo constante (`MessageDigest.isEqual`) antes de processar;
   - o valor (`amount`) é conferido contra o valor do pagamento.
4. Evento `payment.succeeded` → pagamento `PAID` e pedido avança para `PAID` (reserva **confirmada** no estoque). Evento `payment.failed` → pagamento `FAILED` (pedido permanece pendente).
5. Para testar localmente sem gateway real, use `POST /payments/{id}/simulate-callback`.

Ao cancelar um pedido `CREATED`, somente a quantidade reservada por seus itens é liberada. Ao cancelar um pedido `PAID`, o pagamento passa para `REFUNDED`, a data do estorno é registrada e as unidades são devolvidas ao estoque físico. Pedidos `SHIPPED` ou `DELIVERED` não podem ser cancelados nesse fluxo.

O processamento é **idempotente**: reenvios de eventos já processados não alteram o estado.

---

## Decisões de arquitetura

- **Camadas controller → service → repository → entidade** com DTOs como barreira (nunca expõe entidades pela API). Requisição e resposta são records imutáveis.
- **Regra de negócio no domínio**, não no service: a máquina de estados do pedido (`Order.nextStatus()`, `canCancel()`), as operações de estoque (`Stock.reserve()/confirmReservation()/cancelReservation()`) e a normalização de slug de categoria vivem nas entidades.
- **Reserva de estoque**: o pedido cria a reserva; a confirmação só ocorre no pagamento, mantendo o estoque consistente entre criação e liquidação.
- **Snapshot de endereço**: o pedido grava um `ShippingAddress` (value object `@Embeddable`) copiado da agenda do cliente, para que mudanças futuras no endereço não alterem pedidos já criados.
- **Preço histórico**: `OrderItem` guarda `unitPrice` copiado no momento da compra.
- **Autorização por posse**: além do `@PreAuthorize` por role, as consultas são escopadas pelo dono (`findByIdAndStore_StoreOwnerId`, `findByCustomerIdAndId`) — um `STOREOWNER` nunca acessa dados de outra loja.
- **Documentação como contrato**: os contratos OpenAPI ficam em interfaces (`docs/controller/*Api`) implementadas pelos controllers, mantendo a camada HTTP enxuta.
- **Tratamento de erros global**: `@RestControllerAdvice` normaliza as respostas em `ErrorResponse` (status, mensagem, path, timestamp, lista de erros de validação).
- **Snake_case no banco** via nomes explícitos de coluna, mantendo o domínio em camelCase.

---

## Estrutura do projeto

```
src/main/java/br/com/matheus/commerceapi
├── controller/        # Endpoints (REST + @PreAuthorize)
│   └── admin/         # Operações administrativas
├── service/           # Orquestração das regras de negócio + transações
├── repository/        # Spring Data JPA
├── entity/            # Entidades + regras de domínio
├── domain/            # Value objects (Money, Email) e converters JPA
├── enums/             # OrderStatus, PaymentStatus, PaymentMethod, UserRole
├── dto/
│   ├── request/       # Entrada validada (@Valid)
│   └── response/      # Saída (records)
├── security/
│   ├── config/        # SecurityFilterChain, JWT, method security
│   ├── filter/        # JwtAuthenticationFilter
│   └── service/       # UserDetailsService, SecurityService
├── handler/           # GlobalExceptionHandler + ErrorResponse
├── exception/         # Hierarquia de exceções de negócio
├── docs/              # OpenAPI (Swagger) — contratos e configuração
├── initializer/       # Seed de admin e categorias
├── utils/             # Validação e assinatura HMAC
└── config/            # Beans de segurança (PasswordEncoder, AuthProvider)
```

---

## Testes

```bash
# Linux/macOS
./gradlew test

# Windows
.\gradlew.bat test
```

Os testes cobrem regras de negócio (estoque, pagamento, autenticação, produtos), serviços e controllers, com `@Nested`, testes parametrizados, AssertJ e verificação de interações do Mockito. Os testes de integração (`integration/`) executam o fluxo de ponta a ponta (pedido → webhook assinado → expedição) e as regras de autorização (401/403/posse) contra um PostgreSQL real via Testcontainers — **requer o Docker em execução**; os demais testes não dependem de banco.

---

## Limitações conhecidas (cenário de portfólio)

- **Banco de demonstração**: PostgreSQL local via `docker compose` (credenciais em `application.yaml`), com o schema versionado por Flyway. Os testes de integração usam Testcontainers e não dependem do banco local.
- **Concorrência de estoque**: a reserva atual não possui lock otimista/pessimista; em cenário concorrente a solução seria `@Version` ou lock pessimista / SQL atômico.
- **Expiração de reserva**: reservas de pedidos não pagos não expiram. Em produção, um job periódico deveria liberar reservas após um prazo.
- **Sem refresh token / revogação**: a autenticação é JWT stateless simples (padrão válido para o escopo).
- **Idempotência de criação**: `POST /orders` e `POST /payments` não são idempotentes por chave de idempotência (replays de cliente criariam duplicatas).

---

## Licença

MIT
