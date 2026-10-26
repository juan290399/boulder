import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { CuentaService } from '../../../services/cuenta.service';
import { Usuario } from '../../../models/base';

/**
 * Mi cuenta (P14, RF02): los datos de la cuenta propia y el cambio de contraseña.
 *
 * Las reglas de la contraseña se muestran antes de enviar para no hacer viajar
 * algo que el servidor va a rechazar, pero quien decide es el servidor: si
 * responde que no, se muestra su motivo tal cual.
 */
@Component({
  selector: 'app-mi-cuenta',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './mi-cuenta.html',
  styleUrl: './mi-cuenta.scss',
})
export class MiCuenta implements OnInit {

  private readonly cuentaService = inject(CuentaService);

  readonly cuenta = signal<Usuario | null>(null);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);
  readonly exito = signal(false);

  readonly actual = signal('');
  readonly nueva = signal('');
  readonly repetida = signal('');

  /** Cada regla con su estado, para pintarlas en vivo debajo del campo. */
  readonly reglas = computed(() => {
    const n = this.nueva();
    const usuario = this.cuenta()?.usuario?.toLowerCase() ?? '';
    return [
      { texto: 'Al menos 8 caracteres', ok: n.length >= 8 },
      { texto: 'Letras y números', ok: /\p{L}/u.test(n) && /\d/.test(n) },
      { texto: 'No contiene tu usuario', ok: !!n && (!usuario || !n.toLowerCase().includes(usuario)) },
      { texto: 'Distinta de la actual', ok: !!n && n !== this.actual() },
      { texto: 'Las dos nuevas coinciden', ok: !!n && n === this.repetida() },
    ];
  });

  readonly listo = computed(() => !!this.actual() && this.reglas().every(r => r.ok));

  ngOnInit(): void {
    this.cuentaService.miCuenta().subscribe({
      next: c => this.cuenta.set(c),
      error: () => this.error.set('No se pudieron cargar los datos de la cuenta.'),
    });
  }

  cambiar(): void {
    if (!this.listo() || this.guardando()) {
      return;
    }
    this.guardando.set(true);
    this.error.set(null);
    this.exito.set(false);
    this.cuentaService.cambiarContrasena(this.actual(), this.nueva()).subscribe({
      next: () => {
        this.guardando.set(false);
        this.exito.set(true);
        this.actual.set('');
        this.nueva.set('');
        this.repetida.set('');
      },
      error: (e: HttpErrorResponse) => {
        this.guardando.set(false);
        this.error.set(e.error?.message ?? 'No se pudo cambiar la contraseña.');
      },
    });
  }
}
