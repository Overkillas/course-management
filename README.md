# Course Management

Aplicação web para gerenciamento de cursos e alunos, desenvolvida como desafio técnico. O
administrador cadastra alunos e cursos, matricula alunos em cursos e lista os matriculados por
curso; o aluno autentica, consulta o próprio perfil e as próprias matrículas, e troca a senha no
primeiro acesso. O **backend** (API REST) e o **frontend** (Angular) estão implementados e
documentados aqui.

---

## Sobre o projeto

Operações de negócio, centradas na figura do administrador:

- Cadastrar, listar e excluir alunos.
- Cadastrar, listar e excluir cursos.
- Matricular alunos em cursos.
- Listar alunos matriculados por curso.

Sobre essa base há **autenticação por JWT** e **controle de acesso por papel** (admin/aluno),
com **troca de senha obrigatória no primeiro acesso** do aluno. A prioridade foi a qualidade e a
consistência do núcleo: regras de negócio corretas, bem estruturadas e cobertas por testes. O
planejamento, as prioridades e as decisões estão na
[documentação do backend](backend/docs).

---

## Tecnologias

### Backend

- **Linguagem:** Java 17
- **Framework:** Quarkus
- **API:** REST com serialização JSON (Jackson)
- **Persistência:** Hibernate ORM com Panache (padrão Repository)
- **Banco de dados:** MySQL
- **Migrations:** Flyway (schema versionado e dados de referência)
- **Validação:** Bean Validation (Hibernate Validator)
- **Autenticação:** JWT via SmallRye JWT (assinatura assimétrica RS256), com controle de acesso por papel
- **Documentação da API:** OpenAPI / Swagger UI
- **Testes:** JUnit 5 + RestAssured sobre `@QuarkusTest`

### Frontend

- **Framework:** Angular 20 (standalone components + signals)
- **Linguagem:** TypeScript (modo strict)
- **UI:** Angular Material
- **HTTP e estado:** `HttpClient` com interceptors; estado de sessão em services com signals
- **Empacotamento:** estáticos servidos por nginx, em container próprio
- **Testes:** Karma + Jasmine

### Infraestrutura

- **Containerização:** Docker e Docker Compose (frontend + API + MySQL)

---

## Estrutura do repositório

Este é um **monorepo**: backend e frontend convivem no mesmo repositório, cada um em seu próprio
diretório e autocontido (código, documentação e configuração próprios).

```
course-management/
├── backend/                 # API REST (Quarkus)
│   ├── docs/                # documentação de decisões do backend
│   │   ├── db_diagram.md                # modelagem do banco de dados
│   │   ├── decisoes_arquiteturais.md    # arquitetura da API (camadas, pacotes)
│   │   └── planejamento_backend.md      # planejamento, prioridades e decisões
│   └── src/
├── frontend/                # aplicação web (Angular)
│   ├── docs/                # documentação de decisões do frontend
│   │   └── planejamento_frontend.md     # planejamento e decisões do front
│   ├── Dockerfile           # build dos estáticos servidos por nginx
│   └── src/
├── docker-compose.yml       # sobe frontend + API + MySQL
├── .env.example             # template de variáveis de ambiente
└── README.md                # este arquivo
```

A co-localização da documentação em cada subprojeto mantém cada parte autocontida.

---

## Como executar

A forma recomendada de subir tudo (frontend, API e MySQL) é via **Docker Compose**: um comando
levanta os três já conectados, com as migrations e os centros de referência aplicados
automaticamente no primeiro boot.

### Pré-requisitos

- **Docker** e **Docker Compose** (caminho recomendado).
- Para rodar o backend isolado em modo de desenvolvimento: **JDK 17+** (o Maven vem pelo wrapper
  `./mvnw`, não precisa instalar à parte).
- Para rodar o frontend isolado em modo de desenvolvimento: **Node.js 20+** (o Angular CLI vem
  como dependência do projeto, instalado pelo `npm install`).

### Subindo com Docker Compose (recomendado)

1. Crie o seu `.env` a partir do template versionado:

   ```bash
   cp .env.example .env          # Linux / macOS
   ```

   ```powershell
   Copy-Item .env.example .env   # Windows (PowerShell)
   ```

2. Suba tudo com um comando:

   ```bash
   docker compose up --build
   ```

Com tudo no ar (as portas vêm do `.env`):

- **Frontend (aplicação web):** `http://localhost:4200` (porta `FRONTEND_PORT`)
- **API:** `http://localhost:8080` (porta `APP_PORT`)
- **Documentação interativa (Swagger UI):** `http://localhost:8080/q/docs`
- **Health check:** `http://localhost:8080/q/health`

Para derrubar: `docker compose down` (ou `docker compose down -v` para apagar também os dados do
banco).

O **administrador inicial** é semeado no primeiro boot a partir do `.env` (valores de demo:
`admin@unifor.br` / `Ab!12345`).

