import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Student } from './student.models';

/**
 * Acesso à API de alunos (/students), restrita ao admin. Por enquanto só a listagem
 */
@Injectable({ providedIn: 'root' })
export class StudentService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/students`;

  list(): Observable<Student[]> {
    return this.http.get<Student[]>(this.baseUrl);
  }
}
