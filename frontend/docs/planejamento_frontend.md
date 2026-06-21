# Planejamento do Frontend

> Documento de planejamento e decisões do frontend para o desafio técnico
> (gerenciamento de cursos e alunos). O foco não é explicar Angular, e sim
> registrar **o que vou construir, por quê, e em que ordem**. É mais enxuto que a
> documentação do backend de propósito: o núcleo de regras de negócio vive no
> backend, e o frontend é a interface que o consome.
>
> O documento cresce de forma aditiva: a cada etapa concluída, registro aqui o que
> foi decidido nela, em vez de antecipar tudo de uma vez.

---

## 1. Visão geral

O frontend é a interface web que consome a API REST já pronta (ver `backend/`). Há
dois perfis de uso, espelhando os papéis do backend:

- **Admin:** gerencia alunos, cursos e matrículas, e lista os alunos matriculados
  por curso.
- **Aluno:** consulta o próprio perfil e as próprias matrículas, e troca a senha no
  primeiro acesso.

A API roda em `http://localhost:8080` (Swagger em `/q/docs`). O contrato consumido
está resumido no `README.md` da raiz.

---

## 2. Stack e por quê

| Escolha | Motivo resumido |
|---|---|
| **Angular** | Framework pedido para o frontend. SPA que conversa com a API por JSON. |
| **Standalone components + signals** | O estilo atual do Angular, com menos boilerplate que o modelo antigo de módulos. |
| **TypeScript em modo `strict`** | Erros de contrato (campo errado, tipo errado) aparecem em tempo de compilação, não em produção. |
| **Angular Material** | Biblioteca de componentes oficial. Entrega tabelas, formulários, dialogs e avisos (snackbars) prontos, o que acelera o CRUD e padroniza o visual. |
| **`HttpClient` com interceptors** | Cliente HTTP nativo. O interceptor é o ponto único onde anexo o token e trato erros de autenticação. |
| **Estado em services com signals** | Para o tamanho do projeto, um service singular guardando o estado de sessão basta. Uma biblioteca de estado dedicada seria peso sem retorno. |

---

## 3. Estrutura de pastas

A organização espelha a do backend: **por funcionalidade** (cada tela e seu código
ficam juntos), e não por tipo técnico de arquivo. Mantém coeso o que muda junto e
deixa as duas metades do repositório com a mesma lógica de navegação.

```
frontend/
├── docs/                  # este documento
└── src/
    ├── environments/      # endereço da API por ambiente
    └── app/
        ├── core/          # código transversal (o "shared" do backend)
        │   ├── auth/      # sessão, leitura do token, guards
        │   └── http/      # interceptor e tratamento de erro
        ├── features/      # uma pasta por tela (login, students, courses, ...)
        ├── app.routes.ts  # tabela de rotas
        └── app.config.ts  # configuração da aplicação
```

---

## 4. Telas e rotas

| Rota | Acesso | Tela |
|---|---|---|
| `/login` | público | Login |
| `/change-password` | autenticado (trava de 1º acesso) | Troca de senha |
| `/students` | admin | Lista, cadastro e exclusão de alunos |
| `/courses` | admin | Lista, cadastro e exclusão de cursos |
| `/courses/:id/students` | admin | Matriculados no curso e matrícula de aluno |
| `/me` | autenticado | Perfil do aluno |
| `/me/courses` | autenticado | Cursos do aluno |

A raiz redireciona conforme o papel: admin para `/students`, aluno para
`/me/courses`. Não há tela inicial separada porque o desafio não pede uma.

---

## 5. Autenticação

O login (`POST /auth/login`) devolve **apenas um token** (JWT). O papel do usuário e
a flag de troca de senha não vêm em campos separados: vivem dentro do token. Por
isso o frontend **lê (decodifica) o token** para saber o papel e se é o primeiro
acesso. Essa leitura serve só para a interface; quem de fato autoriza cada operação
é o backend.

- **Token guardado no `localStorage`**, para a sessão sobreviver a um recarregar de
  página.
- **Interceptor**: anexa o token em toda requisição protegida e, se a API responder
  `401`, encerra a sessão e leva ao login.
- **Guards** (filtros de rota): bloqueiam acesso sem token, com token expirado ou
  com papel errado.
- **Primeiro acesso**: se o token indicar que a senha precisa ser trocada, a única
  tela liberada é a de troca. Depois de trocar (`POST /me/password`), o token antigo
  continua bloqueado, então a tela encerra a sessão e pede um novo login. Refazer o
  login é o que entrega um token já liberado.

---

## 6. Decisões e trade-offs

Registro as escolhas que tinham alternativa real, com o motivo da decisão.

**Angular Material, em vez de CSS escrito do zero.** O Material entrega os
componentes que todo CRUD precisa (tabela, formulário, dialog, aviso) prontos e
consistentes, o que economiza tempo e mantém o foco na lógica.

**Trade-off.** É uma dependência a mais, com alguma curva da biblioteca. CSS do
zero daria controle total, mas gastaria tempo reconstruindo o que o Material já
resolve.

**CORS habilitado no backend, em vez do proxy do servidor de desenvolvimento.** O
servidor de desenvolvimento do Angular (porta 4200) e a API (porta 8080) são origens
diferentes, e o navegador bloqueia essa chamada por padrão (CORS). Optei por liberar
o CORS no próprio backend para `localhost:4200`.

**Alternativa descartada.** Um proxy só de desenvolvimento evitaria mexer no
backend, mas escolhi a configuração explícita no servidor por ser a que também vale
para a entrega.

**Token no `localStorage`, em vez de só em memória.** O `localStorage` mantém o
usuário logado depois de recarregar a página. Como o backend ainda não tem
renovação de token (refresh), guardar só em memória faria a sessão cair a cada
recarregamento, um atrito grande.

**Trade-off.** O `localStorage` fica exposto a ataques de script (XSS).

**Leitura do token sem biblioteca.** Ler os dados do token é uma função curta, então
não adicionei uma dependência só para isso. A leitura é só para a interface e não
verifica a assinatura, porque essa verificação é responsabilidade do backend.

---

## 7. Ordem das etapas

Cada etapa termina com a aplicação subindo e funcionando, no mesmo espírito do
backend.

1. **Fundação.** Criação do projeto, configuração de ambiente e do Material.
2. **Autenticação.** Login, leitura do token, guarda da sessão, interceptor e
   guards. Inclui habilitar o CORS no backend.
3. **Primeiro acesso.** Tela de troca de senha e a trava que a torna obrigatória.
4. **Núcleo do admin.** Alunos, cursos e matrículas.
5. **Self-service do aluno.** Perfil e próprias matrículas.
6. **Acabamento.** Padronização de erros, estados de carregamento e navegação por
   papel.
7. **Entrega.** Empacotamento em Docker e integração final ao repositório.
</content>
