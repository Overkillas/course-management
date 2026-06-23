import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Course } from '../course.models';
import { CourseService } from '../course.service';

/**
 * Dialog de edição de curso. Só o nome é editável (centro e semestres não mudam por
 * aqui). As validações espelham as do cadastro: nome obrigatório, de 3 a 100
 * caracteres. No sucesso, fecha devolvendo o curso atualizado; trata 404 e demais erros.
 */
@Component({
  selector: 'app-course-edit-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressBarModule,
    MatIconModule,
  ],
  templateUrl: './course-edit-dialog.html',
  styleUrl: './course-edit-dialog.scss',
})
export class CourseEditDialog {
  private readonly service = inject(CourseService);
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly dialogRef = inject(MatDialogRef<CourseEditDialog, Course>);
  private readonly data = inject<Course>(MAT_DIALOG_DATA);

  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.group({
    name: [this.data.name, [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);

    this.service.updateName(this.data.id, this.form.getRawValue().name).subscribe({
      next: (course) => this.dialogRef.close(course),
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.errorMessage.set(
          err.status === 404
            ? 'Este curso não existe mais. Atualize a lista.'
            : 'Não foi possível salvar. Tente novamente.',
        );
      },
    });
  }
}
