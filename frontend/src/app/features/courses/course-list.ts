import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';
import { ConfirmDialog } from '../../shared/confirm-dialog/confirm-dialog';
import { Course } from './course.models';
import { CourseFormDialog } from './course-form-dialog/course-form-dialog';
import { CourseService } from './course.service';

/**
 * Listagem de cursos (admin), com cadastro (dialog) e exclusão (com confirmação).
 * Após uma escrita bem-sucedida, recarrega a lista do servidor.
 */
@Component({
  selector: 'app-course-list',
  imports: [RouterLink, MatTableModule, MatProgressBarModule, MatButtonModule, MatIconModule],
  templateUrl: './course-list.html',
  styleUrl: './course-list.scss',
})
export class CourseList {
  private readonly service = inject(CourseService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly courses = signal<Course[]>([]);
  readonly loading = signal(true);
  readonly error = signal(false);
  readonly displayedColumns = ['name', 'center', 'totalSemesters', 'students', 'actions'];

  constructor() {
    this.load();
  }

  openCreate(): void {
    this.dialog
      .open(CourseFormDialog)
      .afterClosed()
      .subscribe((created?: Course) => {
        if (created) {
          this.snackBar.open('Curso cadastrado.', 'Fechar', { duration: 4000 });
          this.load();
        }
      });
  }

  confirmDelete(course: Course): void {
    this.dialog
      .open(ConfirmDialog, {
        data: {
          title: 'Excluir curso',
          message: `Excluir ${course.name}? As matrículas nele também serão removidas.`,
          confirmText: 'Excluir',
        },
      })
      .afterClosed()
      .subscribe((confirmed?: boolean) => {
        if (confirmed) {
          this.remove(course);
        }
      });
  }

  private remove(course: Course): void {
    this.service.delete(course.id).subscribe({
      next: () => {
        this.snackBar.open('Curso excluído.', 'Fechar', { duration: 4000 });
        this.load();
      },
      error: () => this.snackBar.open('Não foi possível excluir o curso.', 'Fechar', { duration: 4000 }),
    });
  }

  private load(): void {
    this.loading.set(true);
    this.error.set(false);

    this.service.list().subscribe({
      next: (courses) => {
        this.courses.set(courses);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      },
    });
  }
}
