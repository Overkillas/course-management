import { Component, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { UserProfile } from '../../core/me/me.models';
import { MeService } from '../../core/me/me.service';

/**
 * Perfil do próprio usuário autenticado (/me). Só exibição: nome e e-mail.
 */
@Component({
  selector: 'app-profile',
  imports: [MatCardModule, MatProgressBarModule],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
})
export class Profile {
  private readonly service = inject(MeService);

  readonly profile = signal<UserProfile | null>(null);
  readonly loading = signal(true);
  readonly error = signal(false);

  constructor() {
    this.service.getProfile().subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      },
    });
  }
}
