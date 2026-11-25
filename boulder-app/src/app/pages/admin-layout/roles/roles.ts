import { RouterLink } from '@angular/router';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';

import { RolService } from '../../../services/rol.service';
import { UsuarioService } from '../../../services/usuario.service';
import { Rol } from '../../../models/base';
import { PERMISOS, ROLES, presentacionRol } from '../../../models/roles';

@Component({
  selector: 'app-admin-roles',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './roles.html',
  styleUrl: './roles.scss',
})
export class Roles implements OnInit {

  private readonly rolService = inject(RolService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly cdr = inject(ChangeDetectorRef);

  roles: Rol[] = [];

  /**
   * Cuántas cuentas activas usan cada rol, por código.
   *
   * El valor se declara opcional a propósito: un rol que nadie usa no tiene
   * entrada en el mapa, y sin `| undefined` TypeScript da por seguro un acceso
   * que en ejecución devuelve `undefined`.
   */
  cuentasPorRol: Record<string, number | undefined> = {};

  /** Usuario de cada cuenta activa con el rol, para mostrarlo bajo el conteo. */
  usuariosPorRol: Record<string, string[] | undefined> = {};

  readonly permisos = PERMISOS;

  /** Arranca en true: los datos se piden en ngOnInit y el primer render ya dice «Cargando». */
  loading = true;
  error = '';

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading = true;

    this.rolService.listar().subscribe({
      next: (data) => {
        // De más a menos permisos, en el orden de la matriz y no en el de la base.
        const orden = Object.keys(ROLES);
        const posicion = (c: string) => (orden.indexOf(c) === -1 ? orden.length : orden.indexOf(c));
        this.roles = [...data].sort((a, b) => posicion(a.codigo) - posicion(b.codigo));
        this.contarCuentas();
      },
      error: (err) => this.fallo('No se pudo cargar el catálogo de roles', err),
    });
  }

  /**
   * El backend no expone un conteo por rol, así que se deriva del padrón de
   * usuarios, que esta pantalla ya puede leer. Si el listado creciera, esto
   * merece un endpoint de agregación.
   */
  private contarCuentas(): void {
    this.usuarioService.listar().subscribe({
      next: (usuarios) => {
        const conteo: Record<string, number | undefined> = {};
        const nombres: Record<string, string[] | undefined> = {};
        for (const u of usuarios) {
          if (u.estado !== true) {
            continue;
          }
          for (const codigo of u.roles ?? []) {
            conteo[codigo] = (conteo[codigo] ?? 0) + 1;
            (nombres[codigo] ??= []).push(u.usuario);
          }
        }
        this.cuentasPorRol = conteo;
        this.usuariosPorRol = nombres;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => this.fallo('No se pudo contar las cuentas por rol', err),
    });
  }

  presentacion(codigo: string) {
    return presentacionRol(codigo);
  }

  permite(roles: string[], codigo: string): boolean {
    return roles.includes(codigo);
  }

  private fallo(mensaje: string, err: unknown): void {
    console.error(mensaje, err);
    this.error = mensaje;
    this.loading = false;
    this.cdr.detectChanges();
  }
}
