import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { StudentService } from './student.service';
import { StudentList } from './student-list';

describe('StudentList', () => {
  let serviceMock: { list: jasmine.Spy };

  beforeEach(async () => {
    serviceMock = { list: jasmine.createSpy('list').and.returnValue(of([])) };

    await TestBed.configureTestingModule({
      imports: [StudentList],
      providers: [{ provide: StudentService, useValue: serviceMock }],
    }).compileComponents();
  });

  it('carrega os alunos na criação', () => {
    const students = [{ id: 1, name: 'Maria', email: 'maria@x.com', mustChangePassword: false }];
    serviceMock.list.and.returnValue(of(students));

    const fixture = TestBed.createComponent(StudentList);
    fixture.detectChanges();

    expect(serviceMock.list).toHaveBeenCalled();
    expect(fixture.componentInstance.students()).toEqual(students);
    expect(fixture.componentInstance.loading()).toBeFalse();
  });

  it('sinaliza erro quando a listagem falha', () => {
    serviceMock.list.and.returnValue(throwError(() => new Error('fail')));

    const fixture = TestBed.createComponent(StudentList);
    fixture.detectChanges();

    expect(fixture.componentInstance.error()).toBeTrue();
    expect(fixture.componentInstance.loading()).toBeFalse();
  });
});
