import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';

import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.scss',
})
export class AdminLayout {

  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  /** Del claim `nombre` del JWT; el token no cambia durante la sesión. */
  readonly usuarioAdmin = this.authService.getNombreFromToken() ?? 'Administrador';

  /** Iniciales para el avatar de la barra superior. */
  readonly iniciales = this.authService.getInicialesFromToken();

  /**
   * Vuelve a la zona operativa.
   *
   * El panel solo contiene lo que es exclusivo de administración; los datos de
   * operación (avances, sondajes, desvíos, quicklog) se consultan en el
   * layout normal, al que un ADMIN también tiene acceso.
   */
  irAOperaciones(): void {
    this.router.navigate(['/avances']);
  }

  cerrarSesion(): void {
    this.router.navigate(['/auth/logout']);
  }
}
