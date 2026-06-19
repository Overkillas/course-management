# Planejamento e Decisões Técnicas do Backend

> Documento de planejamento do backend para o desafio técnico (gerenciamento de
> cursos e alunos). O objetivo aqui não é descrever *como* cada coisa foi
> implementada, e sim registrar **o que pretendo construir, por quê, e em que
> ordem de prioridade**, de forma que o processo de decisão fique explícito antes
> mesmo de o código ser lido.
>
> A modelagem detalhada do banco vive em documento próprio (`db_diagram.md`).
> Aqui o foco é a visão de backend como um todo: stack, escopo, segurança,
> prioridades e sequência de entrega.

---

## 1. Princípios que guiam todas as decisões

Antes das escolhas concretas, os princípios que as orientam. Eles aparecem
repetidos ao longo do documento porque foram o critério de desempate em cada
decisão difícil.

**Aderência ao escopo antes de ambição.** O desafio é explícito ao dizer que uma
solução bem pensada e consistente vale mais que uma implementação extensa, porém
frágil. Toda funcionalidade extra que eu adicionar precisa justificar seu custo,
e nenhuma delas pode comprometer o núcleo que o desafio de fato avalia.

**Regras de negócio defendidas no banco, não só na aplicação.** Validação na
camada de aplicação é a primeira linha de defesa, mas não a última. Restrições
de integridade (unicidade, chaves estrangeiras, checagens) vivem também no
schema, de modo que a regra continue válida mesmo que a aplicação erre.

**A aplicação tem que levantar em qualquer máquina.** O critério de avaliação
mais importante é conseguir subir o sistema. Por isso a infraestrutura é
empacotada de forma reprodutível e a separação entre ambiente de desenvolvimento
e ambiente de entrega é tratada com cuidado (seção 4).

**Começar simples e evoluir de forma aditiva.** Cada decisão de modelagem foi
pensada para que evoluções futuras sejam acréscimos, não reescritas. Isso me
permite priorizar com segurança: se o tempo apertar, o que ficar de fora não
deixa dívida estrutural.

---

## 2. Stack do backend

| Camada | Escolha | Motivo resumido |
|---|---|---|
| Framework | Quarkus | Sugerido pelo desafio. Dev mode com live reload e ferramentas de produtividade que aceleram o ciclo. |
| API REST | Stack REST moderna do Quarkus + Jackson | Padrão recomendado atual para APIs REST com JSON no ecossistema. |
| Persistência | Hibernate ORM com Panache | ORM consolidado, com a camada Panache reduzindo boilerplate de CRUD sem esconder o que está acontecendo. Modelo imperativo/bloqueante, adequado a um CRUD (o modelo reativo seria complexidade sem retorno aqui). |
| Banco | MySQL | Banco relacional dentro das opções do desafio, empacotado via Docker para reprodutibilidade. |
| Migrations | Flyway | Versiona o schema e permite expressar fielmente restrições que a geração automática de schema não cobre (checagens e comportamento de exclusão). Também versiona os dados de referência (seeds). |
| Validação | Bean Validation (Hibernate Validator) | Validação declarativa de entrada no backend, um dos diferenciais pedidos. |
| Documentação da API | OpenAPI + Swagger UI | Contrato navegável da API. Facilita o avaliador testar sem ferramenta externa e serve de referência para o consumo pelo frontend. |
| Autenticação | JWT (assinatura assimétrica) | Padrão de mercado para APIs stateless, base para o controle de acesso por papel. |

**Por que Flyway e não geração automática de schema.** A geração automática de
tabelas pelo ORM cria o essencial, mas não reproduz fielmente certas regras de
integridade que eu quero garantir no banco (checagens de valor e o comportamento
de exclusão em cascata ou restrito). Versionar o schema com migrations deixa o
banco como fonte de verdade explícita, dá histórico de evolução e combina com o
critério de boas práticas.

