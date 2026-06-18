# Modelagem do Banco de Dados

> Documento de decisões de modelagem para o desafio técnico (gerenciamento de
> cursos e alunos). O foco aqui não é ensinar conceitos de banco, e sim
> registrar **as decisões de schema, o porquê de cada uma e os trade-offs**
> envolvidos. Banco: **MySQL** (InnoDB). Diagrama no dbdiagram.io (DBML).

---

## 1. Princípios que guiam as decisões

**Aderência ao escopo.** O schema modela exatamente o que as quatro operações de
negócio exigem, da forma mais limpa possível. Complexidade que o desafio não pede
é registrada como plano futuro (§7), não antecipada.

**Regras de negócio defendidas no banco.** Validação na aplicação é a primeira
linha de defesa, não a última. Unicidade, chaves estrangeiras e checagens vivem
também no schema, de modo que a regra continue válida mesmo que a aplicação erre.

**Evolução aditiva.** Cada decisão foi tomada para que evoluções futuras sejam
acréscimos, não reescritas. Começar simples não gera dívida estrutural.

Como o escopo não exige controle de progressão ou conclusão de curso, a tabela
que liga aluno e curso é uma *tabela de junção livre*: registra apenas o vínculo,
sem lógica de progresso. O acompanhamento de estado da matrícula está preservado
em [Planos futuros](#7-planos-futuros).

---

## 2. Versão adotada (escopo do desafio)

Quatro tabelas: `users`, `centers`, `courses` e a junção `enrollments`.

> Este bloco é a **representação conceitual** do schema, otimizada para leitura:
> os `Enum` documentam os valores válidos, mas fisicamente essas colunas são
> `VARCHAR` (§3.3), e restrições como `CHECK` e `ON DELETE` não aparecem aqui. A
> **fonte de verdade executável do schema são as migrations Flyway**; este
> documento existe para explicar a estrutura e as decisões, não para ser rodado.

```dbml
Enum user_role {
  admin
  aluno
}

Table users {
  id                   integer      [pk, increment]
  name                 varchar(100) [not null]
  email                varchar(255) [not null, unique] // usado no login
  password_hash        varchar(255) [not null]         // hash (bcrypt/argon2), nunca texto puro
  role                 user_role    [not null, default: 'aluno'] // fisicamente VARCHAR, ver 3.3
  must_change_password boolean      [not null, default: true]    // ver 3.3
  created_at           timestamp    [not null, default: `now()`]
}

Table centers {
  id         integer     [pk, increment]
  code       varchar(10) [not null, unique] // sigla do centro (ex: CCT, CCS)
  created_at timestamp   [not null, default: `now()`]
  updated_at timestamp   [not null, default: `now()`]
}

Table courses {
  id              integer      [pk, increment]
  name            varchar(100) [not null]
  center_id       integer      [not null, ref: > centers.id] // ON DELETE RESTRICT, ver 3.4
  total_semesters integer      [not null] // CHECK (total_semesters > 0) na DDL
  created_at      timestamp    [not null, default: `now()`]
}

// Tabela de juncao "livre": registra apenas o vinculo aluno <-> curso.
Table enrollments {
  id         integer   [pk, increment]
  user_id    integer   [not null, ref: > users.id]
  course_id  integer   [not null, ref: > courses.id]
  created_at timestamp [not null, default: `now()`]

  indexes {
    (user_id, course_id) [unique, name: 'uq_enrollment_user_course']
    course_id            [name: 'idx_enrollment_course']
  }
}
```

> Notas sobre o DBML:
> - O dbdiagram.io não modela `CHECK` constraints nem o comportamento `ON DELETE`
>   de forma inline. A restrição `total_semesters > 0` e os `ON DELETE` (§3.4 e
>   §3.6) entram na migration real (no MySQL 8.0.16+ as `CHECK` são validadas).
>   Ficam aqui como comentário.
> - A coluna `role` aparece como `user_role` no diagrama apenas para documentar os
>   valores válidos. Fisicamente é `VARCHAR` (§3.3).

---

## 3. Decisões de modelagem

### 3.1. Nomenclatura

Todo o schema usa inglês e `snake_case`, com tabelas no plural (a tabela
representa a coleção de registros). Padroniza o vocabulário e evita inconsistência
de idioma no mesmo schema.

### 3.2. Chave primária: identificador artificial auto-incremento

Todas as tabelas usam `id integer [pk, increment]`: uma chave primária artificial,
sem significado de negócio.

- **Por quê:** identificadores artificiais são estáveis (não mudam quando um dado
  de negócio muda) e simplificam referências entre tabelas e o mapeamento ORM.
- **Trade-off na junção:** `enrollments` poderia ter PK composta
  `(user_id, course_id)`, dispensando o `id` e já forçando a unicidade. Optei pelo
  meio-termo: `id` artificial + constraint `UNIQUE(user_id, course_id)`. Mantenho a
  ergonomia de uma linha referenciável por um único `id` e ainda garanto a
  unicidade do par. Para quem prefira modelagem relacional estrita, a PK composta é
  igualmente defensável.

### 3.3. Tabela `users`

| Decisão | Motivo | Trade-off / anti-padrão evitado |
|---|---|---|
| `email` é `NOT NULL` + `UNIQUE` | É a credencial de login. Sem `UNIQUE`, dois cadastros com o mesmo e-mail tornam a autenticação ambígua. | Confiar só na validação da aplicação; a constraint é a defesa que independe de a aplicação acertar. |
| `password` armazenado como `password_hash` | O nome documenta que ali nunca entra senha em texto puro. | Armazenar senha reversível/plana. |
| `role` é enum comportamental **no código**, persistido como **string** (`VARCHAR`) | A autorização raciocina sobre esse valor (`admin` vs `aluno`), então ele é um conceito de domínio que vive no código. Persistir pelo nome é portável e dispensa o tipo `ENUM` proprietário. | Uma tabela `roles` permitiria papéis administráveis em runtime e N:N; descartada por ser mais do que o escopo pede. **Anti-padrão evitado:** persistir o enum como ordinal (0,1,2), que corromperia os dados ao reordenar as constantes. Persistir sempre pelo nome. |
| `must_change_password boolean NOT NULL default true` | Materializa o fluxo de troca de senha no primeiro acesso: o aluno criado pelo admin nasce com `true`, e o primeiro login com troca bem-sucedida grava `false`. | Inferir "senha temporária" a partir de outra coluna; um booleano explícito é mais legível e direto. |
| `created_at` como `timestamp` com `default NOW()` | Guarda o instante do cadastro, suficiente para auditoria e ordenação. | |

### 3.4. Centro como tabela (`centers`), não enum

`center` é uma tabela de lookup referenciada por `courses.center_id`, em vez de
um enum.

**Por que tabela, e não enum (nem no banco nem no código):** o centro é dado de
referência, não um valor sobre o qual o código decide fluxo. Um `ENUM` nativo do
SQL exigiria `ALTER TABLE ... MODIFY COLUMN` a cada valor novo (o acoplamento de
DDL do enum sem o benefício de o valor ser administrável como dado), e declarar o
enum no código acoplaria o deploy da aplicação a uma mudança que é, na prática,
dado. A tabela resolve os dois: centros são geridos via `INSERT`, sem migration
nem deploy, e a tabela pode ganhar atributos próprios depois (§7).

Decisões de integridade:

- **`centers.code` é `NOT NULL` + `UNIQUE`:** `code` guarda a sigla (ex: `CCT`,
  `CCS`), que é a chave de negócio mais estável do centro (mais que um nome
  completo, este reescrevível). Por isso é o `UNIQUE` natural. `varchar(10)` basta.
- **`courses.center_id` é `NOT NULL`:** todo curso pertence a um centro.
- **FK com `ON DELETE RESTRICT`:** o banco bloqueia excluir um centro com cursos
  vinculados. `CASCADE` seria perigoso (apagaria os cursos junto), e o escopo pede
  exclusão de alunos e cursos, não de centros.
- **`total_semesters NOT NULL`** com `CHECK (total_semesters > 0)` na DDL: número
  de semestres negativo ou zero não faz sentido de negócio.

### 3.5. Tabela `enrollments` (junção)

- **Relacionamento N:N.** Um curso tem vários alunos, e o modelo acomoda também um
  aluno em vários cursos. Mesmo que a regra real seja "um curso por aluno", o N:N é
  o superconjunto flexível: o caso 1:N vira um caso particular controlado por
  constraint, sem reescrever o schema (suposição em §5, refinamento em §7).
- **Junção livre:** além das FKs e do `created_at`, a tabela não carrega nada,
  coerente com a decisão de não modelar progressão (§1).
- **`UNIQUE(user_id, course_id)` é a regra central da matrícula:** um aluno não se
  matricula duas vezes no mesmo curso. A constraint torna a operação idempotente no
  nível do banco (um duplo-clique no botão "Matricular" não gera vínculo
  duplicado), independentemente do que a aplicação faça.

### 3.6. Comportamento de exclusão (`ON DELETE`)

Para as FKs de `enrollments`, adotei hard delete com `ON DELETE CASCADE`: apagar um
aluno ou um curso remove as matrículas associadas, evitando vínculos órfãos.

- **Trade-off:** hard delete perde histórico. Se preservar o registro de matrículas
  passadas fosse requisito, a escolha seria soft delete (`deleted_at`): "excluir"
  vira `UPDATE` e as listagens filtram `WHERE deleted_at IS NULL`. O histórico não é
  pedido, então hard delete é suficiente e mais enxuto (§5).

| FK | `ON DELETE` | Razão |
|---|---|---|
| `enrollments.user_id` -> `users.id` | `CASCADE` | Excluir aluno remove suas matrículas. |
| `enrollments.course_id` -> `courses.id` | `CASCADE` | Excluir curso remove suas matrículas. |
| `courses.center_id` -> `centers.id` | `RESTRICT` | Não permitir apagar centro com cursos vinculados. |

---

## 4. Índices

Os índices aqui têm duas naturezas distintas. Os **únicos** (`email`, `code`, o
par `user_id, course_id`) existem primariamente por **correção**: impõem regras de
negócio no banco, e a aceleração de leitura é um efeito colateral. Os
**secundários não-únicos** (`idx_enrollment_course`) existem por **performance de
leitura**: não impõem nenhuma regra, e neste volume de dados o ganho é
imperceptível. Eu os incluo deliberadamente porque expressam a intenção de design
para a query que o desafio nomeia ("listar alunos por curso"): em escala, é
exatamente esse índice que separa uma busca direta de um full scan da tabela de
matrículas. Mesmo assim evito o excesso: cada índice custa escrita, então não
indexo coluna sem uma regra ou uma query do escopo que a justifique.

1. **`UNIQUE` em `users.email`.** Impede contas duplicadas e atende o lookup do
   login (`WHERE email = ?`).
2. **`UNIQUE` em `centers.code`.** Impede sigla de centro duplicada.
3. **`UNIQUE(user_id, course_id)` em `enrollments`** (`uq_enrollment_user_course`).
   Materializa a regra "no máximo uma matrícula do aluno por curso" e acelera a
   checagem "esse aluno já está nesse curso?".
4. **Índice em `enrollments.course_id`** (`idx_enrollment_course`). Atende o
   requisito *"Listar alunos matriculados por curso"* (`WHERE course_id = ?`). O
   índice composto `(user_id, course_id)` não serve para essa query, porque um
   índice composto só é aproveitado quando o filtro inclui sua coluna líder
   (`user_id`, a regra do prefixo mais à esquerda); por isso declaro um índice
   próprio liderado por `course_id`. Com os dois, cubro os dois sentidos de
   consulta: "cursos de um aluno" pelo prefixo do composto, "alunos de um curso"
   por este índice.

   *Alternativa equivalente:* inverter o único para `(course_id, user_id)` e criar
   um avulso em `user_id`. Escolho a ordem atual por convenção; ambas são corretas.

**Índices de chave estrangeira.** Não declaro índices avulsos para as FKs porque o
InnoDB cria um índice para toda coluna de FK que ainda não tenha um. Na prática,
`user_id` e `course_id` já são cobertos pelos índices acima, e `center_id` recebe o
índice automático do engine, o que também mantém eficiente o `RESTRICT` ao excluir
um centro. (Nota de portabilidade: o PostgreSQL não indexa FKs automaticamente,
então num eventual port seria preciso declará-los.)

**Não indexado deliberadamente:** `name`, `role` e demais colunas. Não há query no
escopo que filtre por elas.

---

## 5. Suposições adotadas

1. **Exclusão de aluno/curso é definitiva (hard delete).** O histórico de
   matrículas não precisa ser preservado. Caso passe a ser, o caminho é soft delete
   (`deleted_at`).
2. **Regra de múltiplos cursos por aluno não confirmada.** O N:N foi escolhido por
   acomodar ambos os casos. Se a regra for "um curso ativo por vez", vira uma
   constraint adicional (§7).
3. **Centros são dados de referência semeados**, geridos na tabela `centers`.
   Excluir um centro com cursos é bloqueado pela FK (`RESTRICT`).

---

## 6. Resumo das relações

- `centers` 1 : N `courses` (FK `center_id`, `ON DELETE RESTRICT`).
- `users` N : N `courses`, resolvido pela junção `enrollments`
  (`enrollments.user_id` e `enrollments.course_id`, ambas `ON DELETE CASCADE`).
- Unicidade do par `(user_id, course_id)` garantida por índice único.

---

## 7. Planos futuros

Evoluções já pensadas para serem aditivas, fora do escopo desta entrega.

### 7.1. Enriquecer `centers`

A tabela nasce mínima (`id`, `code`, `created_at`, `updated_at`); ter virado tabela
(e não enum) é o que permite crescê-la como dado. Candidatos: **`name`** (nome
completo, separado da sigla, para exibição), **`description`** e **`active`** (para
desativar um centro sem apagá-lo).

### 7.2. Estado da matrícula (progressão)

Se o produto precisar acompanhar o estado da matrícula, a evolução é enriquecer a
junção com um estado explícito, definido pelo administrador (estado declarado, não
inferido):

```dbml
Enum enrollment_status {
  active     // matricula em andamento
  completed  // curso concluido
  cancelled  // matricula trancada/cancelada
}

Table enrollments {
  id         integer           [pk, increment]
  user_id    integer           [not null, ref: > users.id]
  course_id  integer           [not null, ref: > courses.id]
  status     enrollment_status [not null, default: 'active'] // fisicamente VARCHAR
  created_at timestamp         [not null, default: `now()`]
  updated_at timestamp         [not null, default: `now()`]

  indexes {
    (user_id, course_id) [unique, name: 'uq_enrollment_user_course']
    course_id            [name: 'idx_enrollment_course']
  }
}
```

Pontos de design:

- **`status` é enum comportamental** (máquina de estados `active -> completed`),
  então, como `user_role`, vive no código e é persistido como string. No diagrama
  aparece como `enrollment_status` só para documentar os valores.
- **`updated_at`** para auditar quando o status mudou.
- **Regra "um aluno com no máximo um curso ativo".** O MySQL não tem índice parcial
  (filtrado), recurso que no PostgreSQL resolveria isso direto. A técnica
  equivalente no MySQL é uma **coluna gerada**: uma coluna cujo valor o banco
  calcula a partir de outras (aqui, vale o `user_id` quando a matrícula está ativa
  e `NULL` caso contrário), com um índice único sobre ela. Como vários `NULL` não
  colidem num índice único, as matrículas históricas ficam livres e só pode existir
  uma ativa por aluno:

  ```sql
  ALTER TABLE enrollments
    ADD COLUMN active_user_id INT
      AS (IF(status = 'active', user_id, NULL)) STORED,
    ADD UNIQUE KEY uq_one_active_enrollment (active_user_id);
  ```

A migração da versão livre para esta seria aditiva (adicionar `status` com default
`active`, `updated_at` e, se aplicável, a coluna gerada), sem quebrar dados
existentes.
