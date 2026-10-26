import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../../services/auth.service';

/** Etiquetas amigables por código de rol; los no listados se muestran tal cual. */
const ETIQUETAS_ROL: Record<string, string> = {
  'ADMIN': '⚙️ Administrador',
  'GEOLOGO': '🪨 Geólogo / Exploración',
  'ASISTENTE_GEOLOGIA': '📋 Asistente de Geología',
  'VISITANTE': '👁️ Visitante (solo lectura)',
};

@Component({
  selector: 'app-role-selection',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './role-selection.html',
  styleUrls: ['./role-selection.scss']
})
export class RoleSelection implements OnInit {

  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);

  roles: string[] = [];
  selectedRole: string | null = null;

  ngOnInit(): void {
    // Sin token no hay nada que elegir: de vuelta al login.
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/auth/login']);
      return;
    }

    this.roles = this.authService.getRolesFromToken();

    // Con un único rol no tiene sentido preguntar.
    if (this.roles.length === 1) {
      this.selectedRole = this.roles[0];
      this.onRoleSelected();
      return;
    }

    this.cdr.detectChanges();
  }

  etiqueta(rol: string): string {
    return ETIQUETAS_ROL[rol] ?? rol;
  }

  onRoleSelected(): void {
    if (!this.selectedRole) return;

    this.authService.setSelectedRole(this.selectedRole);

    // Quien elige actuar como administrador espera el panel, no la vista
    // operativa; el resto de roles van a la zona de trabajo habitual.
    this.router.navigate([this.selectedRole === 'ADMIN' ? '/admin' : '/']);
  }

  volverAlLogin(): void {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }
}
