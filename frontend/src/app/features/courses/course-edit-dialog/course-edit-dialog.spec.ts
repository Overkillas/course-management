import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { Course } from '../course.models';
import { CourseService } from '../course.service';
import { CourseEditDialog } from './course-edit-dialog';

describe('CourseEditDialog', () => {
  let serviceMock: { updateName: jasmine.Spy };
  let dialogRefMock: { close: jasmine.Spy };
  const course: Course = {
    id: 5,
    name: 'Computação',
    totalSemesters: 8,
    center: { id: 1, code: 'CCT' },
    studentCount: 3,
  };

  beforeEach(async () => {
    serviceMock = { updateName: jasmine.createSpy('updateName') };
    dialogRefMock = { close: jasmine.createSpy('close') };

    await TestBed.configureTestingModule({
      imports: [CourseEditDialog],
      providers: [
        { provide: CourseService, useValue: serviceMock },
        { provide: MatDialogRef, useValue: dialogRefMock },
        { provide: MAT_DIALOG_DATA, useValue: course },
      ],
    }).compileComponents();
  });

  function createComponent() {
    return TestBed.createComponent(CourseEditDialog).componentInstance;
  }

  it('pré-preenche o nome do curso', () => {
    expect(createComponent().form.controls.name.value).toBe('Computação');
  });

  it('não salva quando o nome tem menos de 3 caracteres', () => {
    const component = createComponent();
    component.form.controls.name.setValue('CC');
    component.submit();
    expect(component.form.controls.name.hasError('minlength')).toBeTrue();
    expect(serviceMock.updateName).not.toHaveBeenCalled();
  });

  it('fecha com o curso atualizado no sucesso', () => {
    const updated = { ...course, name: 'Ciência da Computação' };
    serviceMock.updateName.and.returnValue(of(updated));
    const component = createComponent();
    component.form.controls.name.setValue('Ciência da Computação');

    component.submit();

    expect(serviceMock.updateName).toHaveBeenCalledWith(5, 'Ciência da Computação');
    expect(dialogRefMock.close).toHaveBeenCalledWith(updated);
  });

  it('mostra erro quando o curso não existe mais (404)', () => {
    serviceMock.updateName.and.returnValue(throwError(() => new HttpErrorResponse({ status: 404 })));
    const component = createComponent();
    component.form.controls.name.setValue('Ciência da Computação');

    component.submit();

    expect(component.errorMessage()).toBe('Este curso não existe mais. Atualize a lista.');
    expect(dialogRefMock.close).not.toHaveBeenCalled();
  });
});
