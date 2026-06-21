import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { EnrollmentService } from './enrollment.service';

describe('EnrollmentService', () => {
  let service: EnrollmentService;
  let httpMock: HttpTestingController;
  const courseStudentsUrl = `${environment.apiUrl}/courses/3/students`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(EnrollmentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('lista matriculados via GET /courses/{id}/students', () => {
    service.listStudents(3).subscribe();

    const req = httpMock.expectOne(courseStudentsUrl);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('matricula via POST /courses/{id}/students', () => {
    service.enroll(3, 9).subscribe();

    const req = httpMock.expectOne(courseStudentsUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ studentId: 9 });
    req.flush({ id: 1, studentId: 9, courseId: 3, createdAt: 'x' });
  });

  it('lista os próprios cursos via GET /me/courses', () => {
    service.myCourses().subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/me/courses`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
