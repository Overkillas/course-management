import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Course, CreateCourseRequest } from './course.models';

/**
 * Acesso à API de cursos (/courses), restrita ao admin: listar, cadastrar e excluir.
 */
@Injectable({ providedIn: 'root' })
export class CourseService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/courses`;

  list(): Observable<Course[]> {
    return this.http.get<Course[]>(this.baseUrl);
  }

  create(request: CreateCourseRequest): Observable<Course> {
    return this.http.post<Course>(this.baseUrl, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
