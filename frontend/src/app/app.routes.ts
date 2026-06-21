import { Routes } from '@angular/router';
import { adminGuard } from './core/auth/admin.guard';
import { authGuard } from './core/auth/auth.guard';
import { mustChangePasswordGuard } from './core/auth/must-change-password.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/login/login').then((m) => m.Login),
  },
  {
    path: 'change-password',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/change-password/change-password').then((m) => m.ChangePassword),
  },
  {
    path: '',
    canActivate: [authGuard, mustChangePasswordGuard],
    loadComponent: () => import('./layout/shell').then((m) => m.Shell),
    children: [
      {
        path: 'students',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/students/student-list').then((m) => m.StudentList),
      },
      {
        path: 'courses',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/courses/course-list').then((m) => m.CourseList),
      },
      {
        path: 'courses/:courseId/students',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/enrollments/course-students').then((m) => m.CourseStudents),
      },
      { path: '', pathMatch: 'full', redirectTo: 'students' },
    ],
  },
  // Rota catch-all temporária: rotas ainda não construídas (ex.: /me/courses) caem
  // aqui. Vai sumindo conforme as telas do aluno chegam (Etapa 4).
  {
    path: '**',
    canActivate: [authGuard, mustChangePasswordGuard],
    loadComponent: () =>
      import('./features/under-construction/under-construction').then((m) => m.UnderConstruction),
  },
];
