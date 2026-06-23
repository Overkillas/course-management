import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { CourseService } from './course.service';

describe('CourseService', () => {
  let service: CourseService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/courses`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CourseService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('lista cursos via GET /courses', () => {
    let result: unknown;
    service.list().subscribe((value) => (result = value));

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush([]);

    expect(result).toEqual([]);
  });

  it('cadastra um curso via POST /courses', () => {
    const request = { name: 'Ciência da Computação', centerId: 1, totalSemesters: 8 };
    service.create(request).subscribe();

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush({ id: 1, name: request.name, totalSemesters: 8, center: { id: 1, code: 'CCT' } });
  });

  it('edita o nome via PATCH /courses/{id}', () => {
    service.updateName(5, 'Engenharia de Software').subscribe();

    const req = httpMock.expectOne(`${baseUrl}/5`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ name: 'Engenharia de Software' });
    req.flush({
      id: 5,
      name: 'Engenharia de Software',
      totalSemesters: 8,
      center: { id: 1, code: 'CCT' },
      studentCount: 3,
    });
  });

  it('exclui um curso via DELETE /courses/{id}', () => {
    service.delete(5).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/5`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
