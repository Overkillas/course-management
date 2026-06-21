import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UserProfile } from './me.models';

/**
 * Self-service do usuário autenticado (/me): perfil e troca de senha.
 */
@Injectable({ providedIn: 'root' })
export class MeService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/me`;

  getProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(this.baseUrl);
  }

  // Troca a senha do próprio usuário. O backend responde 204 (sem corpo).
  changePassword(newPassword: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/password`, { newPassword });
  }
}