**Nota de contexto sobre a stack.** Este é o meu primeiro projeto com Quarkus.
Já tenho experiência prévia com Java e com Spring Boot, e parte da motivação ao
escolher seguir a sugestão do desafio foi exatamente exercitar a transição de um
ecossistema que conheço para o Quarkus. Registro isso de forma transparente por
dois motivos: primeiro, porque algumas decisões podem refletir uma escolha mais
conservadora ou idiomática enquanto eu consolido familiaridade com o framework;
segundo, porque entendo que avaliar como alguém aprende uma ferramenta nova é
tão revelador quanto avaliar o domínio de uma já conhecida. Onde uma convenção
específica do Quarkus diferir do que eu faria por hábito no Spring, a intenção é
seguir o idiomático do Quarkus.

---

## 3. Escopo funcional (o núcleo avaliado)

As quatro operações de negócio que o desafio pede são o coração da entrega e têm
prioridade máxima:

1. Cadastrar, listar e excluir **alunos**.
2. Cadastrar, listar e excluir **cursos**.
3. **Matricular** alunos em cursos.
4. **Listar alunos matriculados** por curso.

A regra de negócio central da matrícula é que um aluno não pode se matricular
duas vezes no mesmo curso. Essa regra é garantida por unicidade no banco, o que
a torna idempotente independentemente do comportamento da interface (um clique
duplo no botão não gera vínculo duplicado).

A exclusão de aluno e de curso remove em cascata as matrículas associadas,
evitando vínculos órfãos. A modelagem completa, com todas as decisões de
integridade referencial, está em `db_diagram.md`.

---

## 4. Ambiente: desenvolvimento e entrega são fases diferentes

Essa é uma distinção deliberada e importante para entender o setup.

**Durante o desenvolvimento**, o Quarkus sobe automaticamente as dependências de
infraestrutura (banco e cache) como containers efêmeros, sem necessidade de
configuração manual de conexão. Isso mantém o ciclo de desenvolvimento rápido e
sem fricção.

**Para a entrega**, existe um arquivo Docker Compose que sobe a aplicação
junto com o banco (e o cache, quando aplicável), com as conexões configuradas de
forma explícita por variáveis de ambiente. O objetivo é que avaliar a aplicação
seja, na prática, um único comando para levantar tudo.

As duas fases convivem por meio de perfis de configuração: o perfil de
desenvolvimento usa a infraestrutura automática, e o perfil de produção usa as
conexões explícitas injetadas no Docker Compose. É o mesmo código, com
configurações distintas por ambiente.

O uso de Docker atende, ainda, ao diferencial de empacotamento pedido pelo
desafio.

---

## 5. Autenticação e autorização

A autenticação é um diferencial do desafio, e eu pretendo implementá-la de forma
completa. O ponto central da decisão de design é que a autenticação demonstre
**controle de acesso por papel de verdade**, e não apenas uma tela de login
simbólica.

**Modelo de usuário e papéis.** Existe um papel de administrador e um papel de
aluno. O administrador é quem executa as operações de gestão (todo o núcleo da
seção 3). O aluno é um usuário que também consegue autenticar, o que permite
demonstrar a diferença real de permissões entre os dois papéis.

**Criação de aluno e primeira senha.** O administrador cadastra o aluno e define
uma senha inicial (tratada como temporária). No primeiro acesso, o aluno
autenticado pode trocar a própria senha. Esse fluxo é simples e não depende de
nenhuma infraestrutura externa, porque o usuário já está autenticado no momento
da troca. As senhas nunca são armazenadas em texto puro, apenas seu hash.

*Alternativa considerada e descartada.* Cheguei a desenhar um fluxo em que a
senha inicial seria gerada automaticamente pelo sistema (um valor padrão como
`Ab!12345`), em vez de informada pelo administrador. A vantagem seria abstrair do
admin a responsabilidade de criar e repassar uma senha ao aluno. Descartei essa
ideia por uma questão de incerteza sobre quanto controle o administrador deve
ter sobre a credencial. Como o desafio é centrado na figura do administrador,
parti do princípio de que dar a ele mais controle (definir explicitamente a
senha inicial) é preferível a esconder essa decisão atrás de uma geração
automática. A geração automática fica registrada como uma evolução possível,
caso a regra de negócio venha a favorecer menos intervenção do admin.

