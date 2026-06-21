import { Routes } from '@angular/router';
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
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  // Rota catch-all temporária: telas ainda não construídas caem aqui, protegidas pelo
  // authGuard e pela trava de primeiro acesso. Será removida conforme cada feature
  // (students, me/courses) for implementada.
  {
    path: '**',
    canActivate: [authGuard, mustChangePasswordGuard],
    loadComponent: () =>
      import('./features/under-construction/under-construction').then((m) => m.UnderConstruction),
  },
];
