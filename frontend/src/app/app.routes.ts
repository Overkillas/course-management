import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/login/login').then((m) => m.Login),
  },
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  // Rota catch-all temporária: telas ainda não construídas caem aqui. Será removida
  // conforme cada feature (students, me/courses, change-password) for implementada.
  {
    path: '**',
    loadComponent: () =>
      import('./features/under-construction/under-construction').then((m) => m.UnderConstruction),
  },
];
