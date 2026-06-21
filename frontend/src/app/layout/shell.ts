import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth/auth.service';

/**
 * Layout das telas autenticadas: toolbar com navegação e logout, e um router-outlet
 * para a tela atual. A navegação se adapta ao papel (por ora, os links do admin).
 */
@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatToolbarModule, MatButtonModule],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',
})
export class Shell {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly email = this.auth.claims()?.upn ?? null;
  readonly isAdmin = this.auth.role() === 'admin';

  logout(): void {
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }
}
