import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of, throwError } from 'rxjs';
import { EnrollmentService } from './enrollment.service';
import { CourseStudents } from './course-students';

describe('CourseStudents', () => {
  let serviceMock: { listStudents: jasmine.Spy; enroll: jasmine.Spy };
  let dialogMock: { open: jasmine.Spy };
  let snackMock: { open: jasmine.Spy };

  const student = { id: 1, name: 'Ana', email: 'ana@x.com', mustChangePassword: false };

  beforeEach(async () => {
    serviceMock = {
      listStudents: jasmine.createSpy('listStudents').and.returnValue(of([])),
      enroll: jasmine.createSpy('enroll'),
    };
    dialogMock = { open: jasmine.createSpy('open') };
    snackMock = { open: jasmine.createSpy('open') };

    await TestBed.configureTestingModule({
      imports: [CourseStudents],
      providers: [
        { provide: EnrollmentService, useValue: serviceMock },
        { provide: MatDialog, useValue: dialogMock },
        { provide: MatSnackBar, useValue: snackMock },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ courseId: '7' }) } },
        },
      ],
    }).compileComponents();
  });

  function createComponent() {
    return TestBed.createComponent(CourseStudents).componentInstance;
  }

  it('carrega os matriculados do curso na criação', () => {
    serviceMock.listStudents.and.returnValue(of([student]));
    const component = createComponent();
    expect(serviceMock.listStudents).toHaveBeenCalledWith(7);
    expect(component.students()).toEqual([student]);
    expect(component.loading()).toBeFalse();
  });

  it('sinaliza erro quando a listagem falha', () => {
    serviceMock.listStudents.and.returnValue(throwError(() => new Error('fail')));
    const component = createComponent();
    expect(component.error()).toBeTrue();
    expect(component.loading()).toBeFalse();
  });

  it('recarrega a lista após matricular', () => {
    dialogMock.open.and.returnValue({ afterClosed: () => of(true) });
    const component = createComponent();
    serviceMock.listStudents.calls.reset();

    component.openEnroll();

    expect(serviceMock.listStudents).toHaveBeenCalled();
    expect(snackMock.open).toHaveBeenCalled();
  });
});
