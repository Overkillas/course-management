import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth/auth.service';
import { MeService } from '../core/me/me.service';

/**
 * Layout das telas autenticadas: toolbar com navegação por papel e um menu de conta
 * (nome, e-mail, papel e logout) no canto. Os dados do próprio usuário ficam aqui, no
 * lugar de uma tela de perfil dedicada.
 */
@Component({
  selector: 'app-shell',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDividerModule,
  ],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',
})
export class Shell {
  private readonly auth = inject(AuthService);
  private readonly me = inject(MeService);
  private readonly router = inject(Router);

  readonly email = this.auth.claims()?.upn ?? null;
  readonly isAdmin = this.auth.role() === 'admin';
  readonly roleLabel = this.isAdmin ? 'Administrador' : this.auth.role() === 'aluno' ? 'Aluno' : null;
  readonly userName = signal<string | null>(null);

  constructor() {
    this.me.getProfile().subscribe({
      next: (profile) => this.userName.set(profile.name),
      error: () => {},
    });
  }

  logout(): void {
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }
}
