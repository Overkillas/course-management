import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { CenterService } from './center.service';

describe('CenterService', () => {
  let service: CenterService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CenterService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('lista centros via GET /centers', () => {
    const centers = [{ id: 1, code: 'CCT' }];
    let result: unknown;

    service.list().subscribe((value) => (result = value));

    const req = httpMock.expectOne(`${environment.apiUrl}/centers`);
    expect(req.request.method).toBe('GET');
    req.flush(centers);

    expect(result).toEqual(centers);
  });
});
