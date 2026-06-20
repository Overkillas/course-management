# Decisões Arquiteturais da API

> Documento de decisões de arquitetura do backend para o desafio técnico
> (gerenciamento de cursos e alunos). O foco aqui não é ensinar padrões de
> arquitetura, e sim registrar **como organizei o código da API e por que escolhi
> cada estrutura**. Não há código de implementação; o documento registra o desenho
> e o raciocínio por trás dele.
>
> A modelagem de dados vive em `db_diagram.md` e o planejamento geral, com escopo,
> prioridades e ordem de implementação, em `planejamento_backend.md`. Este
> documento assume as decisões daqueles dois.

---

## 1. Princípio norteador: organização por domínio

A decisão estrutural mais importante que tomei foi organizar o código **por
domínio** (package-by-feature), e não por camada técnica (package-by-layer).

Na prática, deixei o nível mais alto de pacotes representar **conceitos de
negócio** (`user`, `course`, `enrollment`, `center`), e dentro de cada um coloquei
as camadas técnicas daquele domínio (a exposição da API, a regra de negócio, o
acesso a dados, a entidade, os objetos de transporte). A alternativa que descartei
é o inverso: pacotes de camada no topo (`resources`, `services`, `repositories`)
com todos os domínios misturados dentro de cada um.

**Por que organizei por domínio.** O critério é coesão: o que muda junto fica
junto. Quando preciso mexer na regra de matrícula, abro o pacote `enrollment` e
encontro ali a exposição, a regra e a persistência daquele conceito, sem navegar
entre quatro pastas distantes. A organização por camada espalha um único conceito
por todo o projeto, o que aumenta o custo de entender e modificar uma
funcionalidade.

**Trade-off.** A organização por camada tem a vantagem de tornar a arquitetura em
camadas imediatamente visível na árvore de pastas, e é familiar para quem aprendeu
assim. O custo, porém, é o espalhamento do domínio, que piora conforme o número de
funcionalidades cresce. Para uma aplicação organizada em torno de regras de
negócio claras, considero a organização por domínio a que melhor expressa a
intenção do sistema.

Esse é o mesmo princípio de localidade que apliquei em outras decisões do projeto
(documentação co-localizada por subprojeto, configuração próxima do que ela
descreve): manter próximo o que é conceitualmente relacionado.

---

## 2. As camadas dentro de cada domínio

Dentro de cada módulo segui uma estrutura em camadas com responsabilidades bem
separadas. Separei as responsabilidades de propósito: cada camada tem um único
motivo para mudar.

| Camada | Responsabilidade | Por que separei |
|---|---|---|
| `resource` | Exposição HTTP: recebe a requisição, delega, devolve a resposta. | Isola o protocolo (HTTP/REST) da regra de negócio. A regra não deve saber que está sendo chamada por HTTP. |
| `service` | Regra de negócio e orquestração. | É o coração do domínio. Concentra as decisões e validações de negócio, independente de como os dados entram ou saem. |
| `repository` | Acesso a dados. | Isola a persistência. O service raciocina sobre o domínio sem se acoplar a detalhes de banco. |
| `domain` | A entidade e os tipos do domínio. | Representa o conceito de negócio em si, a estrutura sobre a qual todo o resto opera. |
| `dtos` | Objetos de transporte de entrada e saída da API. | Desacopla o contrato público da API da estrutura interna de persistência (ver seção 4). |
| `mapper` | Conversão entre entidade e DTO. | Centraliza a tradução, mantendo service e resource livres desse detalhe (ver seção 5). |

### 2.1. Terminologia: `resource`, não `controller`

Chamei a camada de exposição de `resource`, e não `controller`. No padrão Jakarta
REST (JAX-RS), que é a base da stack REST do Quarkus, a classe que expõe endpoints
é um `resource`, enquanto `controller` é o termo de outro ecossistema (Spring
MVC). Usar `resource` é apenas seguir a convenção do framework sobre o qual o
projeto é construído, mantendo o código alinhado ao vocabulário que o próprio
Quarkus e suas ferramentas adotam.

### 2.2. Acesso a dados: padrão Repository, e não Active Record

Na camada de persistência usei o **padrão Repository**: cada domínio tem uma
classe de repositório dedicada, e a entidade permanece focada em representar o
dado, sem carregar os métodos de persistência.

