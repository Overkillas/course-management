import { FormControl } from '@angular/forms';
import { strongPassword } from './strong-password.validator';

describe('strongPassword', () => {
  function validate(value: string) {
    return strongPassword(new FormControl(value));
  }

  it('aceita uma senha forte', () => {
    expect(validate('Senha!123')).toBeNull();
  });

  it('não acusa erro com valor vazio (fica para o required)', () => {
    expect(validate('')).toBeNull();
  });

  it('rejeita senha curta', () => {
    expect(validate('Ab!2')).toEqual({ strongPassword: true });
  });

  it('rejeita senha sem dígito', () => {
    expect(validate('SenhaForte!')).toEqual({ strongPassword: true });
  });

  it('rejeita senha sem letra', () => {
    expect(validate('12345678!')).toEqual({ strongPassword: true });
  });

  it('rejeita senha sem caractere especial', () => {
    expect(validate('Senha1234')).toEqual({ strongPassword: true });
  });
});
