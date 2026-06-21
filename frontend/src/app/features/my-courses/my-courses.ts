import { Component, inject, signal } from '@angular/core';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { Course } from '../courses/course.models';
import { EnrollmentService } from '../enrollments/enrollment.service';

/**
 * Cursos em que o aluno autenticado está matriculado (/me/courses). Só leitura.
 */
@Component({
  selector: 'app-my-courses',
  imports: [MatTableModule, MatProgressBarModule],
  templateUrl: './my-courses.html',
  styleUrl: './my-courses.scss',
})
export class MyCourses {
  private readonly service = inject(EnrollmentService);

  readonly courses = signal<Course[]>([]);
  readonly loading = signal(true);
  readonly error = signal(false);
  readonly displayedColumns = ['name', 'center', 'totalSemesters'];

  constructor() {
    this.service.myCourses().subscribe({
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
