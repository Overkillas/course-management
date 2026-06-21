import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { MeService } from '../../core/me/me.service';
import { ChangePassword } from './change-password';

describe('ChangePassword', () => {
  let meMock: { changePassword: jasmine.Spy };
  let authMock: { logout: jasmine.Spy };
  let routerMock: { navigateByUrl: jasmine.Spy };
  let snackMock: { open: jasmine.Spy };

  beforeEach(async () => {
    meMock = { changePassword: jasmine.createSpy('changePassword') };
    authMock = { logout: jasmine.createSpy('logout') };
    routerMock = { navigateByUrl: jasmine.createSpy('navigateByUrl') };
    snackMock = { open: jasmine.createSpy('open') };

    await TestBed.configureTestingModule({
      imports: [ChangePassword],
      providers: [
        { provide: MeService, useValue: meMock },
        { provide: AuthService, useValue: authMock },
        { provide: Router, useValue: routerMock },
        { provide: MatSnackBar, useValue: snackMock },
      ],
    }).compileComponents();
  });

  function createComponent() {
    const fixture = TestBed.createComponent(ChangePassword);
    fixture.detectChanges();
    return fixture.componentInstance;
  }

  it('cria o componente', () => {
    expect(createComponent()).toBeTruthy();
  });

  it('não chama a troca quando o formulário é inválido', () => {
    const component = createComponent();
    component.submit();
    expect(meMock.changePassword).not.toHaveBeenCalled();
  });

  it('não chama a troca quando as senhas não coincidem', () => {
    const component = createComponent();
    component.form.setValue({ newPassword: 'Senha!123', confirmPassword: 'Outra!123' });
    component.submit();
    expect(meMock.changePassword).not.toHaveBeenCalled();
  });

  it('troca a senha, desloga e volta ao login no sucesso', () => {
    meMock.changePassword.and.returnValue(of(undefined));
    const component = createComponent();
    component.form.setValue({ newPassword: 'Senha!123', confirmPassword: 'Senha!123' });

    component.submit();

    expect(meMock.changePassword).toHaveBeenCalledWith('Senha!123');
    expect(authMock.logout).toHaveBeenCalled();
    expect(snackMock.open).toHaveBeenCalled();
    expect(routerMock.navigateByUrl).toHaveBeenCalledWith('/login');
  });

  it('mostra mensagem de erro quando a troca falha', () => {
    meMock.changePassword.and.returnValue(throwError(() => new HttpErrorResponse({ status: 400 })));
    const component = createComponent();
    component.form.setValue({ newPassword: 'Senha!123', confirmPassword: 'Senha!123' });

    component.submit();

    expect(component.errorMessage()).toBe('Não foi possível trocar a senha. Tente novamente.');
    expect(authMock.logout).not.toHaveBeenCalled();
    expect(routerMock.navigateByUrl).not.toHaveBeenCalled();
  });
});
