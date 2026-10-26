import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { EmpleadoService } from '../../../services/empleado.service';
import { EmpresaService } from '../../../services/empresa.service';
import { Cargo, Empleado } from '../../../models/base';
import { Empresa } from '../../../models/operacional';

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

@Component({
  selector: 'app-admin-empleados',
  standalone: true,
  imports: [CommonModule, FormsModule],
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

  busqueda = '';
  filtroArea = '';
  soloActivos = false;

  loading = false;
  mensaje = '';
  error = '';

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

  guardar(): void {
    this.limpiarAvisos();

    const op$ = this.form.id
      ? this.service.actualizar(this.form.id, this.form)
      : this.service.crear(this.form);

    op$.subscribe({
      next: () => {
        this.mensaje = this.form.id ? 'Empleado actualizado' : 'Empleado creado';
        this.limpiar();
        this.listar();
      },
      error: (err) => this.fallo('No se pudo guardar el empleado', err),
    });
  }

  editar(e: Empleado): void {
    this.form = { ...e };
    this.limpiarAvisos();
    this.cdr.detectChanges();
  }

  limpiar(): void {
    this.form = FORM_VACIO();
    this.cdr.detectChanges();
  }

  cambiarEstado(e: Empleado, activo: boolean): void {
    if (!e.id) {
      return;
    }
    this.limpiarAvisos();

    this.service.cambiarEstado(e.id, activo).subscribe({
      next: () => {
        this.mensaje = activo
          ? `${e.nombres} ${e.apellidos ?? ''} reincorporado`.trim()
          : `${e.nombres} ${e.apellidos ?? ''} dado de baja`.trim();
        this.listar();
      },
      error: (err) => this.fallo('No se pudo cambiar el estado del empleado', err),
    });
  }

  private limpiarAvisos(): void {
    this.mensaje = '';
    this.error = '';
  }

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
