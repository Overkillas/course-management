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
      {
        path: 'me',
        loadComponent: () => import('./features/profile/profile').then((m) => m.Profile),
      },
      {
        path: 'me/courses',
        loadComponent: () => import('./features/my-courses/my-courses').then((m) => m.MyCourses),
      },
      { path: '', pathMatch: 'full', redirectTo: 'students' },
    ],
  },
  // Rota desconhecida cai no shell, que pelo guard manda para o login ou para o
  // destino conforme o papel.
  { path: '**', redirectTo: '' },
];
