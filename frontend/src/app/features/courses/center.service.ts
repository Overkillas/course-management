import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Center } from './center.models';

/**
 * Acesso à lista de centros (/centers), dado de referência somente-leitura. Co-locado
 * em courses porque o único consumidor é o formulário de cadastro de curso.
 */
@Injectable({ providedIn: 'root' })
export class CenterService {
  private readonly http = inject(HttpClient);

  list(): Observable<Center[]> {
    return this.http.get<Center[]>(`${environment.apiUrl}/centers`);
  }
}
