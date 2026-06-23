import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { Course } from './course.models';
import { CourseService } from './course.service';
import { CourseList } from './course-list';

describe('CourseList', () => {
  let serviceMock: { list: jasmine.Spy; create: jasmine.Spy; delete: jasmine.Spy };
  let dialogMock: { open: jasmine.Spy };
  let snackMock: { open: jasmine.Spy };

  const aCourse: Course = {
    id: 1,
    name: 'Ciência da Computação',
    totalSemesters: 8,
    center: { id: 1, code: 'CCT' },
    studentCount: 3,
  };

  beforeEach(async () => {
    serviceMock = {
      list: jasmine.createSpy('list').and.returnValue(of([])),
      create: jasmine.createSpy('create'),
      delete: jasmine.createSpy('delete').and.returnValue(of(undefined)),
    };
    dialogMock = { open: jasmine.createSpy('open') };
    snackMock = { open: jasmine.createSpy('open') };

    await TestBed.configureTestingModule({
      imports: [CourseList],
      providers: [
        provideRouter([]),
        { provide: CourseService, useValue: serviceMock },
        { provide: MatDialog, useValue: dialogMock },
        { provide: MatSnackBar, useValue: snackMock },
      ],
    }).compileComponents();
  });

  function createComponent() {
    const fixture = TestBed.createComponent(CourseList);
    fixture.detectChanges();
    return fixture.componentInstance;
  }

  it('carrega os cursos na criação', () => {
    serviceMock.list.and.returnValue(of([aCourse]));
    const component = createComponent();
    expect(serviceMock.list).toHaveBeenCalled();
    expect(component.courses()).toEqual([aCourse]);
    expect(component.loading()).toBeFalse();
  });

  it('sinaliza erro quando a listagem falha', () => {
    serviceMock.list.and.returnValue(throwError(() => new Error('fail')));
    const component = createComponent();
    expect(component.error()).toBeTrue();
    expect(component.loading()).toBeFalse();
  });

  it('recarrega a lista quando um curso é cadastrado', () => {
    dialogMock.open.and.returnValue({ afterClosed: () => of(aCourse) });
    const component = createComponent();
    serviceMock.list.calls.reset();

    component.openCreate();

    expect(serviceMock.list).toHaveBeenCalled();
    expect(snackMock.open).toHaveBeenCalled();
  });

  it('atualiza a linha quando um curso é editado', () => {
    serviceMock.list.and.returnValue(of([aCourse]));
    const updated = { ...aCourse, name: 'Engenharia de Software' };
    dialogMock.open.and.returnValue({ afterClosed: () => of(updated) });
    const component = createComponent();

    component.openEdit(aCourse);

    expect(component.courses()).toEqual([updated]);
    expect(snackMock.open).toHaveBeenCalled();
  });

  it('exclui o curso após confirmação', () => {
    dialogMock.open.and.returnValue({ afterClosed: () => of(true) });
    const component = createComponent();
    serviceMock.list.calls.reset();

    component.confirmDelete(aCourse);

    expect(serviceMock.delete).toHaveBeenCalledWith(1);
    expect(serviceMock.list).toHaveBeenCalled();
    expect(snackMock.open).toHaveBeenCalled();
  });

  it('não exclui quando a confirmação é cancelada', () => {
    dialogMock.open.and.returnValue({ afterClosed: () => of(false) });
    const component = createComponent();

    component.confirmDelete(aCourse);

    expect(serviceMock.delete).not.toHaveBeenCalled();
  });
});
