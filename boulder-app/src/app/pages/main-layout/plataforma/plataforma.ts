import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { PlataformaService } from '../../../services/plataforma.service';
import { ProyectoService } from '../../../services/proyecto.service';
import { Plataforma as PlataformaModel, Proyecto } from '../../../models/operacional';
import { AuthService } from '../../../services/auth.service';
import { mensajeDeError } from '../../../util/interfaz';
import { Ficha } from '../../../components/ficha/ficha';
import { SondajeService } from '../../../services/sondaje.service';
import { ZONAS_UTM } from '../../admin-layout/proyecto/proyecto';

const FORM_VACIO = (): PlataformaModel => ({
  proyectoId: '',
  codigo: '',
  nombre: '',
  este: undefined,
  norte: undefined,
  elevacion: undefined,
  numeroPozas: 0,
  estado: 'En construccion',
  tipo: '',
  ubicacion: '',
  notas: '',
});

/** Estados de una plataforma, con el valor que guarda la base y su rótulo. */
export const ESTADOS_PLATAFORMA = [
  { valor: 'En construccion', rotulo: 'En construcción', chip: 'chip--warn' },
  { valor: 'Operativo', rotulo: 'Operativa', chip: 'chip--ok' },
  { valor: 'Concluido', rotulo: 'Concluida', chip: '' },
];

/** Plataformas en maestro-detalle: lista a todo el ancho y ficha al elegir una. */
@Component({
  selector: 'app-plataforma',
  standalone: true,
  imports: [CommonModule, FormsModule, Ficha],
  templateUrl: './plataforma.html',
  styleUrl: './plataforma.scss',
})
export class Plataforma implements OnInit {
  private readonly service = inject(PlataformaService);
  readonly puedeEditar = inject(AuthService).puedeMantenerMaestros();
  private readonly proyectoService = inject(ProyectoService);
  private readonly sondajeService = inject(SondajeService);
  /** Sondajes en perforación por plataforma: «en uso». */
  enUso = new Map<string, string[]>();
  private readonly cdr = inject(ChangeDetectorRef);

  readonly estados = ESTADOS_PLATAFORMA;
  plataformas: PlataformaModel[] = [];
  proyectos: Proyecto[] = [];
  form: PlataformaModel & { id?: string } = FORM_VACIO();

  /** La ficha de alta o edición está abierta. */
  fichaAbierta = false;
  /** Filtro en vivo por código, nombre o proyecto. */
  filtro = '';
  busquedaCodigo = '';
  busquedaId = '';
  /** Arranca en true: el listado se pide en ngOnInit. */
  loading = true;
  guardando = false;
  mensaje = '';
  error = '';
  errorFormulario = '';
  filtrado = false;
  sinCoincidencias = '';

  ngOnInit(): void {
    this.proyectoService.listar().subscribe({
      next: (data) => {
        this.proyectos = data;
        this.cdr.detectChanges();
      },
      error: (err) => this.fallo('No se pudieron cargar los proyectos', err),
    });
    // Qué sondaje tiene hoy cada plataforma: los que no tienen fecha de fin.
    this.sondajeService.listar().subscribe({
      next: (sondajes) => {
        this.enUso = new Map();
        for (const s of sondajes.filter(x => !x.fechaFin && x.plataformaId)) {
          this.enUso.set(s.plataformaId!, [...(this.enUso.get(s.plataformaId!) ?? []), s.nombre || s.codigo]);
        }
        this.cdr.detectChanges();
      },
      error: () => { /* sin «en uso»: la lista sigue sirviendo */ },
    });
    this.listar();
  }

  get plataformasVisibles(): PlataformaModel[] {
    const q = this.filtro.trim().toLowerCase();
    return !q ? this.plataformas : this.plataformas.filter(p =>
      [p.codigo, p.nombre, this.nombreProyecto(p.proyectoId)].some(v => (v ?? '').toLowerCase().includes(q)));
  }

  verTodos(): void {
    this.limpiarAvisos();
    this.filtro = '';
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
      error: (err) => this.fallo('No se pudo cargar el listado de plataformas', err),
    });
  }

  // ------------------------------------------------------------------ ficha

  nueva(): void {
    this.form = FORM_VACIO();
    this.abrirFicha();
  }

  editar(plataforma: PlataformaModel): void {
    if (!this.puedeEditar) {
      return;
    }
    this.form = { ...plataforma };
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

  esSeleccionada(p: PlataformaModel & { id?: string }): boolean {
    return this.fichaAbierta && !!this.form.id && p.id === this.form.id;
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
        this.guardando = false;
        this.mensaje = `Plataforma ${this.form.codigo} ${esEdicion ? 'actualizada' : 'creada'}.`;
        this.cerrarFicha();
        this.listar();
      },
      error: (err) => {
        console.error('No se pudo guardar la plataforma', err);
        this.guardando = false;
        this.errorFormulario = mensajeDeError(err, 'No se pudo guardar');
        this.cdr.detectChanges();
      },
    });
  }

  // ---------------------------------------------------------- búsqueda exacta

  buscarPorCodigo(): void {
    const codigo = this.busquedaCodigo.trim();
    if (!codigo) return;
    this.loading = true;
    this.limpiarAvisos();
    this.service.obtenerPorCodigo(codigo).subscribe({
      next: (p) => { this.filtrado = true; this.setLista(p ? [p] : []); },
      error: (err) => this.sinResultados(err, `No hay ninguna plataforma con el código ${codigo}.`),
    });
  }

  buscarPorId(): void {
    if (!this.busquedaId.trim()) return;
    this.loading = true;
    this.limpiarAvisos();
    this.service.obtenerPorId(this.busquedaId.trim()).subscribe({
      next: (p) => { this.filtrado = true; this.setLista(p ? [p] : []); },
      error: (err) => this.sinResultados(err, 'No hay ninguna plataforma con ese ID.'),
    });
  }

  /** Coordenada con 2 decimales (centímetros) y sin separador de miles. */
  coord(v?: number | null): string {
    return v === null || v === undefined ? '—' : v.toFixed(2);
  }

  /** Zona UTM del proyecto elegido en la ficha, para rotular las coordenadas. */
  get zonaDelProyecto(): string | null {
    const srid = this.proyectos.find(p => p.id === this.form.proyectoId)?.srid;
    return ZONAS_UTM.find(z => z.srid === srid)?.etiqueta ?? null;
  }

  /** «DDH-037, DDH-033 +2»: hasta dos nombres cortos y cuántos más hay. */
  resumenEnUso(id?: string): string {
    const lista = (id && this.enUso.get(id)) || [];
    if (!lista.length) { return '—'; }
    return lista.slice(0, 2).join(', ') + (lista.length > 2 ? ` +${lista.length - 2}` : '');
  }

  estadoDe(valor?: string | null) {
    return this.estados.find(e => e.valor.toLowerCase() === (valor ?? '').toLowerCase());
  }

  nombreProyecto(id?: string): string {
    return this.proyectos.find((p) => p.id === id)?.nombre ?? '—';
  }

  private setLista(data: PlataformaModel[]): void {
    this.plataformas = [...data];
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

  private fallo(msg: string, err: unknown): void {
    console.error(msg, err);
    this.error = mensajeDeError(err, msg);
    this.loading = false;
    this.cdr.detectChanges();
  }
}
