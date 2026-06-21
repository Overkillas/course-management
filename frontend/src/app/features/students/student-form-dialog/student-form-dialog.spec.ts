import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { MatDialogRef } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { StudentService } from '../student.service';
import { StudentFormDialog } from './student-form-dialog';

describe('StudentFormDialog', () => {
  let serviceMock: { create: jasmine.Spy };
  let dialogRefMock: { close: jasmine.Spy };

  beforeEach(async () => {
    serviceMock = { create: jasmine.createSpy('create') };
    dialogRefMock = { close: jasmine.createSpy('close') };

    await TestBed.configureTestingModule({
      imports: [StudentFormDialog],
      providers: [
        { provide: StudentService, useValue: serviceMock },
        { provide: MatDialogRef, useValue: dialogRefMock },
      ],
    }).compileComponents();
  });

  function createComponent() {
    return TestBed.createComponent(StudentFormDialog).componentInstance;
  }

  it('não cadastra quando o formulário é inválido', () => {
    const component = createComponent();
    component.submit();
    expect(serviceMock.create).not.toHaveBeenCalled();
  });

  it('fecha o dialog com o aluno criado no sucesso', () => {
    const student = { id: 1, name: 'Maria', email: 'maria@x.com', mustChangePassword: true };
    serviceMock.create.and.returnValue(of(student));
    const component = createComponent();
    component.form.setValue({ name: 'Maria', email: 'maria@x.com', password: 'Temp1234' });

    component.submit();

    expect(serviceMock.create).toHaveBeenCalledWith({
      name: 'Maria',
      email: 'maria@x.com',
      password: 'Temp1234',
    });
    expect(dialogRefMock.close).toHaveBeenCalledWith(student);
  });

  it('mostra erro de e-mail duplicado no 409', () => {
    serviceMock.create.and.returnValue(throwError(() => new HttpErrorResponse({ status: 409 })));
    const component = createComponent();
    component.form.setValue({ name: 'Maria', email: 'maria@x.com', password: 'Temp1234' });

    component.submit();

    expect(component.errorMessage()).toBe('Já existe um aluno com este e-mail.');
    expect(dialogRefMock.close).not.toHaveBeenCalled();
  });
});
