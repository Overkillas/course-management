import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  AbstractControl,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { MeService } from '../../core/me/me.service';
import { strongPassword } from './strong-password.validator';

// Validator de grupo: confirma que os dois campos de senha são iguais.
function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const newPassword = group.get('newPassword')?.value;
  const confirmPassword = group.get('confirmPassword')?.value;
  return newPassword === confirmPassword ? null : { passwordsMismatch: true };
}

/**
 * Tela de troca de senha do primeiro acesso. Define a nova senha e, no sucesso,
 * encerra a sessão e volta ao login: o token antigo continua travado pelo backend,
 * então re-logar é o que entrega um token já liberado.
 */
@Component({
  selector: 'app-change-password',
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressBarModule,
    MatIconModule,
  ],
  templateUrl: './change-password.html',
  styleUrl: './change-password.scss',
})
export class ChangePassword {
  private readonly me = inject(MeService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly snackBar = inject(MatSnackBar);

  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly hideNew = signal(true);
  readonly hideConfirm = signal(true);

  readonly form = this.fb.group(
    {
      newPassword: ['', [Validators.required, strongPassword]],
      confirmPassword: ['', [Validators.required]],
    },
    { validators: passwordsMatch },
  );

  // Valor atual da nova senha, espelhado em signal para a checklist de requisitos
  // reagir conforme o usuário digita.
  private readonly newPasswordValue = signal('');

  readonly rules = computed(() => {
    const value = this.newPasswordValue();
    return {
      length: value.length >= 8,
      letter: /[a-zA-Z]/.test(value),
      digit: /\d/.test(value),
      special: /[^a-zA-Z0-9]/.test(value),
    };
  });

  constructor() {
    this.form.controls.newPassword.valueChanges
      .pipe(takeUntilDestroyed())
      .subscribe((value) => this.newPasswordValue.set(value));
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);

    this.me.changePassword(this.form.getRawValue().newPassword).subscribe({
      next: () => {
        this.auth.logout();
        this.snackBar.open('Senha alterada. Entre novamente.', 'Fechar', { duration: 5000 });
        this.router.navigateByUrl('/login');
      },
      error: (_err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.errorMessage.set('Não foi possível trocar a senha. Tente novamente.');
      },
    });
  }
}
