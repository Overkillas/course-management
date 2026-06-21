import { Center } from './center.models';

// Espelha o CourseResponse do backend, com o centro aninhado (id + sigla).
export interface Course {
  id: number;
  name: string;
  totalSemesters: number;
  center: Center;
}

// Dados para cadastrar um curso (CreateCourseRequest do backend).
export interface CreateCourseRequest {
  name: string;
  centerId: number;
  totalSemesters: number;
}
