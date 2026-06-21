import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { StudentService } from '../../students/student.service';
import { EnrollmentService } from '../enrollment.service';
import { EnrollStudentDialog } from './enroll-student-dialog';

describe('EnrollStudentDialog', () => {
  let studentServiceMock: { list: jasmine.Spy };
  let enrollmentServiceMock: { enroll: jasmine.Spy };
  let dialogRefMock: { close: jasmine.Spy };

  const ana = { id: 1, name: 'Ana', email: 'ana@x.com', mustChangePassword: false };
  const bia = { id: 2, name: 'Bia', email: 'bia@x.com', mustChangePassword: false };
  const caio = { id: 3, name: 'Caio', email: 'caio@x.com', mustChangePassword: false };

  async function setup(enrolledStudentIds: number[]) {
    studentServiceMock = { list: jasmine.createSpy('list').and.returnValue(of([ana, bia, caio])) };
    enrollmentServiceMock = { enroll: jasmine.createSpy('enroll') };
    dialogRefMock = { close: jasmine.createSpy('close') };

    await TestBed.configureTestingModule({
      imports: [EnrollStudentDialog],
      providers: [
        { provide: StudentService, useValue: studentServiceMock },
        { provide: EnrollmentService, useValue: enrollmentServiceMock },
        { provide: MatDialogRef, useValue: dialogRefMock },
        { provide: MAT_DIALOG_DATA, useValue: { courseId: 10, enrolledStudentIds } },
      ],
    }).compileComponents();

    return TestBed.createComponent(EnrollStudentDialog).componentInstance;
  }

  it('esconde os alunos já matriculados', async () => {
    const component = await setup([2]);
    expect(component.available()).toEqual([ana, caio]);
    expect(component.filtered()).toEqual([ana, caio]);
  });

  it('não matricula sem um aluno selecionado', async () => {
    const component = await setup([]);
    component.submit();
    expect(enrollmentServiceMock.enroll).not.toHaveBeenCalled();
    expect(component.errorMessage()).toBe('Selecione um aluno da lista.');
  });

  it('matricula o aluno selecionado e fecha com sucesso', async () => {
    const component = await setup([]);
    enrollmentServiceMock.enroll.and.returnValue(
      of({ id: 1, studentId: 1, courseId: 10, createdAt: 'x' }),
    );
    component.control.setValue(ana);

    component.submit();

    expect(enrollmentServiceMock.enroll).toHaveBeenCalledWith(10, 1);
    expect(dialogRefMock.close).toHaveBeenCalledWith(true);
  });

  it('mostra erro de matrícula duplicada no 409', async () => {
    const component = await setup([]);
    enrollmentServiceMock.enroll.and.returnValue(throwError(() => new HttpErrorResponse({ status: 409 })));
    component.control.setValue(bia);

    component.submit();

    expect(component.errorMessage()).toBe('Este aluno já está matriculado neste curso.');
    expect(dialogRefMock.close).not.toHaveBeenCalled();
  });
});
