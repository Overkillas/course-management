// Perfil do próprio usuário autenticado (UserResponse do backend). Só dados de
// exibição: papel e mustChangePassword vivem no token, não aqui.
export interface UserProfile {
  id: number;
  name: string;
  email: string;
}
