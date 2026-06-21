import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Course } from '../courses/course.models';
import { Student } from '../students/student.models';
import { Enrollment } from './enrollment.models';

/**
 * Acesso à matrícula. Do lado do admin, como subcoleção do curso
 * (/courses/{id}/students): listar matriculados e matricular. Do lado do aluno, os
 * próprios cursos (/me/courses). A listagem de matriculados reaproveita o model
 * Student e a de cursos reaproveita Course (o backend devolve esses DTOs).
 */
@Injectable({ providedIn: 'root' })
export class EnrollmentService {
  private readonly http = inject(HttpClient);

  private studentsUrl(courseId: number): string {
    return `${environment.apiUrl}/courses/${courseId}/students`;
  }

  listStudents(courseId: number): Observable<Student[]> {
    return this.http.get<Student[]>(this.studentsUrl(courseId));
  }

  enroll(courseId: number, studentId: number): Observable<Enrollment> {
    return this.http.post<Enrollment>(this.studentsUrl(courseId), { studentId });
  }

  // Cursos em que o próprio aluno autenticado está matriculado (GET /me/courses).
  myCourses(): Observable<Course[]> {
    return this.http.get<Course[]>(`${environment.apiUrl}/me/courses`);
  }
}
