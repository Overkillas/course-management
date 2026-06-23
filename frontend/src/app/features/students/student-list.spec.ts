import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { StudentService } from './student.service';
import { StudentList } from './student-list';

describe('StudentList', () => {
  let serviceMock: { list: jasmine.Spy; create: jasmine.Spy; delete: jasmine.Spy };
  let dialogMock: { open: jasmine.Spy };
  let snackMock: { open: jasmine.Spy };

  beforeEach(async () => {
    serviceMock = {
      list: jasmine.createSpy('list').and.returnValue(of([])),
      create: jasmine.createSpy('create'),
      delete: jasmine.createSpy('delete').and.returnValue(of(undefined)),
    };
    dialogMock = { open: jasmine.createSpy('open') };
    snackMock = { open: jasmine.createSpy('open') };

    await TestBed.configureTestingModule({
      imports: [StudentList],
      providers: [
        { provide: StudentService, useValue: serviceMock },
        { provide: MatDialog, useValue: dialogMock },
        { provide: MatSnackBar, useValue: snackMock },
      ],
    }).compileComponents();
  });

  function createComponent() {
    const fixture = TestBed.createComponent(StudentList);
    fixture.detectChanges();
    return fixture.componentInstance;
  }

  const aStudent = { id: 1, name: 'Maria', email: 'maria@x.com', mustChangePassword: false };

  it('carrega os alunos na criação', () => {
    serviceMock.list.and.returnValue(of([aStudent]));
    const component = createComponent();
    expect(serviceMock.list).toHaveBeenCalled();
    expect(component.students()).toEqual([aStudent]);
    expect(component.loading()).toBeFalse();
  });

  it('sinaliza erro quando a listagem falha', () => {
    serviceMock.list.and.returnValue(throwError(() => new Error('fail')));
    const component = createComponent();
    expect(component.error()).toBeTrue();
    expect(component.loading()).toBeFalse();
  });

  it('recarrega a lista quando um aluno é cadastrado', () => {
    const created = { id: 2, name: 'João', email: 'joao@x.com', mustChangePassword: true };
    dialogMock.open.and.returnValue({ afterClosed: () => of(created) });
    const component = createComponent();
    serviceMock.list.calls.reset();

    component.openCreate();

    expect(serviceMock.list).toHaveBeenCalled();
    expect(snackMock.open).toHaveBeenCalled();
  });

  it('atualiza a linha quando um aluno é editado', () => {
    serviceMock.list.and.returnValue(of([aStudent]));
    const updated = { ...aStudent, name: 'Maria Souza' };
    dialogMock.open.and.returnValue({ afterClosed: () => of(updated) });
    const component = createComponent();

    component.openEdit(aStudent);

    expect(component.students()).toEqual([updated]);
    expect(snackMock.open).toHaveBeenCalled();
  });

  it('exclui o aluno após confirmação', () => {
    dialogMock.open.and.returnValue({ afterClosed: () => of(true) });
    const component = createComponent();
    serviceMock.list.calls.reset();

    component.confirmDelete(aStudent);

    expect(serviceMock.delete).toHaveBeenCalledWith(1);
    expect(serviceMock.list).toHaveBeenCalled();
    expect(snackMock.open).toHaveBeenCalled();
  });

  it('não exclui quando a confirmação é cancelada', () => {
    dialogMock.open.and.returnValue({ afterClosed: () => of(false) });
    const component = createComponent();

    component.confirmDelete(aStudent);

    expect(serviceMock.delete).not.toHaveBeenCalled();
  });
});
