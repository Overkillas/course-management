import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { Center } from '../center.models';
import { CenterService } from '../center.service';
import { Course } from '../course.models';
import { CourseService } from '../course.service';

// Teto do total de semestres, espelhando a regra do backend. Barramos aqui para
// evitar o round-trip de um 400.
const MAX_SEMESTERS = 100;

/**
 * Dialog de cadastro de curso. O centro é um dropdown populado por GET /centers. Os
 * validadores espelham o CreateCourseRequest: nome obrigatório, centro obrigatório e
 * número de semestres obrigatório e positivo. No sucesso, fecha devolvendo o curso.
 */
@Component({
  selector: 'app-course-form-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatProgressBarModule,
  ],
  templateUrl: './course-form-dialog.html',
  styleUrl: './course-form-dialog.scss',
})
export class CourseFormDialog {
  private readonly courseService = inject(CourseService);
  private readonly centerService = inject(CenterService);
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly dialogRef = inject(MatDialogRef<CourseFormDialog, Course>);

  readonly centers = signal<Center[]>([]);
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.group({
    // Nome: 3 a 100 caracteres, espelhando o backend.
    //
    // Filtro de caracteres considerado e NÃO implementado: chegou a ser desenhada uma
    // allowlist (letras, números, espaço e pontuação) que bloquearia emojis e símbolos
    // como #, @, $. Descartei porque, por menor que seja a chance, alguns desses
    // caracteres podem fazer parte de um nome de curso legítimo, e prefiro não
    // restringir além do tamanho a ponto de barrar um nome válido.
    name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
    centerId: [null as number | null, [Validators.required]],
    totalSemesters: [
      null as number | null,
      [Validators.required, Validators.min(1), Validators.max(MAX_SEMESTERS)],
    ],
  });

  constructor() {
    this.centerService.list().subscribe({
      next: (centers) => this.centers.set(centers),
      error: () => this.errorMessage.set('Não foi possível carregar os centros.'),
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);

    const value = this.form.getRawValue();
    this.courseService
      .create({
        name: value.name,
        centerId: value.centerId!,
        totalSemesters: value.totalSemesters!,
      })
      .subscribe({
        next: (course) => this.dialogRef.close(course),
        error: (_err: HttpErrorResponse) => {
          this.submitting.set(false);
          this.errorMessage.set('Não foi possível cadastrar o curso. Tente novamente.');
        },
      });
  }
}