O ORM oferece dois estilos: o *Active Record*, em que a própria entidade carrega
os métodos de acesso a dados, e o *Repository*, em que essa responsabilidade vive
em uma classe separada. Escolhi o Repository de propósito, por dois motivos:

- **Separação de responsabilidades.** A entidade representa o conceito de negócio;
  o repositório representa o acesso a esse conceito no armazenamento. Mantê-los
  separados respeita a divisão de camadas descrita acima.
- **Testabilidade.** Com o acesso a dados isolado em uma classe própria, consigo
  testar o service substituindo o repositório por uma implementação de teste, sem
  depender do banco real. O Active Record, ao fundir entidade e persistência,
  dificulta esse isolamento.

**Trade-off.** O Active Record é mais conciso e gera menos arquivos, sendo
conveniente para casos muito simples. O custo é o acoplamento entre o modelo e a
persistência. Como valorizo camadas claras e testes das regras de negócio, o
padrão Repository me pareceu o mais coerente para este projeto.

### 2.3. Estratégia de busca: `JOIN FETCH` para evitar N+1

As associações entre entidades são mapeadas como `LAZY` (carregadas sob demanda),
o padrão seguro que evita arrastar o grafo inteiro a cada consulta. O efeito
colateral conhecido é o **N+1**: ao listar N entidades e tocar uma associação lazy
de cada uma (por exemplo, o centro de cada curso, para montar o DTO de resposta), o
ORM dispara 1 consulta da lista mais 1 por associação acessada.

Por isso, as consultas de listagem que precisam de uma associação a materializam num
único SQL com `JOIN FETCH`, em vez de deixar o lazy disparar consulta por consulta
(ver `CourseRepository.listAllWithCenter`). No volume deste desafio o ganho é
imperceptível, e o cache de primeiro nível do Hibernate já limita o custo aos
registros distintos, mas declaro o `JOIN FETCH` deliberadamente para expressar a
intenção de design e manter a leitura à prova de escala. É o mesmo critério dos
índices secundários em `db_diagram.md` §4: imperceptível agora, decisivo em escala.

### 2.4. Granularidade: um service por agregado, resources por rota

Vindo de um contexto de *controllers* (Spring MVC, NestJs...), o instinto natural seria reunir
tudo que envolve o usuário numa única classe (um `UserController`). Aqui as camadas
são fatiadas em eixos diferentes, de propósito:

- O **service** é organizado por **agregado de domínio**: existe um único
  `UserService` com a regra de negócio do usuário (gestão de alunos pelo admin e o
  self-service do próprio usuário).
- Os **resources** são organizados por **rota HTTP e autorização**: `/students`
  (CRUD de alunos, restrito ao admin) e `/me` (perfil do próprio autenticado) são
  dois `resource` distintos, ambos finos sobre o mesmo `UserService`.

O que empurra essa separação no resource não é uma regra do framework. O que decide é a
**escolha das URLs**: `/students` (específico do domínio, melhor que um genérico
`/users` para este desafio) e `/me` não compartilham prefixo, e no JAX-RS uma classe
tem um único `@Path` base. Mantendo essas URLs, duas classes é o encaixe natural.
Soma-se a isso o `@RolesAllowed("admin")` no nível da classe do `StudentResource`,
que funciona como rede de segurança (todo método é admin, sem exceção); numa classe
única, a autorização iria método a método.

Em resumo: o service unifica por domínio; os resources separam por rota e tranca.

---

## 3. Convenção de nomenclatura de pacotes

A regra que adotei para singular e plural é baseada no que o pacote **representa**:

- **Módulo de domínio: singular.** `user`, `course`, `enrollment`, `center`. O
  módulo representa o conceito (o domínio "usuário"), não uma coleção de coisas.
- **Camadas técnicas com classe principal única: singular.** `resource`,
  `service`, `repository`, `domain`, `mapper`. Normalmente tenho uma classe central
  por camada dentro do módulo.
- **Pacotes que são coleções de itens do mesmo tipo: plural.** `dtos`,
  `exceptions`. A pasta agrupa vários elementos da mesma natureza, e o plural
  comunica isso.

