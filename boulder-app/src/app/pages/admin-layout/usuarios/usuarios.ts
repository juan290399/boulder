import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { AuthService } from '../../../services/auth.service';
import { RolService } from '../../../services/rol.service';
import { UsuarioService } from '../../../services/usuario.service';
import { Rol, Usuario } from '../../../models/base';

type FiltroEstado = 'todos' | 'activos' | 'inactivos' | 'pendientes';

const FORM_VACIO = (): Usuario & { id?: string } => ({
  nombres: '',
  apellidos: '',
  correo: '',
  usuario: '',
  contrasenia: '',
  estado: true,
});

@Component({
  selector: 'app-admin-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.scss',
})
export class Usuarios implements OnInit {

  private readonly service = inject(UsuarioService);
  private readonly rolService = inject(RolService);
  private readonly authService = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);

  /** Para no ofrecer acciones que el backend va a rechazar sobre uno mismo. */
  private readonly usuarioActual = this.authService.getUsuarioFromToken();

  usuarios: Usuario[] = [];
  roles: Rol[] = [];

  form: Usuario & { id?: string } = FORM_VACIO();

  busqueda = '';
  filtroEstado: FiltroEstado = 'todos';

  loading = false;
  mensaje = '';
  error = '';

  /** Fila cuyo editor de roles está abierto. */
  editandoRolesDe: Usuario | null = null;
  rolesSeleccionados = new Set<string>();

  ngOnInit(): void {
    this.cargarRoles();
    this.listar();
  }

  // ---------------------------------------------------------------- listados

  listar(): void {
    this.loading = true;
    this.service.listar().subscribe({
      next: (data) => {
        this.usuarios = [...data];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => this.fallo('No se pudo cargar el listado de usuarios', err),
    });
  }

  private cargarRoles(): void {
    this.rolService.listar().subscribe({
      next: (data) => {
        this.roles = [...data];
        this.cdr.detectChanges();
      },
      error: (err) => this.fallo('No se pudo cargar el catálogo de roles', err),
    });
  }

  /**
   * Filtrado en cliente: `listar()` trae el padrón completo y son pocas
   * cuentas. Si el listado creciera, esto pasa a ser un parámetro del GET.
   */
  get usuariosFiltrados(): Usuario[] {
    const texto = this.busqueda.trim().toLowerCase();

    return this.usuarios.filter((u) => {
      const coincide = !texto
        || `${u.nombres} ${u.apellidos}`.toLowerCase().includes(texto)
        || u.usuario.toLowerCase().includes(texto)
        || u.correo.toLowerCase().includes(texto);

      if (!coincide) {
        return false;
      }

      switch (this.filtroEstado) {
        case 'activos':
          return u.estado === true;
        case 'inactivos':
          return u.estado === false;
        // El auto-registro deja la cuenta inactiva y sin roles; son las que
        // esperan que un administrador las habilite.
        case 'pendientes':
          return u.estado === false && (u.roles?.length ?? 0) === 0;
        default:
          return true;
      }
    });
  }

  get totalPendientes(): number {
    return this.usuarios.filter((u) => u.estado === false && (u.roles?.length ?? 0) === 0).length;
  }

  esUsuarioActual(u: Usuario): boolean {
    return this.usuarioActual !== null && u.usuario === this.usuarioActual;
  }

  // ------------------------------------------------------------------- altas

  guardar(): void {
    this.limpiarAvisos();

    const op$ = this.form.id
      ? this.service.actualizar(this.form.id, this.form)
      : this.service.crear(this.form);

    op$.subscribe({
      next: () => {
        this.mensaje = this.form.id ? 'Cuenta actualizada' : 'Cuenta creada';
        this.limpiar();
        this.listar();
      },
      error: (err) => this.fallo('No se pudo guardar la cuenta', err),
    });
  }

  editar(u: Usuario): void {
    // La contraseña nunca vuelve del backend; en blanco significa "no cambiar".
    this.form = { ...u, contrasenia: '' };
    this.limpiarAvisos();
    this.cdr.detectChanges();
  }

  limpiar(): void {
    this.form = FORM_VACIO();
    this.cdr.detectChanges();
  }

  cambiarEstado(u: Usuario, activo: boolean): void {
    if (!u.id) {
      return;
    }
    this.limpiarAvisos();

    this.service.cambiarEstado(u.id, activo).subscribe({
      next: () => {
        this.mensaje = activo
          ? `Cuenta de ${u.usuario} activada`
          : `Cuenta de ${u.usuario} desactivada`;
        this.listar();
      },
      error: (err) => this.fallo('No se pudo cambiar el estado de la cuenta', err),
    });
  }

  // ------------------------------------------------------------------- roles

  abrirRoles(u: Usuario): void {
    this.editandoRolesDe = u;
    this.rolesSeleccionados = new Set(u.roles ?? []);
    this.limpiarAvisos();
    this.cdr.detectChanges();
  }

  cerrarRoles(): void {
    this.editandoRolesDe = null;
    this.rolesSeleccionados.clear();
    this.cdr.detectChanges();
  }

  alternarRol(codigo: string): void {
    if (this.rolesSeleccionados.has(codigo)) {
      this.rolesSeleccionados.delete(codigo);
    } else {
      this.rolesSeleccionados.add(codigo);
    }
  }

  tieneRolSeleccionado(codigo: string): boolean {
    return this.rolesSeleccionados.has(codigo);
  }

  guardarRoles(): void {
    const destino = this.editandoRolesDe;
    if (!destino?.id) {
      return;
    }
    this.limpiarAvisos();

    this.service.asignarRoles(destino.id, [...this.rolesSeleccionados]).subscribe({
      next: () => {
        this.mensaje = `Roles de ${destino.usuario} actualizados`;
        this.cerrarRoles();
        this.listar();
      },
      error: (err) => this.fallo('No se pudieron asignar los roles', err),
    });
  }

  // ------------------------------------------------------------------ avisos

  private limpiarAvisos(): void {
    this.mensaje = '';
    this.error = '';
  }

  /**
   * Prefiere el mensaje del backend: las salvaguardas devuelven textos
   * concretos ("No puedes quitarte a ti mismo el rol ADMIN") que explican
   * mucho mejor el rechazo que un genérico.
   */
  private fallo(contexto: string, err: unknown): void {
    console.error(contexto, err);

    let detalle = '';
    if (err instanceof HttpErrorResponse) {
      if (err.status === 403) {
        detalle = 'No tienes permiso para esta operación.';
      } else {
        detalle = err.error?.errores ?? err.error?.message ?? '';
      }
    }

    this.error = detalle ? `${contexto}: ${detalle}` : contexto;
    this.loading = false;
    this.cdr.detectChanges();
  }
}
