import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { StudentService } from '../student.service';
import { StudentEditDialog } from './student-edit-dialog';

describe('StudentEditDialog', () => {
  let serviceMock: { updateName: jasmine.Spy };
  let dialogRefMock: { close: jasmine.Spy };
  const student = { id: 7, name: 'Maria', email: 'maria@x.com', mustChangePassword: false };

  beforeEach(async () => {
    serviceMock = { updateName: jasmine.createSpy('updateName') };
    dialogRefMock = { close: jasmine.createSpy('close') };

    await TestBed.configureTestingModule({
      imports: [StudentEditDialog],
      providers: [
        { provide: StudentService, useValue: serviceMock },
        { provide: MatDialogRef, useValue: dialogRefMock },
        { provide: MAT_DIALOG_DATA, useValue: student },
      ],
    }).compileComponents();
  });

  function createComponent() {
    return TestBed.createComponent(StudentEditDialog).componentInstance;
  }

  it('pré-preenche o nome do aluno', () => {
    expect(createComponent().form.controls.name.value).toBe('Maria');
  });

  it('não salva quando o nome é inválido', () => {
    const component = createComponent();
    component.form.controls.name.setValue('');
    component.submit();
    expect(serviceMock.updateName).not.toHaveBeenCalled();
  });

  it('fecha com o aluno atualizado no sucesso', () => {
    const updated = { ...student, name: 'Maria Souza' };
    serviceMock.updateName.and.returnValue(of(updated));
    const component = createComponent();
    component.form.controls.name.setValue('Maria Souza');

    component.submit();

    expect(serviceMock.updateName).toHaveBeenCalledWith(7, 'Maria Souza');
    expect(dialogRefMock.close).toHaveBeenCalledWith(updated);
  });

  it('mostra erro quando o aluno não existe mais (404)', () => {
    serviceMock.updateName.and.returnValue(throwError(() => new HttpErrorResponse({ status: 404 })));
    const component = createComponent();
    component.form.controls.name.setValue('Maria Souza');

    component.submit();

    expect(component.errorMessage()).toBe('Este aluno não existe mais. Atualize a lista.');
    expect(dialogRefMock.close).not.toHaveBeenCalled();
  });
});
