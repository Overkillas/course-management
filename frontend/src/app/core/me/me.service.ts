import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

/**
 * Self-service do usuário autenticado (/me). Por ora só a troca de senha
 */
@Injectable({ providedIn: 'root' })
export class MeService {
  private readonly http = inject(HttpClient);

  // Troca a senha do próprio usuário. O backend responde 204 (sem corpo).
  changePassword(newPassword: string): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/me/password`, { newPassword });
  }
}
