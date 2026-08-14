# E-commerce — API

API REST em Java + Spring Boot para um e-commerce simples de loja única
(sem multi-tenant): catálogo de produtos com categorias, promoções e
destaques, carrinho persistente por sessão, avaliações anônimas de
produto e painel administrativo protegido por JWT. Consumida pelo
frontend Angular em [`e-commerce-frontend`](../e-commerce-frontend). Veja
`roadmap.md` na raiz do projeto para o escopo completo e o histórico de
decisões de arquitetura.

**Não há checkout/pedido processado pelo backend.** A compra é finalizada
fora da aplicação, por um botão que abre o WhatsApp com os itens e o
total (número da loja configurado pelo admin) — as entidades `Order`/
`OrderItem` existiram até 2026-08-10 e foram removidas por completo.

Stack: Java 21, Spring Boot 4 (Web, Data JPA, Security, Validation),
Jackson 3 (pacote `tools.jackson`, não `com.fasterxml.jackson`), PostgreSQL,
Flyway, JWT (`io.jsonwebtoken:jjwt`), Lombok.

## Pré-requisitos

- JDK 21+
- Docker (Postgres local via `docker compose`, e Testcontainers para os testes)
- Maven não precisa estar instalado — use o wrapper (`./mvnw`)

## Rodando localmente

```bash
docker compose up -d              # sobe o Postgres (usa .env, ver .env.example)
cp .env.example .env               # ajuste se necessário
set -a && source .env && set +a    # bash; no PowerShell, defina as variáveis manualmente
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. As migrations Flyway rodam
automaticamente e criam o usuário admin de desenvolvimento:

- **E-mail:** `admin@ecommerce.com`
- **Senha:** `admin123`

CORS liberado por padrão para `http://localhost:4200` (origem default do
Angular dev server), configurável via `CORS_ALLOWED_ORIGINS`.

## Testes

```bash
./mvnw test
```

Testes de integração (`@SpringBootTest` + Testcontainers) sobem um
Postgres real via Docker e aplicam as migrations de verdade — **o Docker
precisa estar rodando** para `./mvnw test` funcionar. Um único container
Postgres é reaproveitado por toda a suíte (ver `AbstractIntegrationTest`);
cada teste roda em uma transação própria, desfeita ao final.

## Autenticação

- `POST /api/auth/login` — body `{ "email": "...", "password": "..." }`,
  retorna `{ "token": "<jwt>" }`.
- Enviar `Authorization: Bearer <token>` nas rotas protegidas.
- Rotas protegidas (role `ADMIN`): escrita de categorias/produtos, upload
  e gerenciamento de imagens de produto, `/api/admin/**`,
  `DELETE /api/reviews/{id}`, `PUT /api/settings`.

## Carrinho: header obrigatório

Todas as rotas de `/api/cart/**` exigem o header:

```
X-Session-Id: <uuid gerado no frontend, persistido em localStorage>
```

O carrinho é criado automaticamente no primeiro `GET /api/cart` com um
`sessionId` novo.

## Endpoints

### Categorias

| Método | Rota                   | Acesso  | Descrição                                                   |
| ------ | ---------------------- | ------- | ------------------------------------------------------------ |
| GET    | `/api/categories`      | público | lista todas                                                  |
| POST   | `/api/categories`      | admin   | cria categoria                                                |
| PUT    | `/api/categories/{id}` | admin   | edita                                                          |
| DELETE | `/api/categories/{id}` | admin   | remove; produtos vinculados são reatribuídos à categoria padrão ("Geral") em vez de bloquear |

A categoria padrão (`isDefault=true`, seed "Geral") nunca pode ser removida.

### Produtos

