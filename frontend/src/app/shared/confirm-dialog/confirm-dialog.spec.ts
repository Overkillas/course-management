import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ConfirmDialog } from './confirm-dialog';

describe('ConfirmDialog', () => {
  let dialogRefMock: { close: jasmine.Spy };

  beforeEach(async () => {
    dialogRefMock = { close: jasmine.createSpy('close') };

    await TestBed.configureTestingModule({
      imports: [ConfirmDialog],
      providers: [
        { provide: MatDialogRef, useValue: dialogRefMock },
        { provide: MAT_DIALOG_DATA, useValue: { title: 'Excluir', message: 'Confirma?' } },
      ],
    }).compileComponents();
  });

  function createComponent() {
    return TestBed.createComponent(ConfirmDialog).componentInstance;
  }

  it('fecha com true ao confirmar', () => {
    createComponent().confirm();
    expect(dialogRefMock.close).toHaveBeenCalledWith(true);
  });

  it('fecha com false ao cancelar', () => {
    createComponent().cancel();
    expect(dialogRefMock.close).toHaveBeenCalledWith(false);
  });
});
