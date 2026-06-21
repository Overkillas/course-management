import { TestBed } from '@angular/core/testing';
import { MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';
import { CenterService } from '../center.service';
import { CourseService } from '../course.service';
import { CourseFormDialog } from './course-form-dialog';

describe('CourseFormDialog', () => {
  let courseServiceMock: { create: jasmine.Spy };
  let centerServiceMock: { list: jasmine.Spy };
  let dialogRefMock: { close: jasmine.Spy };

  beforeEach(async () => {
    courseServiceMock = { create: jasmine.createSpy('create') };
    centerServiceMock = {
      list: jasmine.createSpy('list').and.returnValue(of([{ id: 1, code: 'CCT' }])),
    };
    dialogRefMock = { close: jasmine.createSpy('close') };

    await TestBed.configureTestingModule({
      imports: [CourseFormDialog],
      providers: [
        { provide: CourseService, useValue: courseServiceMock },
        { provide: CenterService, useValue: centerServiceMock },
        { provide: MatDialogRef, useValue: dialogRefMock },
      ],
    }).compileComponents();
  });

  function createComponent() {
    return TestBed.createComponent(CourseFormDialog).componentInstance;
  }

  it('carrega os centros na criação', () => {
    const component = createComponent();
    expect(centerServiceMock.list).toHaveBeenCalled();
    expect(component.centers()).toEqual([{ id: 1, code: 'CCT' }]);
  });

  it('não cadastra quando o formulário é inválido', () => {
    const component = createComponent();
    component.submit();
    expect(courseServiceMock.create).not.toHaveBeenCalled();
  });

  it('não cadastra quando os semestres passam de 100', () => {
    const component = createComponent();
    component.form.setValue({ name: 'Computação', centerId: 1, totalSemesters: 101 });

    component.submit();

    expect(component.form.controls.totalSemesters.hasError('max')).toBeTrue();
    expect(courseServiceMock.create).not.toHaveBeenCalled();
  });

  it('não cadastra quando o nome tem menos de 3 caracteres', () => {
    const component = createComponent();
    component.form.setValue({ name: 'CC', centerId: 1, totalSemesters: 8 });

    component.submit();

    expect(component.form.controls.name.hasError('minlength')).toBeTrue();
    expect(courseServiceMock.create).not.toHaveBeenCalled();
  });

  it('fecha o dialog com o curso criado no sucesso', () => {
    const course = { id: 1, name: 'Computação', totalSemesters: 8, center: { id: 1, code: 'CCT' } };
    courseServiceMock.create.and.returnValue(of(course));
    const component = createComponent();
    component.form.setValue({ name: 'Computação', centerId: 1, totalSemesters: 8 });

    component.submit();

    expect(courseServiceMock.create).toHaveBeenCalledWith({
      name: 'Computação',
      centerId: 1,
      totalSemesters: 8,
    });
    expect(dialogRefMock.close).toHaveBeenCalledWith(course);
  });
});
