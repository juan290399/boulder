import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Observable, of, switchMap } from 'rxjs';

import { AuthService } from '../../../services/auth.service';
import { RolService } from '../../../services/rol.service';
import { UsuarioService } from '../../../services/usuario.service';
import { Rol, Usuario } from '../../../models/base';
import { mensajeDeError } from '../../../util/interfaz';
import { Ficha } from '../../../components/ficha/ficha';

type FiltroEstado = 'todos' | 'activos' | 'inactivos' | 'pendientes';

const FORM_VACIO = (): Usuario & { id?: string } => ({
  nombres: '',
  apellidos: '',
  correo: '',
  usuario: '',
  contrasenia: '',
  estado: true,
});

/** El auto-registro deja la cuenta inactiva y sin roles: espera que un administrador la apruebe. */
const esPendiente = (u: Usuario): boolean => u.estado === false && (u.roles?.length ?? 0) === 0;

/**
 * Cuentas en maestro-detalle: la lista a la izquierda y, a la derecha, la ficha
 * con los datos, los roles y la activación de la cuenta elegida.
 */
@Component({
  selector: 'app-admin-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule, Ficha],
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
  /** Roles marcados en la ficha. */
  rolesSeleccionados = new Set<string>();
  /** La cuenta abierta estaba pendiente al abrir la ficha: guardar la aprueba. */
  aprobando = false;

  /** La ficha de alta o edición está abierta. */
  fichaAbierta = false;
  busqueda = '';
  filtroEstado: FiltroEstado = 'todos';

  /** Arranca en true: el listado se pide en ngOnInit y así el primer render ya muestra «Cargando». */
  loading = true;
  guardando = false;
  mensaje = '';
  /** Errores de la lista (carga). */
  error = '';
  /** Errores de la ficha: se muestran junto a los campos. */
  errorFormulario = '';

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
        case 'pendientes':
          return esPendiente(u);
        default:
          return true;
      }
    });
  }

  get totalPendientes(): number {
    return this.usuarios.filter(esPendiente).length;
  }

  esPendiente(u: Usuario): boolean {
    return esPendiente(u);
  }

  esUsuarioActual(u: Usuario): boolean {
    return this.usuarioActual !== null && u.usuario === this.usuarioActual;
  }

  verPendientes(): void {
    this.filtroEstado = 'pendientes';
    this.busqueda = '';
    this.cdr.detectChanges();
  }

  // ------------------------------------------------------------------- ficha

  nueva(): void {
    this.form = FORM_VACIO();
    this.rolesSeleccionados = new Set();
    this.aprobando = false;
    this.abrirFicha();
  }

  editar(u: Usuario): void {
    // La contraseña nunca vuelve del backend; en blanco significa "no cambiar".
    this.form = { ...u, contrasenia: '' };
    this.rolesSeleccionados = new Set(u.roles ?? []);
    this.aprobando = esPendiente(u);
    this.abrirFicha();
  }

  cerrarFicha(): void {
    this.fichaAbierta = false;
    this.errorFormulario = '';
    this.cdr.detectChanges();
  }

  private abrirFicha(): void {
    this.limpiarAvisos();
    this.errorFormulario = '';
    this.fichaAbierta = true;
    this.cdr.detectChanges();
  }

  esSeleccionado(u: Usuario): boolean {
    return this.fichaAbierta && !!this.form.id && u.id === this.form.id;
  }

  alternarRol(codigo: string): void {
    if (this.rolesSeleccionados.has(codigo)) {
      this.rolesSeleccionados.delete(codigo);
    } else {
      this.rolesSeleccionados.add(codigo);
    }
  }

  /**
   * Guarda en un solo paso lo que la ficha muestra: los datos, los roles si
   * cambiaron y, si la cuenta estaba pendiente, la activa (aprobación).
   */
  guardar(): void {
    this.errorFormulario = '';
    if (this.aprobando && this.rolesSeleccionados.size === 0) {
      this.errorFormulario = 'Para aprobar la cuenta, marca al menos un rol.';
      this.cdr.detectChanges();
      return;
    }

    this.guardando = true;
    const esEdicion = !!this.form.id;
    const aprobar = this.aprobando;
    const roles = [...this.rolesSeleccionados];
    const rolesCambiaron = !this.mismosRoles(this.form.roles ?? [], roles);

    const datos$: Observable<Usuario> = esEdicion
      ? this.service.actualizar(this.form.id!, this.form)
      : this.service.crear(this.form);

    datos$.pipe(
      switchMap((guardado) => {
        const id = guardado?.id ?? this.form.id!;
        const roles$: Observable<unknown> = rolesCambiaron ? this.service.asignarRoles(id, roles) : of(null);
        return roles$.pipe(switchMap(() => (aprobar ? this.service.cambiarEstado(id, true) : of(null))));
      }),
    ).subscribe({
      next: () => {
        this.mensaje = aprobar
          ? `Cuenta de ${this.form.usuario} aprobada.`
          : `Cuenta de ${this.form.usuario} ${esEdicion ? 'actualizada' : 'creada'}.`;
        this.terminar();
      },
      error: (err) => this.falloFormulario('No se pudo guardar la cuenta', err),
    });
  }

  /** Activa o desactiva la cuenta abierta en la ficha. */
  cambiarEstado(activo: boolean): void {
    const id = this.form.id;
    if (!id) {
      return;
    }
    this.errorFormulario = '';
    this.guardando = true;

    this.service.cambiarEstado(id, activo).subscribe({
      next: () => {
        this.mensaje = `Cuenta de ${this.form.usuario} ${activo ? 'activada' : 'desactivada'}.`;
        this.terminar();
      },
      error: (err) => this.falloFormulario('No se pudo cambiar el estado de la cuenta', err),
    });
  }

  private mismosRoles(antes: string[], ahora: string[]): boolean {
    return antes.length === ahora.length && antes.every((r) => ahora.includes(r));
  }

  private terminar(): void {
    this.guardando = false;
    this.cerrarFicha();
    this.listar();
  }

  // ------------------------------------------------------------------ avisos

  private limpiarAvisos(): void {
    this.mensaje = '';
    this.error = '';
  }

  private falloFormulario(contexto: string, err: unknown): void {
    console.error(contexto, err);
    this.guardando = false;
    this.errorFormulario = mensajeDeError(err, contexto);
    this.cdr.detectChanges();
  }

  /** Muestra el error con el mensaje del servidor (ver util/interfaz). */
  private fallo(contexto: string, err: unknown): void {
    console.error(contexto, err);
    this.error = mensajeDeError(err, contexto);
    this.loading = false;
    this.cdr.detectChanges();
  }
}
