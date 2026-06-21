import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { AuthService } from '../auth/auth.service';
import { tokenInterceptor } from './token.interceptor';

describe('tokenInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let authMock: { token: () => string | null; logout: jasmine.Spy };
  let routerMock: { navigateByUrl: jasmine.Spy };
  let currentToken: string | null;

  beforeEach(() => {
    currentToken = 'abc';
    authMock = { token: () => currentToken, logout: jasmine.createSpy('logout') };
    routerMock = { navigateByUrl: jasmine.createSpy('navigateByUrl') };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([tokenInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authMock },
        { provide: Router, useValue: routerMock },
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('anexa o token Bearer quando há sessão', () => {
    http.get('/api/x').subscribe();
    const req = httpMock.expectOne('/api/x');
    expect(req.request.headers.get('Authorization')).toBe('Bearer abc');
    req.flush({});
  });

  it('não anexa header quando não há token', () => {
    currentToken = null;
    http.get('/api/x').subscribe();
    const req = httpMock.expectOne('/api/x');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('não anexa token na chamada de /auth/login', () => {
    http.post(`${environment.apiUrl}/auth/login`, {}).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('encerra a sessão e redireciona no 401', () => {
    let errored = false;
    http.get('/api/y').subscribe({ error: () => (errored = true) });
    httpMock.expectOne('/api/y').flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(authMock.logout).toHaveBeenCalled();
    expect(routerMock.navigateByUrl).toHaveBeenCalledWith('/login');
    expect(errored).toBeTrue();
  });
});
