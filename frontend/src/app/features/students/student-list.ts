import { Component, inject, signal } from '@angular/core';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { Student } from './student.models';
import { StudentService } from './student.service';

/**
 * Listagem de alunos (admin). Cadastro e exclusão entram no próximo passo.
 */
@Component({
  selector: 'app-student-list',
  imports: [MatTableModule, MatProgressBarModule],
  templateUrl: './student-list.html',
  styleUrl: './student-list.scss',
})
export class StudentList {
  private readonly service = inject(StudentService);

  readonly students = signal<Student[]>([]);
  readonly loading = signal(true);
  readonly error = signal(false);
  readonly displayedColumns = ['name', 'email'];

  constructor() {
    this.load();
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
