import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Student } from '../students/student.models';
import { Enrollment } from './enrollment.models';

/**
 * Acesso à matrícula como subcoleção do curso (/courses/{id}/students), restrita ao
 * admin: listar os matriculados e matricular um aluno. A listagem reaproveita o model
 * Student (o backend devolve StudentResponse).
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
}
