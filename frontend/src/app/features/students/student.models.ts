// Espelha o StudentResponse do backend. O mustChangePassword indica se o aluno ainda
// está no primeiro acesso (senha temporária definida pelo admin, não trocada).
export interface Student {
  id: number;
  name: string;
  email: string;
  mustChangePassword: boolean;
}
