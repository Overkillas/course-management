import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { mustChangePasswordGuard } from './must-change-password.guard';

describe('mustChangePasswordGuard', () => {
  let authMock: { mustChangePassword: jasmine.Spy };
  let routerMock: { parseUrl: jasmine.Spy };

  beforeEach(() => {
    authMock = { mustChangePassword: jasmine.createSpy('mustChangePassword') };
    routerMock = { parseUrl: jasmine.createSpy('parseUrl').and.callFake((url: string) => url) };

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authMock },
        { provide: Router, useValue: routerMock },
      ],
    });
  });

  function run(): unknown {
    return TestBed.runInInjectionContext(() => mustChangePasswordGuard({} as never, {} as never));
  }

  it('redireciona para a troca quando é primeiro acesso', () => {
    authMock.mustChangePassword.and.returnValue(true);
    expect(run()).toBe('/change-password');
  });

  it('libera quando não precisa trocar a senha', () => {
    authMock.mustChangePassword.and.returnValue(false);
    expect(run()).toBeTrue();
  });
});
