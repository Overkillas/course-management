import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { Course } from '../courses/course.models';
import { EnrollmentService } from '../enrollments/enrollment.service';
import { MyCourses } from './my-courses';

describe('MyCourses', () => {
  let serviceMock: { myCourses: jasmine.Spy };

  const aCourse: Course = {
    id: 1,
    name: 'Ciência da Computação',
    totalSemesters: 8,
    center: { id: 1, code: 'CCT' },
  };

  beforeEach(async () => {
    serviceMock = { myCourses: jasmine.createSpy('myCourses').and.returnValue(of([])) };

    await TestBed.configureTestingModule({
      imports: [MyCourses],
      providers: [{ provide: EnrollmentService, useValue: serviceMock }],
    }).compileComponents();
  });

  function createComponent() {
    const fixture = TestBed.createComponent(MyCourses);
    fixture.detectChanges();
    return fixture.componentInstance;
  }

  it('carrega os próprios cursos na criação', () => {
    serviceMock.myCourses.and.returnValue(of([aCourse]));

    const component = createComponent();

    expect(serviceMock.myCourses).toHaveBeenCalled();
    expect(component.courses()).toEqual([aCourse]);
    expect(component.loading()).toBeFalse();
  });

  it('sinaliza erro quando a listagem falha', () => {
    serviceMock.myCourses.and.returnValue(throwError(() => new Error('fail')));

    const component = createComponent();

    expect(component.error()).toBeTrue();
    expect(component.loading()).toBeFalse();
  });
});
