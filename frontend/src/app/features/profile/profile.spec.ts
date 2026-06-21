import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { MeService } from '../../core/me/me.service';
import { Profile } from './profile';

describe('Profile', () => {
  let serviceMock: { getProfile: jasmine.Spy };

  beforeEach(async () => {
    serviceMock = { getProfile: jasmine.createSpy('getProfile').and.returnValue(of(null)) };

    await TestBed.configureTestingModule({
      imports: [Profile],
      providers: [{ provide: MeService, useValue: serviceMock }],
    }).compileComponents();
  });

  function createComponent() {
    const fixture = TestBed.createComponent(Profile);
    fixture.detectChanges();
    return fixture.componentInstance;
  }

  it('carrega o perfil na criação', () => {
    const profile = { id: 1, name: 'Ana', email: 'ana@x.com' };
    serviceMock.getProfile.and.returnValue(of(profile));

    const component = createComponent();

    expect(serviceMock.getProfile).toHaveBeenCalled();
    expect(component.profile()).toEqual(profile);
    expect(component.loading()).toBeFalse();
  });

  it('sinaliza erro quando o perfil falha', () => {
    serviceMock.getProfile.and.returnValue(throwError(() => new Error('fail')));

    const component = createComponent();

    expect(component.error()).toBeTrue();
    expect(component.loading()).toBeFalse();
  });
});
