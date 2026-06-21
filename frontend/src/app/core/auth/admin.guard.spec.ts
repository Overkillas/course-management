import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { adminGuard } from './admin.guard';

describe('adminGuard', () => {
  let authMock: { role: jasmine.Spy };
  let routerMock: { parseUrl: jasmine.Spy };

  beforeEach(() => {
    authMock = { role: jasmine.createSpy('role') };
    routerMock = { parseUrl: jasmine.createSpy('parseUrl').and.callFake((url: string) => url) };

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authMock },
        { provide: Router, useValue: routerMock },
      ],
    });
  });

  function run(): unknown {
    return TestBed.runInInjectionContext(() => adminGuard({} as never, {} as never));
  }

  it('libera o admin', () => {
    authMock.role.and.returnValue('admin');
    expect(run()).toBeTrue();
  });

  it('redireciona o não-admin para a área do aluno', () => {
    authMock.role.and.returnValue('aluno');
    expect(run()).toBe('/me/courses');
  });
});
