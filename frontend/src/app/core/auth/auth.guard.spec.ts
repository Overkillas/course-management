import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { authGuard } from './auth.guard';

describe('authGuard', () => {
  let authMock: { claims: jasmine.Spy; logout: jasmine.Spy };
  let routerMock: { parseUrl: jasmine.Spy };

  beforeEach(() => {
    authMock = { claims: jasmine.createSpy('claims'), logout: jasmine.createSpy('logout') };
    routerMock = { parseUrl: jasmine.createSpy('parseUrl').and.callFake((url: string) => url) };

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authMock },
        { provide: Router, useValue: routerMock },
      ],
    });
  });

  function run(): unknown {
    return TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));
  }

  it('libera com token válido', () => {
    authMock.claims.and.returnValue({
      sub: '1',
      upn: 'x',
      groups: ['admin'],
      mustChangePassword: false,
      exp: 9999999999,
    });

    expect(run()).toBeTrue();
    expect(authMock.logout).not.toHaveBeenCalled();
  });

  it('redireciona para login sem sessão', () => {
    authMock.claims.and.returnValue(null);

    expect(run()).toBe('/login');
    expect(authMock.logout).toHaveBeenCalled();
  });

  it('redireciona para login com token expirado', () => {
    authMock.claims.and.returnValue({
      sub: '1',
      upn: 'x',
      groups: [],
      mustChangePassword: false,
      exp: 1000,
    });

    expect(run()).toBe('/login');
    expect(authMock.logout).toHaveBeenCalled();
  });
});