**O princípio acima da regra: consistência.** Mais importante do que qualquer
escolha específica entre singular e plural é aplicá-la de forma uniforme em todo o
projeto. Inconsistência de nomenclatura (um pacote no singular ao lado de outro no
plural sem critério) prejudica a leitura mais do que qualquer convenção individual.
Defini a regra acima justamente para eliminar a decisão caso a caso e garantir
uniformidade.

---

## 4. Separação entre entidade e DTO

Decidi que a API nunca expõe diretamente as entidades de persistência. Toda
entrada e saída passa por **objetos de transporte (DTOs)** dedicados, separados por
intenção:

- Objetos de **entrada** (por exemplo, os dados para criar um usuário) representam
  o que a API aceita receber.
- Objetos de **saída** (por exemplo, a representação de um usuário na resposta)
  representam o que a API devolve.

**Por que separei.** Acoplar o contrato público da API à estrutura interna de
persistência cria dois problemas. Primeiro, **expõe detalhes internos**: campos
sensíveis (como o hash de senha) ou colunas internas não devem vazar na resposta.
Segundo, **engessa a evolução**: qualquer mudança na entidade refletiria
automaticamente no contrato da API, e vice-versa, tornando difícil evoluir um lado
sem quebrar o outro. Separando entidade de DTO, mantenho o contrato da API e o
modelo de dados livres para evoluir de forma independente.

**Anti-padrão que evitei.** Receber e devolver a entidade diretamente nos
endpoints. É mais rápido de escrever no início, mas vaza a estrutura de
persistência para o mundo externo e mistura duas preocupações que têm motivos
distintos para mudar.

Modelei os DTOs como tipos imutáveis (records), adequados a objetos cuja única
função é transportar dados, sem comportamento.

### 4.1. Organização dos DTOs

Por ora, mantive os DTOs de entrada e saída de um domínio em um único pacote
`dtos`, diferenciados por nomes claros que expressam a intenção (sufixos de
requisição e de resposta). Considerei subdividir em pastas separadas para entrada e
saída e descartei para o escopo atual: com a quantidade de DTOs deste projeto, a
subdivisão adicionaria estrutura sem ganho real de clareza (princípio YAGNI). Se o
número de DTOs crescer, a separação passa a fazer sentido.

---

## 5. Mapeamento entre entidade e DTO: manual

Optei por fazer a conversão entre entidades e DTOs com **mapeadores manuais**, uma
classe de mapeamento por domínio, sem dependência de biblioteca de geração
automática.

**Por que manual.** Para o tamanho deste projeto, o mapeamento manual me dá o
melhor equilíbrio:

- **Sem mágica.** O código de conversão fica explícito e legível; não há geração em
  tempo de compilação para entender ou depurar. O que está escrito é exatamente o
  que executa.
- **Sem dependência adicional.** Não acrescento biblioteca nem configuração de
  build, mantendo o projeto mais enxuto.
- **Controle total.** Escrevo qualquer regra de conversão específica diretamente,
  sem precisar acomodar as convenções de uma ferramenta.

**Trade-off.** A geração automática de mapeadores (por exemplo, via uma biblioteca
dedicada) elimina o código repetitivo de conversão e brilha quando há um grande
volume de mapeamentos. O custo é uma dependência a mais e uma camada de código
gerado. Para o número de mapeamentos deste desafio, o boilerplate manual é pequeno
e o ganho da automação não compensa a complexidade adicional.

**Evolução possível.** Se o volume de mapeamentos crescer a ponto de o código
manual ficar repetitivo demais, a migração para geração automática é um refinamento
natural, e ter uma camada `mapper` dedicada desde o início torna essa troca
localizada.

---

## 6. Código compartilhado (`shared`)

Criei um pacote `shared` para o código genuinamente **transversal** a múltiplos
domínios. Seu primeiro uso é o tratamento padronizado de erros da API:

- Um tipo que define o **formato padrão de erro** que a API retorna, garantindo que
  toda falha tenha uma representação consistente para quem consome a API.
- Um tratador **global de exceções**, que captura as falhas e as converte para esse
  formato padrão, de modo que o tratamento de erro não fique espalhado e repetido
  pelos resources.

**Por que centralizei o tratamento de erro.** Uma API que responde erros em
formatos diferentes a cada endpoint é difícil de consumir. Centralizar o formato e
a conversão garante consistência e tira de cada ponto individual da API a
responsabilidade de tratar erro.

