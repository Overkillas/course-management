import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Student } from '../students/student.models';
import { EnrollStudentDialog } from './enroll-student-dialog/enroll-student-dialog';
import { EnrollmentService } from './enrollment.service';

/**
 * Alunos matriculados em um curso (admin), com a ação de matricular. Não há
 * desmatrícula: o backend só expõe listar e matricular; a remoção acontece em cascata
 * ao excluir aluno ou curso.
 */
@Component({
  selector: 'app-course-students',
  imports: [RouterLink, MatTableModule, MatProgressBarModule, MatButtonModule, MatIconModule, MatCardModule],
  templateUrl: './course-students.html',
  styleUrl: './course-students.scss',
})
export class CourseStudents {
  private readonly route = inject(ActivatedRoute);
  private readonly service = inject(EnrollmentService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  private readonly courseId = Number(this.route.snapshot.paramMap.get('courseId'));
  readonly courseName = signal<string | null>(null);
  readonly students = signal<Student[]>([]);
  readonly loading = signal(true);
  readonly error = signal(false);
  readonly displayedColumns = ['name', 'email'];

  constructor() {
    // O nome do curso vem do estado da navegação (a tela de cursos o envia ao abrir
    // esta). Em acesso direto/refresh ele não existe, e o título cai num genérico.
    const courseName = (history.state?.courseName ?? null) as string | null;
    if (courseName) {
      this.courseName.set(courseName);
    }
    this.load();
  }

  openEnroll(): void {
    this.dialog
      .open(EnrollStudentDialog, {
        data: { courseId: this.courseId, enrolledStudentIds: this.students().map((s) => s.id) },
      })
      .afterClosed()
      .subscribe((enrolled?: boolean) => {
        if (enrolled) {
          this.snackBar.open('Aluno matriculado.', 'Fechar', { duration: 4000 });
          this.load();
        }
      });
  }

  private load(): void {
    this.loading.set(true);
    this.error.set(false);

    this.service.listStudents(this.courseId).subscribe({
      next: (students) => {
        this.students.set(students);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      },
    });
  }
}
