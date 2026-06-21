import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { MeService } from './me.service';

describe('MeService', () => {
  let service: MeService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/me`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(MeService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('busca o perfil via GET /me', () => {
    const profile = { id: 1, name: 'Ana', email: 'ana@x.com' };
    let result: unknown;

    service.getProfile().subscribe((value) => (result = value));

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush(profile);

    expect(result).toEqual(profile);
  });

  it('troca a senha via POST /me/password', () => {
    service.changePassword('Nova!123').subscribe();

    const req = httpMock.expectOne(`${baseUrl}/password`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ newPassword: 'Nova!123' });
    req.flush(null);
  });
});