**Disciplina do `shared`.** O risco conhecido de um pacote compartilhado é virar um
depósito de tudo aquilo que não se sabe onde colocar, acumulando código sem coesão.
Por isso fui estrito: só entra em `shared` o que é de fato usado por mais de um
domínio e não pertence a nenhum deles em particular. Um utilitário usado por um
único domínio fica naquele domínio, não no `shared`. Essa disciplina evita que o
pacote compartilhado se torne um ponto de acoplamento entre partes que deveriam ser
independentes.

---

## 7. Configuração

Não criei um pacote de configuração antecipadamente. Deixei a configuração nascer
da necessidade real.

Essa decisão reflete uma característica do framework: grande parte do que, em
outros ecossistemas, exigiria classes de configuração dedicadas é resolvida por
arquivo de propriedades, e a segurança em endpoints é expressa por anotações
diretamente na camada de exposição. Por isso, criar um pacote de configuração com
classes-placeholder antes de existir configuração concreta seria estrutura vazia.

Quando a implementação de funcionalidades como a autenticação exigir configuração
que de fato precise de código (e não apenas de propriedades), crio o pacote de
configuração com conteúdo real. É o mesmo princípio de não antecipar estrutura sem
necessidade que apliquei em outras decisões do projeto.

---

## 8. Os módulos de domínio

A organização por domínio se concretiza nos módulos abaixo, que derivei
diretamente da modelagem (`db_diagram.md`) e do escopo (`planejamento_backend.md`):

- **`user`**: o usuário do sistema (administrador e aluno), com as operações de
  cadastro, listagem e exclusão de alunos. A entidade `User` é a base sobre a qual
  a autenticação opera (ver módulo `auth`).
- **`auth`**: a autenticação e a autorização (login, emissão e verificação do JWT,
  controle de acesso). Fica em módulo próprio, e não dentro de `user`, porque é uma
  preocupação distinta da gestão de usuários e com crescimento próprio (refresh
  token, recuperação de senha e rate limiting, na evolução). Depende de `user` (lê
  credenciais e papel pelo `UserRepository`), numa dependência acíclica `auth ->
  user`.
- **`course`**: o curso, com cadastro, listagem e exclusão.
- **`enrollment`**: a matrícula, que liga aluno e curso. Modelei como conceito
  próprio (e não como um simples relacionamento implícito) porque carrega seus
  próprios dados e regras, em especial a regra central de que um aluno não se
  matricula duas vezes no mesmo curso.
- **`center`**: o centro, tratado como **dado de referência**. Ver seção 8.1.

### 8.1. O módulo `center` e seu escopo reduzido

Tratei o `center` de forma deliberadamente mais enxuta que os demais módulos,
porque seu papel no sistema é diferente: ele é **dado de referência**, não uma
entidade de gestão ativa do administrador.

No escopo atual, o `center` tem:

- **Entidade**, para representar o conceito e ser referenciado pelos cursos.
- **Carga por seed**, ou seja, populei os centros como dado inicial (via migration),
  em vez de criá-los em tempo de execução.
- **Endpoint de listagem (somente leitura)**, cuja finalidade prática é permitir
  que o frontend obtenha a lista de centros, por exemplo para preencher um campo de
  seleção ao cadastrar um curso.

O que o `center` **não** tem, por ora, é o conjunto completo de operações de
escrita (criar, editar, excluir). Essa decisão é coerente com o fato de o desafio
não pedir gestão de centros e de eles serem, por natureza, dado de referência
estável.

**Evolução planejada.** Pretendo concluir o CRUD completo de centros caso haja
tempo, sem que sua ausência comprometa o núcleo do sistema. A estrutura do módulo
já acomoda esse crescimento: adicionar as operações de escrita é estender o que
existe, não reescrever. Esse faseamento segue a priorização explícita do projeto,
em que funcionalidades secundárias podem ficar simplificadas em favor da solidez do
núcleo.

### 8.2. O módulo `enrollment` e o desenho dos endpoints

Modelei matrícula e listagem como uma **subcoleção do curso**, e não como um recurso
de topo `/enrollments` nem como endpoints do lado do aluno:

- `GET /courses/{id}/students`: lista os alunos matriculados no curso.
- `POST /courses/{id}/students`: matricula um aluno (corpo: `{ studentId }`).

