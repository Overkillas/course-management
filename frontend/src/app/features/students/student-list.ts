import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { ConfirmDialog } from '../../shared/confirm-dialog/confirm-dialog';
import { Student } from './student.models';
import { StudentFormDialog } from './student-form-dialog/student-form-dialog';
import { StudentService } from './student.service';

/**
 * Listagem de alunos (admin), com cadastro e exclusão (com confirmação).
 * Após uma escrita bem-sucedida, recarrega a lista do servidor.
 */
@Component({
  selector: 'app-student-list',
  imports: [MatTableModule, MatProgressBarModule, MatButtonModule, MatIconModule, MatCardModule],
  templateUrl: './student-list.html',
  styleUrl: './student-list.scss',
})
export class StudentList {
  private readonly service = inject(StudentService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly students = signal<Student[]>([]);
  readonly loading = signal(true);
  readonly error = signal(false);
  readonly displayedColumns = ['name', 'email', 'actions'];

  constructor() {
    this.load();
  }

  openCreate(): void {
    this.dialog
      .open(StudentFormDialog)
      .afterClosed()
      .subscribe((created?: Student) => {
        if (created) {
          this.snackBar.open('Aluno cadastrado.', 'Fechar', { duration: 4000 });
          this.load();
        }
      });
  }

  confirmDelete(student: Student): void {
    this.dialog
      .open(ConfirmDialog, {
        data: {
          title: 'Excluir aluno',
          message: `Excluir ${student.name}? As matrículas dele também serão removidas.`,
          confirmText: 'Excluir',
          icon: 'warning',
        },
      })
      .afterClosed()
      .subscribe((confirmed?: boolean) => {
        if (confirmed) {
          this.remove(student);
        }
      });
  }

  private remove(student: Student): void {
    this.service.delete(student.id).subscribe({
      next: () => {
        this.snackBar.open('Aluno excluído.', 'Fechar', { duration: 4000 });
        this.load();
      },
      error: () => this.snackBar.open('Não foi possível excluir o aluno.', 'Fechar', { duration: 4000 }),
    });
  }

  private load(): void {
    this.loading.set(true);
    this.error.set(false);

    this.service.list().subscribe({
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
