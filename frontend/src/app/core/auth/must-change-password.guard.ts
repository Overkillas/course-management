import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * Trava de primeiro acesso: se o usuário precisa trocar a senha, qualquer rota
 * protegida o devolve para /change-password, espelhando a trava de 403 do backend.
 * A própria /change-password não usa este guard, para não criar loop.
 */
export const mustChangePasswordGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.mustChangePassword()) {
    return router.parseUrl('/change-password');
  }
  return true;
};
