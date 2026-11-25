import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { ProyectoService } from '../../../services/proyecto.service';
import { EmpresaService } from '../../../services/empresa.service';
import { Proyecto as ProyectoModel, Empresa } from '../../../models/operacional';
import { irA, mensajeDeError } from '../../../util/interfaz';

const FORM_VACIO = (): ProyectoModel => ({
  empresaId: '',
  codigo: '',
  nombre: '',
  ubicacion: '',
  srid: 32718,
});

/** Zonas UTM del Perú (WGS 84); el servidor solo acepta estas tres. */
export const ZONAS_UTM = [
  { srid: 32717, etiqueta: 'UTM 17S (32717)' },
  { srid: 32718, etiqueta: 'UTM 18S (32718)' },
  { srid: 32719, etiqueta: 'UTM 19S (32719)' },
];

@Component({
  selector: 'app-proyecto',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './proyecto.html',
  styleUrl: './proyecto.scss',
})
export class Proyecto implements OnInit {
  private readonly service = inject(ProyectoService);
  private readonly empresaService = inject(EmpresaService);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly zonas = ZONAS_UTM;

  proyectos: ProyectoModel[] = [];
  empresas: Empresa[] = [];
  form: ProyectoModel & { id?: string } = FORM_VACIO();

  busquedaCodigo = '';
  busquedaId = '';
  /** Arranca en true: el listado se pide en ngOnInit. */
  loading = true;
  mensaje = '';
  error = '';
  /** El listado muestra el resultado de una búsqueda, no todos los proyectos. */
  filtrado = false;
  /** Explicación del listado vacío tras una búsqueda sin coincidencias. */
  sinCoincidencias = '';

  ngOnInit(): void {
    this.empresaService.listar().subscribe({
      next: (data) => {
        this.empresas = data;
        this.cdr.detectChanges();
      },
      error: (err) => this.fallo('No se pudieron cargar las empresas', err),
    });
    this.listar();
  }

  /** Botón «Refrescar / Ver todos»: además de recargar, quita los avisos de la búsqueda. */
  verTodos(): void {
    this.limpiarAvisos();
    this.listar();
  }

  listar(): void {
    this.loading = true;
    this.filtrado = false;
    this.sinCoincidencias = '';
    this.busquedaCodigo = '';
    this.busquedaId = '';
    this.service.listar().subscribe({
      next: (data) => this.setLista(data),
      error: (err) => this.fallo('No se pudo cargar el listado de proyectos', err),
    });
  }

  guardar(): void {
    this.limpiarAvisos();
    // Un select sin elegir deja '', que el servidor no puede leer como UUID.
    const cuerpo = { ...this.form, empresaId: this.form.empresaId || null } as ProyectoModel;
    const op$ = this.form.id
      ? this.service.actualizar(this.form.id, cuerpo)
      : this.service.crear(cuerpo);
    op$.subscribe({
      next: () => {
        this.mensaje = this.form.id ? 'Proyecto actualizado' : 'Proyecto creado';
        this.limpiar();
        this.listar();
      },
      error: (err) => this.fallo('No se pudo guardar el proyecto', err),
    });
  }

  buscarPorCodigo(): void {
    const codigo = this.busquedaCodigo.trim().toUpperCase();
    if (!codigo) return;
    this.loading = true;
    this.limpiarAvisos();
    this.service.obtenerPorCodigo(codigo).subscribe({
      next: (p) => { this.filtrado = true; this.setLista(p ? [p] : []); },
      error: (err) => this.sinResultados(err, `No hay ningún proyecto con el código ${codigo}.`),
    });
  }

  buscarPorId(): void {
    if (!this.busquedaId.trim()) return;
    this.loading = true;
    this.limpiarAvisos();
    this.service.obtenerPorId(this.busquedaId.trim()).subscribe({
      next: (p) => { this.filtrado = true; this.setLista(p ? [p] : []); },
      error: (err) => this.sinResultados(err, 'No hay ningún proyecto con ese ID.'),
    });
  }

  nombreEmpresa(id?: string): string {
    return this.empresas.find((e) => e.id === id)?.nombre ?? '—';
  }

  zona(srid?: number): string {
    return this.zonas.find((z) => z.srid === srid)?.etiqueta ?? (srid ? String(srid) : '—');
  }

  editar(proyecto: ProyectoModel): void {
    this.form = { ...proyecto };
    this.limpiarAvisos();
    this.cdr.detectChanges();
    irA('pro-formulario', 'pro-nombre');
  }

  limpiar(): void {
    this.form = FORM_VACIO();
    this.cdr.detectChanges();
  }

  private setLista(data: ProyectoModel[]): void {
    this.proyectos = [...data];
    this.loading = false;
    this.cdr.detectChanges();
  }

  private limpiarAvisos(): void {
    this.mensaje = '';
    this.error = '';
  }

  /** Un 404 de una búsqueda no es un error: es una lista vacía con su explicación. */
  private sinResultados(err: unknown, texto: string): void {
    if (err instanceof HttpErrorResponse && err.status === 404) {
      this.filtrado = true;
      this.sinCoincidencias = texto;
      this.setLista([]);
      return;
    }
    this.fallo('No se pudo hacer la búsqueda', err);
  }

  /** Muestra el error con el mensaje del servidor (ver util/interfaz). */
  private fallo(msg: string, err: unknown): void {
    console.error(msg, err);
    this.error = mensajeDeError(err, msg);
    this.loading = false;
    this.cdr.detectChanges();
  }
}
