import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Student } from '../../students/student.models';
import { StudentService } from '../../students/student.service';
import { EnrollmentService } from '../enrollment.service';

// Dados de entrada do dialog: o curso e quem já está matriculado (para esconder da lista).
export interface EnrollStudentDialogData {
  courseId: number;
  enrolledStudentIds: number[];
}

/**
 * Dialog de matrícula: busca um aluno por nome (autocomplete) e o matricula no curso.
 * Os alunos já matriculados são escondidos da lista; o 409 (matrícula duplicada, ex.
 * corrida) fica como fallback de mensagem. Fecha com true no sucesso.
 */
@Component({
  selector: 'app-enroll-student-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatProgressBarModule,
    MatIconModule,
  ],
  templateUrl: './enroll-student-dialog.html',
  styleUrl: './enroll-student-dialog.scss',
})
export class EnrollStudentDialog {
  private readonly studentService = inject(StudentService);
  private readonly enrollmentService = inject(EnrollmentService);
  private readonly dialogRef = inject(MatDialogRef<EnrollStudentDialog, boolean>);
  private readonly data = inject<EnrollStudentDialogData>(MAT_DIALOG_DATA);

  readonly control = new FormControl<Student | string>('', { nonNullable: true });
  readonly available = signal<Student[]>([]);
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  // Texto digitado, derivado do controle, para filtrar a lista por nome.
  private readonly query = signal('');
  readonly filtered = computed(() => {
    const q = this.query().toLowerCase();
    return this.available().filter((student) => student.name.toLowerCase().includes(q));
  });

  constructor() {
    this.control.valueChanges.pipe(takeUntilDestroyed()).subscribe((value) => {
      this.query.set(typeof value === 'string' ? value : value.name);
    });

    this.studentService.list().subscribe({
      next: (students) => {
        const enrolled = new Set(this.data.enrolledStudentIds);
        this.available.set(students.filter((student) => !enrolled.has(student.id)));
      },
      error: () => this.errorMessage.set('Não foi possível carregar os alunos.'),
    });
  }

  // Como o autocomplete exibe a opção selecionada (o valor é o objeto Student).
  readonly displayStudent = (student: Student | string | null): string =>
    typeof student === 'object' && student ? student.name : '';

  submit(): void {
    const selected = this.control.value;
    if (typeof selected !== 'object' || selected === null) {
      this.errorMessage.set('Selecione um aluno da lista.');
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);

    this.enrollmentService.enroll(this.data.courseId, selected.id).subscribe({
      next: () => this.dialogRef.close(true),
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.errorMessage.set(
          err.status === 409
            ? 'Este aluno já está matriculado neste curso.'
            : 'Não foi possível matricular. Tente novamente.',
        );
      },
    });
  }
}
