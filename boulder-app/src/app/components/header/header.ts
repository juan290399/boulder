import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../services/auth.service';

/** Etiquetas amigables por código de rol; los no listados se muestran tal cual. */
const ETIQUETAS_ROL: Record<string, string> = {
  'ADMIN': 'Administrador',
  'GEOLOGO': 'Geólogo',
  'ASISTENTE_GEOLOGIA': 'Asistente de Geología',
  'VISITANTE': 'Visitante',
};

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './header.html',
  styleUrl: './header.scss',
})
export class Header {

  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  /**
   * Se leen una sola vez al construir el componente: el token no cambia
   * mientras dura la sesión, y volver a decodificarlo en cada ciclo de
   * detección de cambios no aportaría nada.
   */
  readonly nombre = this.authService.getNombreFromToken() ?? 'Usuario';
  readonly iniciales = this.authService.getInicialesFromToken();
  readonly rolActivo = this.authService.getEffectiveRole();
  readonly esAdmin = this.authService.hasRole('ADMIN');

  get etiquetaRol(): string {
    if (!this.rolActivo) {
      return 'Sin rol seleccionado';
    }
    return ETIQUETAS_ROL[this.rolActivo] ?? this.rolActivo;
  }

  cambiarRol(): void {
    this.router.navigate(['/auth/seleccionar-rol']);
  }

  cerrarSesion(): void {
    this.router.navigate(['/auth/logout']);
  }
}
