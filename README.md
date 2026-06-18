# Course Management

Aplicação web para gerenciamento de cursos e alunos, desenvolvida como desafio
técnico. O sistema permite ao administrador cadastrar alunos e cursos, matricular
alunos em cursos e listar os matriculados por curso.

> **Status:** em desenvolvimento ativo. Este README cresce junto com o projeto.
> Algumas seções estão marcadas como _em construção_ e serão preenchidas
> conforme cada parte é implementada.

---

## Sobre o projeto

O objetivo é uma aplicação de gestão acadêmica simples, com as seguintes
operações de negócio centradas na figura do administrador:

- Cadastrar, listar e excluir alunos.
- Cadastrar, listar e excluir cursos.
- Matricular alunos em cursos.
- Listar alunos matriculados por curso.

A prioridade do desenvolvimento é a qualidade e a consistência da solução, com as
regras de negócio principais implementadas de forma correta e bem estruturada. O
detalhamento do planejamento, das prioridades e da ordem de implementação está na
[documentação de planejamento do backend](backend/docs/planejamento_backend.md).

---

## Tecnologias

### Backend

- **Linguagem:** Java 17
- **Framework:** Quarkus
- **API:** REST com serialização JSON (Jackson)
- **Persistência:** Hibernate ORM com Panache
- **Banco de dados:** MySQL
- **Migrations:** Flyway (schema versionado e dados de referência)
- **Validação:** Bean Validation (Hibernate Validator)
- **Documentação da API:** OpenAPI / Swagger UI
- **Autenticação:** JWT _(planejado, ver documentação de planejamento)_

### Frontend

- **Framework:** Angular 17+ _(a ser implementado)_

### Infraestrutura

- **Containerização:** Docker e Docker Compose

---

## Estrutura do repositório

Este é um **monorepo**: backend e frontend convivem no mesmo repositório, cada um
em seu próprio diretório e autocontido (código, documentação e configuração
próprios).

```
course-management/
├── backend/                 # API REST (Quarkus)
│   ├── docs/                # documentação de decisões do backend
│   │   ├── db_diagram.md            # modelagem do banco de dados
│   │   └── planejamento_backend.md  # planejamento, prioridades e decisões
│   └── src/
├── frontend/                # aplicação web (Angular), a ser adicionado
└── README.md                # este arquivo
```

A escolha por monorepo se deu pela simplicidade de avaliação e pela aderência ao
pedido de um único repositório. A co-localização da documentação em cada
subprojeto mantém cada parte autocontida.

---

## Documentação

Documentos de decisão técnica já disponíveis:

- [Modelagem do banco de dados](backend/docs/db_diagram.md): entidades, relações,
  decisões de integridade e índices.
- [Planejamento do backend](backend/docs/planejamento_backend.md): princípios,
  stack, escopo, abordagem de autenticação, prioridades e ordem de implementação.

---

## Como executar

> _Em construção._ Esta seção será preenchida conforme o empacotamento for
> implementado. A intenção é que a aplicação completa suba via Docker Compose com
> um único comando, e que o backend também possa ser executado de forma isolada
> em modo de desenvolvimento.

### Pré-requisitos

> _Em construção._ (Lista de pré-requisitos como JDK, Docker e demais ferramentas.)

### Executando o projeto

> _Em construção._ (Passo a passo de execução via Docker Compose e em modo de
> desenvolvimento.)

---

## API

> _Em construção._ A documentação dos endpoints será disponibilizada via OpenAPI /
> Swagger UI quando o backend estiver em execução, e esta seção trará o endereço de
> acesso e um resumo dos principais recursos.

---

## Autenticação e autorização

> _Em construção._ A abordagem planejada (autenticação por JWT, controle de acesso
> por papel e demais camadas) está descrita na
> [documentação de planejamento](backend/docs/planejamento_backend.md). Esta seção
> trará os detalhes de uso conforme a implementação avançar.

---

## Testes

> _Em construção._ A estratégia de testes prioriza a cobertura das regras de
> negócio principais. Detalhes de como executar a suíte serão adicionados aqui.

---

## Decisões técnicas e suposições

As principais decisões de arquitetura, os trade-offs considerados e as suposições
adotadas estão documentados em detalhe nos arquivos da pasta `backend/docs`. Um
resumo das decisões mais relevantes será consolidado nesta seção ao longo do
desenvolvimento.
