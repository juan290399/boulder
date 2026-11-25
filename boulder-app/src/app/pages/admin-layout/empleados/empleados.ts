import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { EmpleadoService } from '../../../services/empleado.service';
import { EmpresaService } from '../../../services/empresa.service';
import { Cargo, Empleado } from '../../../models/base';
import { Empresa } from '../../../models/operacional';
import { mensajeDeError } from '../../../util/interfaz';
import { Ficha } from '../../../components/ficha/ficha';

/** Etiquetas para las áreas de `base.cargo.area`. */
const ETIQUETAS_AREA: Record<string, string> = {
  'perforacion': 'Perforación',
  'geologia': 'Geología',
  'laboratorio': 'Laboratorio',
  'administracion': 'Administración',
};

const FORM_VACIO = (): Empleado & { id?: string } => ({
  codigo: '',
  nombres: '',
  apellidos: '',
  documento: '',
  email: '',
  telefono: '',
  estado: true,
});

/** Personal: lista a pantalla completa; alta, edición y baja en la ficha de la derecha. */
@Component({
  selector: 'app-admin-empleados',
  standalone: true,
  imports: [CommonModule, FormsModule, Ficha],
  templateUrl: './empleados.html',
  styleUrl: './empleados.scss',
})
export class Empleados implements OnInit {

  private readonly service = inject(EmpleadoService);
  private readonly empresaService = inject(EmpresaService);
  private readonly cdr = inject(ChangeDetectorRef);

  empleados: Empleado[] = [];
  cargos: Cargo[] = [];
  empresas: Empresa[] = [];

  form: Empleado & { id?: string } = FORM_VACIO();

  /** La ficha de alta o edición está abierta. */
  fichaAbierta = false;
  busqueda = '';
  filtroArea = '';
  soloActivos = false;

  /** Arranca en true: el listado se pide en ngOnInit. */
  loading = true;
  guardando = false;
  mensaje = '';
  /** Errores de la lista (carga). */
  error = '';
  /** Errores del formulario: se muestran dentro de la ficha. */
  errorFormulario = '';

  ngOnInit(): void {
    this.cargarCatalogos();
    this.listar();
  }

  listar(): void {
    this.loading = true;
    this.service.listar().subscribe({
      next: (data) => {
        this.empleados = [...data];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => this.fallo('No se pudo cargar el catálogo de personal', err),
    });
  }

  private cargarCatalogos(): void {
    this.service.listarCargos().subscribe({
      next: (data) => {
        this.cargos = [...data];
        this.cdr.detectChanges();
      },
      error: (err) => this.fallo('No se pudo cargar el catálogo de cargos', err),
    });

    this.empresaService.listar().subscribe({
      next: (data) => {
        this.empresas = [...data];
        this.cdr.detectChanges();
      },
      error: (err) => this.fallo('No se pudo cargar el listado de empresas', err),
    });
  }

  /** Filtrado en cliente: el catálogo es corto y ya viene entero. */
  get empleadosFiltrados(): Empleado[] {
    const texto = this.busqueda.trim().toLowerCase();

    return this.empleados.filter((e) => {
      if (this.soloActivos && e.estado !== true) {
        return false;
      }
      if (this.filtroArea && e.cargoArea !== this.filtroArea) {
        return false;
      }
      if (!texto) {
        return true;
      }
      return `${e.nombres} ${e.apellidos ?? ''}`.toLowerCase().includes(texto)
        || e.codigo.toLowerCase().includes(texto);
    });
  }

  /** Áreas presentes en el catálogo de cargos, para el desplegable. */
  get areas(): string[] {
    return [...new Set(this.cargos.map((c) => c.area))].sort();
  }

  etiquetaArea(area: string | undefined): string {
    if (!area) {
      return '—';
    }
    return ETIQUETAS_AREA[area] ?? area;
  }

  // ------------------------------------------------------------------ ficha

  nuevo(): void {
    this.form = FORM_VACIO();
    this.abrirFicha();
  }

  editar(e: Empleado): void {
    this.form = { ...e };
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

  esSeleccionado(e: Empleado): boolean {
    return this.fichaAbierta && !!this.form.id && e.id === this.form.id;
  }

  guardar(): void {
    this.errorFormulario = '';
    this.guardando = true;
    const esEdicion = !!this.form.id;

    const op$ = esEdicion
      ? this.service.actualizar(this.form.id!, this.form)
      : this.service.crear(this.form);

    op$.subscribe({
      next: () => {
        this.mensaje = `${this.nombreCompleto(this.form)} ${esEdicion ? 'actualizado' : 'registrado'}.`;
        this.terminar();
      },
      error: (err) => this.falloFormulario('No se pudo guardar', err),
    });
  }

  /** Baja o reincorporación del empleado abierto en la ficha. */
  cambiarEstado(activo: boolean): void {
    const id = this.form.id;
    if (!id) {
      return;
    }
    this.errorFormulario = '';
    this.guardando = true;

    this.service.cambiarEstado(id, activo).subscribe({
      next: () => {
        this.mensaje = `${this.nombreCompleto(this.form)} ${activo ? 'reincorporado' : 'dado de baja'}.`;
        this.terminar();
      },
      error: (err) => this.falloFormulario('No se pudo cambiar el estado', err),
    });
  }

  private nombreCompleto(e: Empleado): string {
    return `${e.nombres} ${e.apellidos ?? ''}`.trim();
  }

  private terminar(): void {
    this.guardando = false;
    this.cerrarFicha();
    this.listar();
  }

  private falloFormulario(contexto: string, err: unknown): void {
    console.error(contexto, err);
    this.guardando = false;
    this.errorFormulario = mensajeDeError(err, contexto);
    this.cdr.detectChanges();
  }

  private limpiarAvisos(): void {
    this.mensaje = '';
    this.error = '';
  }

  /** Muestra el error con el mensaje del servidor (ver util/interfaz). */
  private fallo(contexto: string, err: unknown): void {
    console.error(contexto, err);
    this.error = mensajeDeError(err, contexto);
    this.loading = false;
    this.cdr.detectChanges();
  }
}
