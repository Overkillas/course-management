import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CreateStudentRequest, Student } from './student.models';

/**
 * Acesso à API de alunos (/students), restrita ao admin: listar, cadastrar e excluir.
 */
@Injectable({ providedIn: 'root' })
export class StudentService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/students`;

  list(): Observable<Student[]> {
    return this.http.get<Student[]>(this.baseUrl);
  }

  create(request: CreateStudentRequest): Observable<Student> {
    return this.http.post<Student>(this.baseUrl, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
