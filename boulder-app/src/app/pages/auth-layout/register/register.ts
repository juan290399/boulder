import { Component, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

import { AuthService } from '../../../services/auth.service';
import { Registro } from '../../../models/base';

const FORM_VACIO = (): Registro => ({
  nombres: '',
  apellidos: '',
  correo: '',
  usuario: '',
  contrasenia: '',
});

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register {

  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  user: Registro = FORM_VACIO();
  loading = false;
  errorMessage = '';
  okMessage = '';

  onRegister(): void {
    this.loading = true;
    this.errorMessage = '';
    this.okMessage = '';

    this.authService.register(this.user).subscribe({
      next: () => {
        this.loading = false;
        this.user = FORM_VACIO();
        this.okMessage =
          'Cuenta creada. Queda pendiente de activación por un administrador; ' +
          'hasta entonces el inicio de sesión será rechazado.';
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = this.mensajeDeError(err);
        this.cdr.detectChanges();
      },
    });
  }

  irAlLogin(): void {
    this.router.navigate(['/auth/login']);
  }

  /** El backend responde con { message, errores? } vía GlobalExceptionHandler. */
  private mensajeDeError(err: any): string {
    const detalle = err?.error?.errores;
    if (detalle && typeof detalle === 'object') {
      const primeros = Object.values(detalle) as string[];
      if (primeros.length > 0) return primeros.join(' · ');
    }
    return err?.error?.message ?? 'No se pudo completar el registro.';
  }
}
