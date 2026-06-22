import { BreakpointObserver } from '@angular/cdk/layout';
import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { map } from 'rxjs';
import { Course } from '../courses/course.models';
import { EnrollmentService } from '../enrollments/enrollment.service';

/**
 * Cursos em que o aluno autenticado está matriculado (/me/courses). Só leitura.
 */
@Component({
  selector: 'app-my-courses',
  imports: [
    MatTableModule,
    MatProgressBarModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
  ],
  templateUrl: './my-courses.html',
  styleUrl: './my-courses.scss',
})
export class MyCourses {
  private readonly service = inject(EnrollmentService);
  private readonly breakpoints = inject(BreakpointObserver);

  readonly courses = signal<Course[]>([]);
  readonly loading = signal(true);
  readonly error = signal(false);

  // Em telas estreitas, mostra nome e centro e um botão "ver mais" para os detalhes.
  readonly isHandset = toSignal(
    this.breakpoints.observe('(max-width: 768px)').pipe(map((state) => state.matches)),
    { initialValue: false },
  );
  readonly displayedColumns = computed(() =>
    this.isHandset()
      ? ['name', 'center', 'details']
      : ['name', 'center', 'totalSemesters', 'students'],
  );

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
