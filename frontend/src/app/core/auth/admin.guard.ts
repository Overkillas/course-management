import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * Restringe rotas ao admin. O authGuard roda antes (garante a sessão), então aqui só
 * resta checar o papel. Um usuário autenticado que não é admin é mandado para a sua
 * própria área (as próprias matrículas).
 */
export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.role() === 'admin' ? true : router.parseUrl('/me/courses');
};
