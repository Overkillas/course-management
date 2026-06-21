import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { Login } from './login';

describe('Login', () => {
  let authMock: {
    login: jasmine.Spy;
    role: jasmine.Spy;
    mustChangePassword: jasmine.Spy;
  };
  let routerMock: { navigateByUrl: jasmine.Spy };

  beforeEach(async () => {
    authMock = {
      login: jasmine.createSpy('login'),
      role: jasmine.createSpy('role').and.returnValue('admin'),
      mustChangePassword: jasmine.createSpy('mustChangePassword').and.returnValue(false),
    };
    routerMock = { navigateByUrl: jasmine.createSpy('navigateByUrl') };

    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        { provide: AuthService, useValue: authMock },
        { provide: Router, useValue: routerMock },
      ],
    }).compileComponents();
  });

  function createComponent() {
    const fixture = TestBed.createComponent(Login);
    fixture.detectChanges();
    return fixture;
  }

  it('cria o componente', () => {
    expect(createComponent().componentInstance).toBeTruthy();
  });

  it('não chama o login quando o formulário é inválido', () => {
    const component = createComponent().componentInstance;
    component.submit();
    expect(authMock.login).not.toHaveBeenCalled();
  });

  it('chama o login e redireciona por papel no sucesso', () => {
    authMock.login.and.returnValue(of({ token: 'x' }));
    const component = createComponent().componentInstance;
    component.form.setValue({ email: 'admin@unifor.br', password: 'Ab!12345' });

    component.submit();

    expect(authMock.login).toHaveBeenCalledWith({ email: 'admin@unifor.br', password: 'Ab!12345' });
    expect(routerMock.navigateByUrl).toHaveBeenCalledWith('/students');
  });

  it('redireciona para a troca de senha quando é primeiro acesso', () => {
    authMock.login.and.returnValue(of({ token: 'x' }));
    authMock.mustChangePassword.and.returnValue(true);
    const component = createComponent().componentInstance;
    component.form.setValue({ email: 'aluno@edu.unifor.br', password: 'temp1234' });

    component.submit();

    expect(routerMock.navigateByUrl).toHaveBeenCalledWith('/change-password');
  });

  it('mostra mensagem de erro em credenciais inválidas (401)', () => {
    authMock.login.and.returnValue(throwError(() => new HttpErrorResponse({ status: 401 })));
    const component = createComponent().componentInstance;
    component.form.setValue({ email: 'admin@unifor.br', password: 'errada' });

    component.submit();

    expect(component.errorMessage()).toBe('E-mail ou senha inválidos.');
    expect(routerMock.navigateByUrl).not.toHaveBeenCalled();
  });
});
