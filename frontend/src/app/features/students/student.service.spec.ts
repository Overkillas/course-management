import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { StudentService } from './student.service';

describe('StudentService', () => {
  let service: StudentService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/students`;

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

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush(students);

    expect(result).toEqual(students);
  });

  it('cadastra um aluno via POST /students', () => {
    const request = { name: 'Maria', email: 'maria@x.com', password: 'Temp1234' };

    service.create(request).subscribe();

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush({ id: 1, ...request, mustChangePassword: true });
  });

  it('exclui um aluno via DELETE /students/{id}', () => {
    service.delete(7).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/7`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
