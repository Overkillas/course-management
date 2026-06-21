// Confirmação da matrícula criada (EnrollmentResponse do backend). Só os ids do
// vínculo e o instante; os dados de aluno/curso já estão em mãos na tela.
export interface Enrollment {
  id: number;
  studentId: number;
  courseId: number;
  createdAt: string;
}