**Por que a partir do curso.** O desafio é admin-only: "matricular alunos em cursos" e
"listar alunos por curso" são ambas ações do administrador, não do aluno. Não existe um
aluno-agente neste escopo que justifique um path do lado do aluno, já que a automatrícula
não é pedida. A própria gramática dos dois requisitos aponta para o curso: "em cursos"
trata o curso como container de destino, e "por curso" é agrupamento por curso. Os dois
descrevem a relação a partir do curso; nenhum a descreve a partir do aluno.

**Escrita com um lado canônico.** A leitura de uma relação pode ser bidirecional sem
custo (são apenas views), mas a escrita não deve existir nos dois lados, ou haveria dois
endpoints mutando o mesmo vínculo. Por isso a matrícula tem um único dono, o curso. Expor
a mesma operação também em `/students/{id}/courses` seria duplicar a mutação.

**O lado do aluno, na Etapa 3, vira `GET /me/courses`.** Com a autenticação surge o agente
que faltava: o aluno autenticado vendo os próprios cursos. Realizei isso como `GET /me/courses`,
e não `GET /students/{id}/courses`: em um endpoint de posse, a identidade deve vir do token (claim
`sub`), não de um id no path, assim, o aluno só acessa o que é dele, sem precisar conferir se o
`{id}` da URL bate com o dono do token. O resource vive no módulo `enrollment` (a regra é de
matrícula), separado do `EnrollmentResource` porque a rota e a tranca diferem (`@Authenticated`,
não admin-only; ver §2.4). É leitura do lado do aluno, então não duplica a escrita, que continua
tendo o curso como dono único.

**O nesting na URL não acopla o domínio.** O path ser `/courses/{id}/students` é uma
decisão de superfície HTTP. A lógica e a regra central (um aluno não se matricula duas
vezes no mesmo curso) continuam no módulo `enrollment`; a resource que serve esse path
vive em `enrollment`, apenas com o `@Path` course-nested. URL e módulo de domínio são
camadas independentes.

---

## 9. Visão consolidada da estrutura

A árvore abaixo ilustra a organização resultante de todas as decisões acima.
Detalhei o módulo `user`; os demais seguem a mesma estrutura, respeitado o escopo
reduzido de `center` (seção 8.1).

```
(raiz de pacotes da aplicação)
├── shared/
│   └── exceptions/
│       ├── (formato padrão de erro da API)
│       └── (tratador global de exceções)
│
├── user/
│   ├── resource/
│   ├── service/
│   ├── repository/
│   ├── domain/
│   ├── mapper/
│   └── dtos/
│       ├── (objeto de entrada: criação de usuário)
│       └── (objeto de saída: resposta de usuário)
│
├── auth/
│   ├── resource/      (login, endpoint público)
│   ├── service/       (validação de credenciais e emissão do JWT)
│   └── dtos/
│
├── course/
│   └── (mesma estrutura de camadas)
│
├── enrollment/
│   └── (mesma estrutura de camadas)
│
└── center/
    ├── resource/      (apenas leitura, por ora)
    ├── service/
    ├── repository/
    └── domain/
```

O pacote de configuração não aparece na árvore por decisão da seção 7: vou criá-lo
quando houver configuração concreta que justifique sua existência.

---

## 10. Resumo das decisões

| Decisão | Escolha | Razão principal |
|---|---|---|
| Organização de pacotes | Por domínio (package-by-feature) | Coesão: o que muda junto fica junto. |
| Camada de exposição | `resource` (terminologia JAX-RS) | Aderência ao idioma do framework. |
| Acesso a dados | Padrão Repository | Separação de responsabilidades e testabilidade. |
| Nomenclatura | Módulo no singular, coleção no plural | Clareza e, acima de tudo, consistência. |
| Contrato da API | DTOs separados das entidades | Não vazar persistência nem engessar a evolução. |
| Mapeamento entidade/DTO | Manual | Simplicidade, sem dependência nem mágica. |
| Código transversal | Pacote `shared`, com disciplina estrita | Reúso real sem virar depósito. |
| Configuração | Criada sob demanda | Não antecipar estrutura vazia. |
| Módulo `center` | Entidade, seed e leitura; CRUD como evolução | Dado de referência, fora do escopo de gestão. |
