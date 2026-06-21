import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthClaims, LoginRequest, LoginResponse, Role } from './auth.models';
import { decodeClaims } from './jwt.util';

// Chave do token no localStorage.
const TOKEN_KEY = 'auth.token';

/**
 * Service do agregado de autenticação (análogo a um @Service único por domínio no
 * backend). Cuida do estado de sessão e da chamada de login.
 *
 * O token é a fonte única do estado: tudo o mais (claims, papel, primeiro acesso)
 * deriva dele por signals. O token é guardado no localStorage para a sessão
 * sobreviver a um recarregar de página.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  // Fonte do estado: o token, hidratado do localStorage na criação do service.
  private readonly _token = signal<string | null>(localStorage.getItem(TOKEN_KEY));

  readonly token = this._token.asReadonly();

  readonly claims = computed<AuthClaims | null>(() => {
    const token = this._token();
    return token ? decodeClaims(token) : null;
  });

  readonly isAuthenticated = computed<boolean>(() => this.claims() !== null);

  readonly role = computed<Role | null>(() => {
    const groups = this.claims()?.groups ?? [];
    if (groups.includes('admin')) {
      return 'admin';
    }
    if (groups.includes('aluno')) {
      return 'aluno';
    }
    return null;
  });

  readonly mustChangePassword = computed<boolean>(() => this.claims()?.mustChangePassword === true);

  // Autentica e, no sucesso, guarda o token (localStorage + signal).
  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${environment.apiUrl}/auth/login`, credentials)
      .pipe(tap((response) => this.setSession(response.token)));
  }

  // Encerra a sessão, limpando o token guardado.
  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    this._token.set(null);
  }

  private setSession(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
    this._token.set(token);
  }
}
