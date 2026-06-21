// Papel do usuário, como vem no claim "groups" do JWT (ver AuthService do backend).
export type Role = 'admin' | 'aluno';

// Claims que o backend coloca no JWT. O front decodifica isto só para uso de UI
// (decidir papel, primeiro acesso e redirecionamento); quem valida o token de fato
// é o backend. Por isso aqui não há verificação de assinatura.
export interface AuthClaims {
  sub: string; // id do usuário
  upn: string; // email
  groups: string[]; // papéis; na prática um único ("admin" ou "aluno")
  mustChangePassword: boolean; // trava de primeiro acesso
  exp: number; // expiração (epoch em segundos)
}

// Corpo enviado ao POST /auth/login.
export interface LoginRequest {
  email: string;
  password: string;
}

// Resposta do login: só o token (papel e mustChangePassword vivem dentro dele).
export interface LoginResponse {
  token: string;
}
