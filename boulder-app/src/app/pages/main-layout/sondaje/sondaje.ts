import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { forkJoin } from 'rxjs';

import { SondajeService } from '../../../services/sondaje.service';
import { ProyectoService } from '../../../services/proyecto.service';
import { CollarService } from '../../../services/collar.service';
import { PlataformaService } from '../../../services/plataforma.service';
import { MaquinaService } from '../../../services/maquina.service';
import {
  Sondaje as SondajeModel,
  Proyecto,
  Collar,
  Plataforma,
  Maquina,
} from '../../../models/operacional';
import { AuthService } from '../../../services/auth.service';
import { mensajeDeError } from '../../../util/interfaz';
import { Ficha } from '../../../components/ficha/ficha';

const FORM_VACIO = (): SondajeModel => ({
  codigo: '',
  nombre: '',
  proyectoId: '',
  collarId: '',
  plataformaId: '',
  maquinaId: '',
  programaSondajeId: '',
  azimutSetup: undefined,
  inclinacionSetup: undefined,
  profundidadActual: 0,
  profundidadFinal: undefined,
  fechaInicio: undefined,
  fechaFin: undefined,
  campania: '',
  notas: '',
});

/**
 * Sondajes en maestro-detalle, como las pantallas de administración: la lista
 * a todo el ancho y, al elegir uno (o pulsar «Nuevo sondaje»), su ficha a la
 * derecha.
 */
@Component({
  selector: 'app-sondaje',
  standalone: true,
  imports: [CommonModule, FormsModule, Ficha],
  templateUrl: './sondaje.html',
  styleUrl: './sondaje.scss',
})
export class Sondaje implements OnInit {
  private readonly service = inject(SondajeService);
  readonly puedeEditar = inject(AuthService).puedeMantenerMaestros();
  private readonly proyectoService = inject(ProyectoService);
  private readonly collarService = inject(CollarService);
  private readonly plataformaService = inject(PlataformaService);
  private readonly maquinaService = inject(MaquinaService);
  private readonly cdr = inject(ChangeDetectorRef);

  sondajes: SondajeModel[] = [];
  proyectos: Proyecto[] = [];
  collares: Collar[] = [];
  plataformas: Plataforma[] = [];
  maquinas: Maquina[] = [];

  form: SondajeModel & { id?: string } = FORM_VACIO();

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
  /** Errores de la lista (carga, búsquedas). */
  error = '';
  /** Errores del formulario: se muestran dentro de la ficha. */
  errorFormulario = '';
  /** El listado muestra el resultado de una búsqueda exacta. */
  filtrado = false;
  /** Explicación del listado vacío tras una búsqueda sin coincidencias. */
  sinCoincidencias = '';

  ngOnInit(): void {
    forkJoin({
      proyectos: this.proyectoService.listar(),
      collares: this.collarService.listar(),
      plataformas: this.plataformaService.listar(),
      maquinas: this.maquinaService.listar(),
    }).subscribe({
      next: (r) => {
        this.proyectos = r.proyectos;
        this.collares = r.collares;
        this.plataformas = r.plataformas;
        this.maquinas = r.maquinas;
        this.cdr.detectChanges();
      },
      error: (err) => this.fallo('No se pudieron cargar los catálogos del formulario', err),
    });
    this.listar();
  }

  /** Lista filtrada en el navegador. */
  get sondajesVisibles(): SondajeModel[] {
    const q = this.filtro.trim().toLowerCase();
    return !q ? this.sondajes : this.sondajes.filter(s =>
      [s.codigo, s.nombre, this.nombreProyecto(s.proyectoId)].some(v => (v ?? '').toLowerCase().includes(q)));
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
      error: (err) => this.fallo('No se pudo cargar el listado de sondajes', err),
    });
  }

  // ------------------------------------------------------------------ ficha

  nuevo(): void {
    this.form = FORM_VACIO();
    this.abrirFicha();
  }

  editar(sondaje: SondajeModel): void {
    if (!this.puedeEditar) {
      return;
    }
    this.form = { ...sondaje };
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

  esSeleccionado(s: SondajeModel & { id?: string }): boolean {
    return this.fichaAbierta && !!this.form.id && s.id === this.form.id;
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
        this.mensaje = `Sondaje ${this.form.codigo} ${esEdicion ? 'actualizado' : 'registrado'}.`;
        this.cerrarFicha();
        this.listar();
      },
      error: (err) => {
        console.error('No se pudo guardar el sondaje', err);
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
      next: (s) => { this.filtrado = true; this.setLista(s ? [s] : []); },
      error: (err) => this.sinResultados(err, `No hay ningún sondaje con el código ${codigo}.`),
    });
  }

  buscarPorId(): void {
    if (!this.busquedaId.trim()) return;
    this.loading = true;
    this.limpiarAvisos();
    this.service.obtenerPorId(this.busquedaId.trim()).subscribe({
      next: (s) => { this.filtrado = true; this.setLista(s ? [s] : []); },
      error: (err) => this.sinResultados(err, 'No hay ningún sondaje con ese ID.'),
    });
  }

  nombreProyecto(id?: string): string {
    return this.proyectos.find((p) => p.id === id)?.nombre ?? '—';
  }

  private setLista(data: SondajeModel[]): void {
    this.sondajes = [...data];
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
