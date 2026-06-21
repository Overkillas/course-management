import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { AuthService } from '../core/auth/auth.service';
import { Shell } from './shell';

describe('Shell', () => {
  let authMock: { claims: jasmine.Spy; role: jasmine.Spy; logout: jasmine.Spy };

  beforeEach(async () => {
    authMock = {
      claims: jasmine.createSpy('claims').and.returnValue({ upn: 'admin@unifor.br' }),
      role: jasmine.createSpy('role').and.returnValue('admin'),
      logout: jasmine.createSpy('logout'),
    };

    await TestBed.configureTestingModule({
      imports: [Shell],
      providers: [provideRouter([]), { provide: AuthService, useValue: authMock }],
    }).compileComponents();
  });

  it('cria o shell', () => {
    const fixture = TestBed.createComponent(Shell);
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeTruthy();
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
