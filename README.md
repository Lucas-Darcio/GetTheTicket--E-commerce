# GetTheTicket

Marketplace de ingressos para eventos: projeto de portfólio fullstack combinando Java/Spring Boot, React/Next.js, Docker, CI/CD e Cloud.

## Stack

- **Backend:** Spring Boot 4.1.0 (Java 21), Spring Data JPA, Spring Security, Flyway, PostgreSQL
- **Frontend:** Next.js
- **Infraestrutura local:** Docker Compose (Postgres, Adminer, backend, frontend)
- **Autenticação:** JWT (stateless), BCrypt para hash de senha

## Rodando o projeto

Pré-requisitos: Docker e Docker Compose.

1. Copie `.env.example` para `.env` e preencha os valores (`DB_USER`, `DB_PASSWORD`, `JWT_SECRET`)
2. `docker compose up --build`
3. Backend: `http://localhost:8080`
4. Frontend: `http://localhost:3000`
5. Adminer (interface do banco): `http://localhost:8081` - System: PostgreSQL, Server: `postgres`, demais campos conforme `.env`

## Estrutura do repositório

```
/backend    - API Spring Boot
/frontend   - aplicação Next.js
docker-compose.yml
```

## Modelo de dados

Entidades planejadas: `Usuario`, `Evento`, `LoteIngresso`, `Reserva`, `Pedido`, `Pagamento`.
Implementado até agora: `User` (tabela `users`).

Migrations gerenciadas via Flyway em `backend/src/main/resources/db/migration`. Cada mudança de schema é uma nova migration versionada (`V1`, `V2`, ...) - nunca editamos uma migration já aplicada.

## Autenticação

A API usa JWT stateless (sem sessão guardada no servidor), entregue via **cookie httpOnly** (não Bearer token no header):

1. `POST /api/auth/login` com `{ email, password }` → em caso de sucesso, o servidor responde `200` e define o cookie `auth_token` (httpOnly, SameSite=Lax) : o corpo da resposta fica vazio, o token nunca é exposto ao JavaScript do frontend
2. O navegador reenvia esse cookie automaticamente em toda requisição seguinte para o backend
3. `GET /api/auth/me` : usado pelo frontend para descobrir quem é o usuário logado (já que o JS não consegue ler o cookie httpOnly diretamente)
4. `POST /api/auth/logout` : limpa o cookie (expira imediatamente)
5. Rotas públicas: `POST /api/users` (registro), `/api/auth/**` (login/logout/me)
6. Todas as demais rotas exigem cookie `auth_token` válido

O frontend (Next.js) acessa a API através de um proxy (`rewrites` no `next.config.ts`) para que, do ponto de vista do navegador, front e back sejam a mesma origem: evita configuração de CORS com credenciais.

Autorização por papel (`BUYER`/`ORGANIZER`/`ADMIN`) disponível via `@PreAuthorize("hasRole('...')")`, a ser aplicada conforme novos endpoints forem criados.

## Endpoints implementados

| Método | Rota | Auth | Descrição |
|---|---|---|---|
| POST | `/api/users` | pública | Cria um usuário (papel padrão `BUYER`) |
| GET | `/api/users/{id}` | requer cookie | Busca usuário por id |
| POST | `/api/auth/login` | pública | Autentica e define o cookie `auth_token` |
| POST | `/api/auth/logout` | pública | Limpa o cookie `auth_token` |
| GET | `/api/auth/me` | requer cookie | Retorna os dados do usuário autenticado |

## Decisões técnicas

- **DTOs em toda API**: entidades JPA nunca são expostas diretamente (nem para leitura, nem para escrita), evitando vazamento de dados sensíveis (como hash de senha) e acoplamento entre o formato do banco e o contrato da API.
- **Senhas com BCrypt** desde o primeiro commit, não como adição posterior.
- **Flyway para controle de schema**: o Hibernate nunca altera o banco automaticamente (`ddl-auto` não é usado para isso); toda mudança de schema é uma migration explícita e versionada.
- **JWT stateless** em vez de sessão: escolhido por ser o padrão mais comum para APIs consumidas por SPA, e por não exigir armazenamento de sessão compartilhado entre instâncias do backend.
- **Cookie httpOnly em vez de Bearer token/localStorage** para entregar o JWT ao frontend: impede que o token seja lido por JavaScript (mitiga XSS). Combinado com `SameSite=Lax` e o proxy do Next.js, evita a necessidade de CORS com credenciais.
- **Segredos via variável de ambiente** (`.env`, nunca commitado): banco e `JWT_SECRET` nunca aparecem hardcoded no código nem no `application.properties`.

## Roadmap

- [x] Fase 1 - Ambiente (Docker, Flyway) + CRUD de usuário
- [x] Fase 2 - Autenticação (Spring Security + JWT)
- [ ] Fase 3 - Modelagem de Evento/LoteIngresso/Reserva + lógica de concorrência
- [ ] Fase 4 - Frontend do comprador (React/Next.js)
- [ ] Fase 5 - Camada de segurança (rate limiting, HMAC webhook)
- [ ] Fase 6 - CI (GitHub Actions)
- [ ] Fase 7 - Cloud (AWS)
- [ ] Fase 8 - CD
- [ ] Fase 9 - Frontend do organizador
- [ ] Fase 10 - Kubernetes (futuro)