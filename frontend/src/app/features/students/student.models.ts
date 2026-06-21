// Espelha o StudentResponse do backend. O mustChangePassword indica se o aluno ainda
// está no primeiro acesso (senha temporária definida pelo admin, não trocada).
export interface Student {
  id: number;
  name: string;
  email: string;
  mustChangePassword: boolean;
}

// Dados para o admin cadastrar um aluno (CreateStudentRequest do backend). A senha é
// a inicial/temporária: valida só tamanho (8 a 72), não a regra de senha forte (essa
// vale apenas na troca do primeiro acesso).
export interface CreateStudentRequest {
  name: string;
  email: string;
  password: string;
}
