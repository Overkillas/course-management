import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from '../core/auth/auth.service';
import { MeService } from '../core/me/me.service';
import { Shell } from './shell';

describe('Shell', () => {
  let authMock: { claims: jasmine.Spy; role: jasmine.Spy; logout: jasmine.Spy };
  let meMock: { getProfile: jasmine.Spy };

  beforeEach(async () => {
    authMock = {
      claims: jasmine.createSpy('claims').and.returnValue({ upn: 'admin@unifor.br' }),
      role: jasmine.createSpy('role').and.returnValue('admin'),
      logout: jasmine.createSpy('logout'),
    };
    meMock = {
      getProfile: jasmine
        .createSpy('getProfile')
        .and.returnValue(of({ id: 1, name: 'Admin', email: 'admin@unifor.br' })),
    };

    await TestBed.configureTestingModule({
      imports: [Shell],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authMock },
        { provide: MeService, useValue: meMock },
      ],
    }).compileComponents();
  });

  it('cria o shell e carrega o nome do usuário', () => {
    const fixture = TestBed.createComponent(Shell);
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeTruthy();
    expect(fixture.componentInstance.userName()).toBe('Admin');
  });

  it('encerra a sessão e volta ao login no logout', () => {
    const fixture = TestBed.createComponent(Shell);
    const router = TestBed.inject(Router);
    const navigateSpy = spyOn(router, 'navigateByUrl');
    fixture.detectChanges();

    fixture.componentInstance.logout();

    expect(authMock.logout).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith('/login');
  });
});
