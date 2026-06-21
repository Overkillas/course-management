import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { isExpired } from './jwt.util';

/**
 * Protege rotas que exigem sessão. Libera se há claims e o token não expirou; caso
 * contrário, limpa a sessão e redireciona para o login. A expiração é verificada
 * aqui (a cada navegação) em vez de em um computed, para ser sempre fresca.
 */
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const claims = auth.claims();
  if (claims && !isExpired(claims)) {
    return true;
  }

  auth.logout();
  return router.parseUrl('/login');
};
