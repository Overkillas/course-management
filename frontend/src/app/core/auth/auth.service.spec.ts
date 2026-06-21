import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

const TOKEN_KEY = 'auth.token';
const LOGIN_URL = `${environment.apiUrl}/auth/login`;

// Monta um JWT de mentira com o payload em base64url (a assinatura é irrelevante).
function makeToken(payload: object): string {
  const header = btoa(JSON.stringify({ alg: 'RS256', typ: 'JWT' }));
  const body = btoa(JSON.stringify(payload))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '');
  return `${header}.${body}.assinatura`;
}

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('guarda o token e expõe a sessão decodificada no login', () => {
    const token = makeToken({
      sub: '1',
      upn: 'admin@unifor.br',
      groups: ['admin'],
      mustChangePassword: false,
      exp: 1893456000,
    });

    let emitted = false;
    service.login({ email: 'admin@unifor.br', password: 'Ab!12345' }).subscribe(() => (emitted = true));

    const req = httpMock.expectOne(LOGIN_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'admin@unifor.br', password: 'Ab!12345' });
    req.flush({ token });

    expect(emitted).toBeTrue();
    expect(service.isAuthenticated()).toBeTrue();
    expect(service.role()).toBe('admin');
    expect(service.mustChangePassword()).toBeFalse();
    expect(localStorage.getItem(TOKEN_KEY)).toBe(token);
  });

  it('limpa a sessão no logout', () => {
    const token = makeToken({
      sub: '2',
      upn: 'aluno@edu.unifor.br',
      groups: ['aluno'],
      mustChangePassword: true,
      exp: 1893456000,
    });

    service.login({ email: 'aluno@edu.unifor.br', password: 'x' }).subscribe();
    httpMock.expectOne(LOGIN_URL).flush({ token });

    expect(service.isAuthenticated()).toBeTrue();
    expect(service.role()).toBe('aluno');
    expect(service.mustChangePassword()).toBeTrue();

    service.logout();

    expect(service.isAuthenticated()).toBeFalse();
    expect(service.role()).toBeNull();
    expect(localStorage.getItem(TOKEN_KEY)).toBeNull();
  });

  it('hidrata a sessão a partir do localStorage na criação', () => {
    const token = makeToken({
      sub: '1',
      upn: 'admin@unifor.br',
      groups: ['admin'],
      mustChangePassword: false,
      exp: 1893456000,
    });

    TestBed.resetTestingModule();
    localStorage.setItem(TOKEN_KEY, token);
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);

    expect(service.isAuthenticated()).toBeTrue();
    expect(service.role()).toBe('admin');
  });
});
