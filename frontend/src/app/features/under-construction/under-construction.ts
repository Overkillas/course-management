import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

/**
 * Tela temporária servida pela rota catch-all enquanto as telas reais não existem.
 * Sai conforme cada feature (students, me/courses, change-password) é construída.
 * Serve para confirmar que o login e o redirecionamento por papel funcionam.
 */
@Component({
  selector: 'app-under-construction',
  imports: [MatButtonModule],
  templateUrl: './under-construction.html',
  styleUrl: './under-construction.scss',
})
export class UnderConstruction {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly email = this.auth.claims()?.upn ?? null;

  logout(): void {
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }
}