### Backend em modo de desenvolvimento

Dentro de `backend/`, com o Docker em execução (o Quarkus sobe um MySQL efêmero sozinho via Dev
Services, sem configuração manual de conexão):

```bash
./mvnw quarkus:dev
```

Live reload ativo e Swagger em `/q/docs`.

### Frontend em modo de desenvolvimento

Dentro de `frontend/`, com a API no ar (via Docker Compose ou `quarkus:dev`):

```bash
npm install
npm start        # ng serve em http://localhost:4200, com live reload
```

O CORS da API já libera `http://localhost:4200`. Os testes unitários rodam com `npm test`.

---

## API

A documentação interativa completa fica no **Swagger UI** (`/q/docs`) com a aplicação no ar.
Visão geral dos recursos:

| Método      | Rota                       | Acesso       | Descrição                                         |
|-------------|----------------------------|--------------|---------------------------------------------------|
| `POST`      | `/auth/login`              | público      | Autentica e retorna o token JWT                   |
| `GET POST`  | `/students`                | admin        | Lista / cadastra alunos                           |
| `PATCH`     | `/students/{id}`           | admin        | Atualiza o nome de um aluno                       |
| `DELETE`    | `/students/{id}`           | admin        | Exclui um aluno                                   |
| `GET POST`  | `/courses`                 | admin        | Lista / cadastra cursos                           |
| `DELETE`    | `/courses/{id}`            | admin        | Exclui um curso                                   |
| `GET`       | `/centers`                 | admin        | Lista os centros (dado de referência)             |
| `GET POST`  | `/courses/{id}/students`   | admin        | Lista matriculados / matricula um aluno no curso  |
| `GET`       | `/me`                      | autenticado  | Perfil do próprio usuário                         |
| `GET`       | `/me/courses`              | autenticado  | Cursos em que o aluno está matriculado            |
| `POST`      | `/me/password`             | autenticado  | Troca a própria senha (obrigatória no 1º acesso)  |

Os erros seguem um formato único (`ApiError`: `timestamp`, `status`, `message`, `path` e, em
validações, a lista de campos inválidos).

---

## Autenticação e autorização

- **Login.** `POST /auth/login` com e-mail e senha retorna um **JWT** (assinatura assimétrica
  RS256). O token carrega o papel do usuário e a flag de troca de senha.
- **Papéis.** `admin` (gestão de alunos, cursos e matrículas) e `aluno` (self-service: perfil,
  próprias matrículas e troca de senha). A autorização é declarativa, via `@RolesAllowed`.
- **Primeiro acesso.** O aluno é criado com uma senha temporária definida pelo admin e a flag
  `must_change_password`. Enquanto não trocar a senha (`POST /me/password`: mínimo 8 caracteres,
  com letra, dígito e caractere especial), fica bloqueado nos demais endpoints. Depois de trocar,
  refaz o login e passa a operar normalmente.
- **Chaves.** A assinatura usa um par RSA de **demonstração** versionado em
  `backend/src/main/resources`, para o sistema subir com um único comando. Em produção, gere ou
  injete um par próprio e **não versione a chave privada**.

As escolhas (inclusive por que migrei de HS256 para RS256) estão em
[planejamento_backend.md](backend/docs/planejamento_backend.md) §5.

---

## Testes

```bash
cd backend
./mvnw test
```

São **39 testes de integração** (JUnit 5 + RestAssured sobre `@QuarkusTest`, com um MySQL efêmero
via Dev Services), priorizando valor sobre quantidade: unicidade de matrícula, exclusão em
cascata, autorização por papel, troca de senha no primeiro acesso e validações de entrada. A
estratégia está em [planejamento_backend.md](backend/docs/planejamento_backend.md) §8.

No **frontend**, os testes unitários rodam de dentro de `frontend/`:

```bash
npm test
```

São testes com Karma + Jasmine sobre os pontos de maior valor: a leitura do token e a expiração,
os guards, o interceptor de token, a validação de senha forte e o comportamento de cada tela.

---

## Decisões técnicas e documentação

As decisões de arquitetura, a modelagem e os trade-offs (do backend) estão documentados em detalhe na pasta
[`backend/docs`](backend/docs):

- [db_diagram.md](backend/docs/db_diagram.md): modelagem do banco: entidades, relações,
  integridade e índices.
- [decisoes_arquiteturais.md](backend/docs/decisoes_arquiteturais.md): organização por domínio,
  camadas, DTOs, mapeamento e tratamento de erros.
- [planejamento_backend.md](backend/docs/planejamento_backend.md): princípios, stack, escopo,
  autenticação, prioridades e ordem de implementação.

As decisões do **frontend** ficam em
[planejamento_frontend.md](frontend/docs/planejamento_frontend.md): stack, estrutura de pastas,
telas e rotas, fluxo de autenticação e os trade-offs de UI.
