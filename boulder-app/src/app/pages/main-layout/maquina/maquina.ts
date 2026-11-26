import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { MaquinaService } from '../../../services/maquina.service';
import { Maquina as MaquinaModel } from '../../../models/operacional';
import { AuthService } from '../../../services/auth.service';
import { mensajeDeError } from '../../../util/interfaz';
import { Ficha } from '../../../components/ficha/ficha';

const FORM_VACIO = (): MaquinaModel => ({
  codigo: '',
  nombre: '',
  tipo: '',
  marca: '',
  motor: '',
  estado: 'Operativa',
});

/** Estados de una máquina, con su color en la lista. */
export const ESTADOS_MAQUINA = [
  { valor: 'Operativa', chip: 'chip--ok' },
  { valor: 'En mantenimiento', chip: 'chip--warn' },
  { valor: 'Fuera de servicio', chip: 'chip--danger' },
];

/** Flota en maestro-detalle: lista a todo el ancho y ficha al elegir una máquina. */
@Component({
  selector: 'app-maquina',
  standalone: true,
  imports: [CommonModule, FormsModule, Ficha],
  templateUrl: './maquina.html',
  styleUrl: './maquina.scss',
})
export class Maquina implements OnInit {
  private readonly service = inject(MaquinaService);
  readonly puedeEditar = inject(AuthService).puedeMantenerMaestros();
  private readonly cdr = inject(ChangeDetectorRef);

  readonly estados = ESTADOS_MAQUINA;
  maquinas: MaquinaModel[] = [];
  form: MaquinaModel & { id?: string } = FORM_VACIO();

  /** La ficha de alta o edición está abierta. */
  fichaAbierta = false;
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
    this.listar();
  }

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
      error: (err) => this.fallo('No se pudo cargar el listado de máquinas', err),
    });
  }

  // ------------------------------------------------------------------ ficha

  nueva(): void {
    this.form = FORM_VACIO();
    this.abrirFicha();
  }

  editar(maquina: MaquinaModel): void {
    if (!this.puedeEditar) {
      return;
    }
    this.form = { ...maquina };
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

  esSeleccionada(m: MaquinaModel & { id?: string }): boolean {
    return this.fichaAbierta && !!this.form.id && m.id === this.form.id;
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
        this.mensaje = `Máquina ${this.form.codigo} ${esEdicion ? 'actualizada' : 'creada'}.`;
        this.cerrarFicha();
        this.listar();
      },
      error: (err) => {
        console.error('No se pudo guardar la máquina', err);
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
      next: (m) => { this.filtrado = true; this.setLista(m ? [m] : []); },
      error: (err) => this.sinResultados(err, `No hay ninguna máquina con el código ${codigo}.`),
    });
  }

  buscarPorId(): void {
    if (!this.busquedaId.trim()) return;
    this.loading = true;
    this.limpiarAvisos();
    this.service.obtenerPorId(this.busquedaId.trim()).subscribe({
      next: (m) => { this.filtrado = true; this.setLista(m ? [m] : []); },
      error: (err) => this.sinResultados(err, 'No hay ninguna máquina con ese ID.'),
    });
  }

  chipEstado(valor?: string | null): string {
    return this.estados.find(e => e.valor.toLowerCase() === (valor ?? '').toLowerCase())?.chip ?? '';
  }

  private setLista(data: MaquinaModel[]): void {
    this.maquinas = [...data];
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