| Método | Rota                             | Acesso  | Descrição                                                             |
| ------ | -------------------------------- | ------- | ----------------------------------------------------------------------- |
| GET    | `/api/products`                  | público | lista produtos ativos; filtros `?category=&name=&onSale=&featured=`     |
| GET    | `/api/products/{id}`             | público | detalhe (404 se inativo)                                                 |
| GET    | `/api/products/slug/{slug}`      | público | detalhe por slug (URL amigável, gerado automaticamente)                  |
| POST   | `/api/products`                  | admin   | cria produto                                                             |
| PUT    | `/api/products/{id}`             | admin   | edita produto                                                            |
| DELETE | `/api/products/{id}`             | admin   | soft delete (`active = false`)                                           |
| PUT    | `/api/products/{id}/reactivate`  | admin   | reverte o soft delete                                                    |
| GET    | `/api/admin/products`            | admin   | lista todos, inclusive inativos                                         |
| POST   | `/api/products/upload-image`     | admin   | multipart `file`, retorna `{ "imageUrl": "/uploads/xxx.png" }`           |
| POST   | `/api/products/{id}/images`      | admin   | adiciona foto à galeria do produto (máx. 3 fotos extras)                 |
| DELETE | `/api/products/{id}/images/{imageId}` | admin | remove foto da galeria                                              |

Catálogo sem paginação no endpoint — a API sempre retorna a lista
filtrada completa (paginação, quando existe, é feita client-side); ver
seção 4.4 do `roadmap.md` para a justificativa.

### Avaliações (reviews)

Anônimas — o projeto não tem login de cliente, só nome livre + nota + comentário opcional.

| Método | Rota                                 | Acesso  | Descrição                                                        |
| ------ | ------------------------------------ | ------- | -------------------------------------------------------------------- |
| GET    | `/api/products/{productId}/reviews`  | público | pagina (`?page=&size=`, máx. 50) e ordena (`?sort=recent\|highest\|lowest`) |
| POST   | `/api/products/{productId}/reviews`  | público | cria avaliação; 409 se o mesmo IP já avaliou o mesmo produto           |
| DELETE | `/api/reviews/{id}`                  | admin   | remove (única moderação disponível)                                    |

`averageRating`/`reviewCount` vêm agregados em `ProductResponse`.

### Carrinho

| Método | Rota                       | Acesso  | Descrição                                            |
| ------ | -------------------------- | ------- | ----------------------------------------------------- |
| GET    | `/api/cart`                | público | cria carrinho se `sessionId` não existir              |
| POST   | `/api/cart/items`          | público | body `{ productId, quantity }`                        |
| PUT    | `/api/cart/items/{itemId}` | público | body `{ quantity }`                                    |
| DELETE | `/api/cart/items/{itemId}` | público | remove item                                            |
| DELETE | `/api/cart`                | público | limpa carrinho inteiro                                 |

### Configurações da loja

| Método | Rota            | Acesso  | Descrição                                                             |
| ------ | ---------------- | ------- | ------------------------------------------------------------------------ |
| GET    | `/api/settings`  | público | `{ "whatsappNumber": string \| null }`                                    |
| PUT    | `/api/settings`  | admin   | body `{ "whatsappNumber": "5511999999999" }` (só dígitos, DDI+DDD+número) |

Registro singleton (`store_settings`, id fixo = 1). `whatsappNumber`
começa `null` até o admin configurar em `/admin/configuracoes` no frontend.

## Formato de erro padrão

```json
{
  "timestamp": "2026-08-06T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Estoque insuficiente para o produto X",
  "path": "/api/cart/items"
}
```

## Regras de negócio importantes

- Estoque é validado ao adicionar/atualizar item no carrinho.
- Produtos inativos (soft delete) não aparecem em listagem/detalhe público.
- `onSale=true` exige `discountPrice < price`; `getEffectivePrice()` é a
  fonte usada pelo carrinho (nunca recalcular a partir de `price` direto).
- Adicionar ao carrinho um produto já presente soma a quantidade ao item
  existente em vez de duplicar.
- Avaliação duplicada (mesmo IP + mesmo produto) é bloqueada por um índice
  único no banco, não só checada na aplicação — cobre corrida entre
  requisições simultâneas.

## Estrutura do projeto

```
src/main/java/.../e_commerce_api
├── config/            # SecurityConfig
├── auth/               # login, JwtUtil, JwtFilter
├── category/
├── product/            # produto, galeria de imagens, upload
├── cart/
├── review/             # avaliações anônimas
├── settings/           # número de WhatsApp da loja
└── exception/          # GlobalExceptionHandler, exceções customizadas
```

Convenção de camadas: Controller → Service → Repository. Entities nunca
são retornadas direto — sempre DTOs de Request/Response.

## Deploy

Ainda não configurado (adiado a pedido do usuário) — ver Fase 8.5 do
`roadmap.md`. CORS de produção (restringir origem) também depende disso.
