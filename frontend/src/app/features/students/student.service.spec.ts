import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { StudentService } from './student.service';

describe('StudentService', () => {
  let service: StudentService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(StudentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('lista alunos via GET /students', () => {
    const students = [{ id: 1, name: 'Maria', email: 'maria@x.com', mustChangePassword: false }];
    let result: unknown;

    service.list().subscribe((value) => (result = value));

    const req = httpMock.expectOne(`${environment.apiUrl}/students`);
    expect(req.request.method).toBe('GET');
    req.flush(students);

    expect(result).toEqual(students);
  });
});
