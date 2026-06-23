import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Student } from '../student.models';
import { StudentService } from '../student.service';

/**
 * Dialog de edição de aluno. Só o nome é editável (o e-mail não muda por aqui). As
 * validações espelham as do cadastro: nome obrigatório, até 100 caracteres. No
 * sucesso, fecha devolvendo o aluno atualizado; trata 404 (sumiu) e demais erros.
 */
@Component({
  selector: 'app-student-edit-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressBarModule,
    MatIconModule,
  ],
  templateUrl: './student-edit-dialog.html',
  styleUrl: './student-edit-dialog.scss',
})
export class StudentEditDialog {
  private readonly service = inject(StudentService);
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly dialogRef = inject(MatDialogRef<StudentEditDialog, Student>);
  private readonly data = inject<Student>(MAT_DIALOG_DATA);

  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.group({
    name: [this.data.name, [Validators.required, Validators.maxLength(100)]],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);

    this.service.updateName(this.data.id, this.form.getRawValue().name).subscribe({
      next: (student) => this.dialogRef.close(student),
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.errorMessage.set(
          err.status === 404
            ? 'Este aluno não existe mais. Atualize a lista.'
            : 'Não foi possível salvar. Tente novamente.',
        );
      },
    });
  }
}