**Controle de acesso por papel.** As operações de gestão são restritas ao
administrador. Para que a distinção de papéis seja efetivamente demonstrável (e
não apenas "o aluno é barrado em tudo"), o aluno tem acesso a um conjunto de
endpoints próprios, como consultar o próprio perfil e listar as próprias
matrículas. Esse recorte introduz uma regra de autorização mais fina e mais
interessante: o aluno só enxerga os próprios dados, ou seja, a autorização leva
em conta não apenas o papel, mas a posse do recurso (ownership), e não somente o
cargo.

**Sessão stateless e a reintrodução consciente de estado.** O token de acesso é
stateless por natureza, o que é justamente a vantagem do JWT. A funcionalidade
de refresh token, que permite renovar o acesso e revogar sessões, reintroduz
estado no servidor de forma deliberada (o estado fica em um cache dedicado). Essa
é uma decisão consciente de trade-off: ganho de controle sobre as sessões em
troca de mais uma peça de infraestrutura. Por ser a camada de maior custo e
menor retorno relativo para o que o desafio avalia, ela é a de menor prioridade
(seção 6).

**Recuperação de senha e proteção contra abuso.** Como evolução do fluxo de
autenticação, pretendo oferecer recuperação de senha para o usuário que não está
autenticado, usando um código de uso único enviado por canal externo (email),
com expiração curta, validação por tentativa limitada e armazenamento apenas do
hash do código. Essa funcionalidade depende de infraestrutura de envio, então é
tratada como camada avançada. Em conjunto, pretendo aplicar limitação de
requisições (rate limiting) nos pontos sensíveis de autenticação, para proteger
contra força bruta e abuso de emissão de códigos.

> **Nota de honestidade sobre escopo de autenticação.** Tenho consciência de que
> o conjunto de autenticação planejado (controle por papel, refresh token,
> recuperação de senha, rate limiting) é mais robusto do que o estritamente
> pedido. A prioridade explícita da seção 6 existe exatamente para garantir que,
> caso o tempo aperte, o corte aconteça nas camadas avançadas de autenticação, e
> nunca no núcleo de negócio que o desafio avalia. O subsistema de segurança não
> pode crescer a ponto de ficar maior ou mais frágil que o domínio que ele
> protege.

---

## 6. Prioridades

As funcionalidades estão organizadas em níveis de prioridade. O nível indica a
ordem de sacrifício: se faltar tempo, o corte começa de baixo para cima, sempre
preservando os níveis superiores.

### Prioridade 0: Núcleo (inegociável)

Tudo que o desafio avalia diretamente. Sem isto, não há entrega.

- Modelagem e schema versionado com migrations.
- Dados de referência (centros) carregados via seed.
- CRUD de alunos.
- CRUD de cursos.
- Matrícula de aluno em curso, com a regra de unicidade garantida.
- Listagem de alunos matriculados por curso.
- Validação de entrada no backend.
- Documentação navegável da API.
- Empacotamento via Docker que permita subir a aplicação por um comando.

### Prioridade 1: Diferenciais de alto valor e baixo risco

Diferenciais que agregam muito e têm risco controlado de implementação.

- Autenticação por JWT.
- Controle de acesso por papel (administrador e aluno).
- Endpoints próprios do aluno com autorização por posse do recurso.
- Troca de senha no primeiro acesso (usuário já autenticado).
- Testes automatizados do backend.

### Prioridade 2: Diferenciais avançados

Camadas que demonstram profundidade, mas com maior custo e maior superfície de
falha. São as primeiras candidatas a virar "trabalho futuro" caso o tempo aperte.

- Refresh token com renovação e revogação de sessão (estado em cache dedicado).
- Recuperação de senha por código de uso único enviado por email.
- Limitação de requisições (rate limiting) nos pontos sensíveis de autenticação.

---

## 7. Ordem de implementação

A sequência é pensada para que **cada etapa termine com a aplicação subindo e
funcionando**. Nunca há um estado intermediário quebrado: a qualquer momento que
o desenvolvimento parasse, haveria um sistema executável e coerente até ali.

