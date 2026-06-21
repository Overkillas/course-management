import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Espelha o StrongPasswordValidator do backend: mínimo de 8 caracteres, com ao menos
 * uma letra, um dígito e um caractere especial. Valor vazio não acusa erro aqui (o
 * Validators.required cuida da ausência), igual ao backend, que trata blank como válido.
 */
export const strongPassword: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const value: string = control.value ?? '';
  if (value.length === 0) {
    return null;
  }

  const isStrong =
    value.length >= 8 && /[a-zA-Z]/.test(value) && /\d/.test(value) && /[^a-zA-Z0-9]/.test(value);

  return isStrong ? null : { strongPassword: true };
};
