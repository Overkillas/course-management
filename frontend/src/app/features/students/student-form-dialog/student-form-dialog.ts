import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Student } from '../student.models';
import { StudentService } from '../student.service';

/**
 * Dialog de cadastro de aluno. Os validadores espelham o CreateStudentRequest do
 * backend: nome e e-mail obrigatórios, e a senha inicial valida só tamanho (8 a 72),
 * não a regra de senha forte (essa vale na troca do primeiro acesso). No sucesso,
 * fecha devolvendo o aluno criado; o 409 (e-mail duplicado) vira mensagem aqui.
 */
@Component({
  selector: 'app-student-form-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressBarModule,
    MatIconModule,
  ],
  templateUrl: './student-form-dialog.html',
  styleUrl: './student-form-dialog.scss',
})
export class StudentFormDialog {
  private readonly service = inject(StudentService);
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly dialogRef = inject(MatDialogRef<StudentFormDialog, Student>);

  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
  });

  // Espelha o valor da senha em signal para o check de tamanho reagir ao digitar. A
  // senha inicial valida só tamanho (8 a 72), não a regra de senha forte.
  private readonly passwordValue = signal('');
  readonly hasMinLength = computed(() => this.passwordValue().length >= 8);

  constructor() {
    this.form.controls.password.valueChanges
      .pipe(takeUntilDestroyed())
      .subscribe((value) => this.passwordValue.set(value));
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);

    this.service.create(this.form.getRawValue()).subscribe({
      next: (student) => this.dialogRef.close(student),
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.errorMessage.set(
          err.status === 409
            ? 'Já existe um aluno com este e-mail.'
            : 'Não foi possível cadastrar o aluno. Tente novamente.',
        );
      },
    });
  }
}