**Etapa 1: Fundação de dados.** Schema versionado por migrations, entidades
mapeadas, dados de referência semeados. Ao final, a base existe e está
consistente.

**Etapa 2: Núcleo de negócio.** Os CRUDs de aluno e curso, a matrícula e a
listagem por curso, com validação de entrada e documentação da API. Ao final, o
que o desafio pede está completo e testável de ponta a ponta. Este é o marco que
garante uma entrega válida.

**Etapa 3: Identidade e acesso.** Autenticação por JWT e controle de acesso por
papel, incluindo os endpoints próprios do aluno e a troca de senha no primeiro
acesso. Ao final, o sistema distingue papéis e protege os recursos de fato.

**Etapa 4: Qualidade.** Os testes automatizados são escritos junto de cada
feature, conforme cada módulo é construído (ver seção 8), então não é aqui que
os testes começam: esta etapa consolida a cobertura, fecha lacunas e edge cases
e cobre os fluxos de acesso por papel, que só passam a existir após a Etapa 3.
Garante que o que foi construído está defendido contra regressão.

**Etapa 5: Camadas avançadas de segurança.** Refresh token com revogação,
recuperação de senha por email e limitação de requisições. Acréscimos sobre uma
base já completa e estável.

**Etapa 6: Empacotamento de entrega.** Finalização do Docker Compose para que
o avaliador suba tudo de forma simples, e revisão da documentação de execução.

> O empacotamento básico para subir a aplicação é tratado desde cedo (faz parte
> da Prioridade 0). A Etapa 6 é o refinamento final da experiência de entrega,
> não o primeiro contato com Docker.

---

## 8. Qualidade e testes

Os testes automatizados são um diferencial pedido, e a estratégia é
**priorizar valor sobre quantidade**. O foco está em cobrir as regras de negócio
que mais importam:

- A regra de unicidade da matrícula (um aluno não se matricula duas vezes no
  mesmo curso).
- O comportamento de exclusão em cascata (excluir aluno ou curso remove as
  matrículas, sem deixar vínculos órfãos).
- As regras de autorização (cada papel acessa apenas o que lhe é permitido, e o
  aluno enxerga apenas os próprios dados).
- As validações de entrada rejeitando dados inválidos.

A intenção não é perseguir um número de cobertura, e sim testar os pontos onde
uma falha teria consequência real de negócio.

**Cadência.** Os testes são escritos junto de cada feature, no mesmo momento em
que a regra é implementada, e não num passe único ao final. Cada módulo entra
com a prova das suas regras; a Etapa 4 (seção 7) atua como reforço e fechamento
de lacunas, não como o ponto em que os testes começam.

---

## 9. Suposições assumidas

Registradas explicitamente porque o desafio valoriza a documentação do processo
de decisão.

1. **Alunos também autenticam.** Optei por permitir que o aluno faça login, em
   vez de tratá-lo apenas como dado gerenciado, para poder demonstrar o controle
   de acesso por papel de forma efetiva.
2. **A senha inicial do aluno é definida pelo administrador no cadastro** e
   tratada como temporária, com troca disponível no primeiro acesso.
3. **Os centros são dados de referência semeados**, fora do escopo de CRUD. Eles
   enriquecem o domínio sem adicionar superfície de gestão que o desafio não
   pede.
4. **A exclusão de aluno e de curso é definitiva** (sem preservação de
   histórico). Caso o histórico passasse a ser necessário, o caminho natural
   seria exclusão lógica, registrado como evolução futura.

---

## 10. Fora de escopo e evoluções futuras

Itens deliberadamente deixados de fora desta entrega, com o caminho de evolução
já pensado para que sejam acréscimos, e não reescritas:

- Preservação de histórico de matrículas via exclusão lógica.
- Acompanhamento de estado da matrícula (em andamento, concluída, cancelada).
- Gestão administrativa dos centros como entidade de CRUD completa.
- Enriquecimento dos centros com atributos institucionais próprios.

A modelagem atual foi desenhada para acomodar essas evoluções de forma aditiva,
o que confirma a decisão de começar simples sem gerar dívida estrutural.
