import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { EmpresaService } from '../../../services/empresa.service';
import { Empresa as EmpresaModel } from '../../../models/operacional';
import { mensajeDeError } from '../../../util/interfaz';
import { Ficha } from '../../../components/ficha/ficha';

const FORM_VACIO = (): EmpresaModel => ({
  codigo: '',
  nombre: '',
  ruc: '',
  direccion: '',
  telefono: '',
  correo: '',
});

/**
 * Empresas en maestro-detalle: la lista a la izquierda y el alta o la edición
 * en la ficha de la derecha, así no se pierde de vista la lista.
 */
@Component({
  selector: 'app-empresa',
  standalone: true,
  imports: [CommonModule, FormsModule, Ficha],
  templateUrl: './empresa.html',
  styleUrl: './empresa.scss',
})
export class Empresa implements OnInit {
  private readonly service = inject(EmpresaService);
  private readonly cdr = inject(ChangeDetectorRef);

  empresas: EmpresaModel[] = [];
  form: EmpresaModel & { id?: string } = FORM_VACIO();

  /** La ficha de alta o edición está abierta. */
  fichaAbierta = false;
  /** Filtro en vivo sobre la lista por nombre, código o RUC. */
  filtro = '';
  busquedaRuc = '';
  busquedaId = '';
  /** Arranca en true: el listado se pide en ngOnInit. */
  loading = true;
  guardando = false;
  mensaje = '';
  /** Errores de la lista (carga, búsquedas). */
  error = '';
  /** Errores del formulario: se muestran dentro de la ficha, junto a los campos. */
  errorFormulario = '';
  /** El listado muestra el resultado de una búsqueda exacta, no todas las empresas. */
  filtrado = false;
  /** Explicación del listado vacío tras una búsqueda sin coincidencias. */
  sinCoincidencias = '';

  ngOnInit(): void {
    this.listar();
  }

  /** Lista filtrada en el navegador: son pocas empresas. */
  get empresasVisibles(): EmpresaModel[] {
    const q = this.filtro.trim().toLowerCase();
    return !q ? this.empresas : this.empresas.filter(e =>
      [e.nombre, e.codigo, e.ruc].some(v => (v ?? '').toLowerCase().includes(q)));
  }

  /** Botón «Refrescar / Ver todas»: además de recargar, quita avisos y filtros. */
  verTodos(): void {
    this.limpiarAvisos();
    this.filtro = '';
    this.listar();
  }

  listar(): void {
    this.loading = true;
    this.filtrado = false;
    this.sinCoincidencias = '';
    this.busquedaRuc = '';
    this.busquedaId = '';
    this.service.listar().subscribe({
      next: (data) => this.setLista(data),
      error: (err) => this.fallo('No se pudo cargar el listado de empresas', err),
    });
  }

  // ------------------------------------------------------------------ ficha

  nueva(): void {
    this.form = FORM_VACIO();
    this.abrirFicha();
  }

  editar(empresa: EmpresaModel): void {
    this.form = { ...empresa };
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

  guardar(): void {
    this.errorFormulario = '';
    this.guardando = true;
    const esEdicion = !!this.form.id;
    const op$ = esEdicion
      ? this.service.actualizar(this.form.id!, this.form)
      : this.service.crear(this.form);
    op$.subscribe({
      next: (guardada) => {
        this.guardando = false;
        this.mensaje = `${guardada?.nombre ?? 'Empresa'} ${esEdicion ? 'actualizada' : 'creada'}.`;
        this.cerrarFicha();
        this.listar();
      },
      error: (err) => {
        console.error('No se pudo guardar la empresa', err);
        this.guardando = false;
        this.errorFormulario = mensajeDeError(err, 'No se pudo guardar');
        this.cdr.detectChanges();
      },
    });
  }

  esSeleccionada(e: EmpresaModel): boolean {
    return this.fichaAbierta && !!this.form.id && (e as EmpresaModel & { id?: string }).id === this.form.id;
  }

  // ---------------------------------------------------------- búsqueda exacta

  buscarPorRuc(): void {
    if (!this.busquedaRuc.trim()) return;
    this.loading = true;
    this.service.obtenerPorRuc(this.busquedaRuc.trim()).subscribe({
      next: (e) => { this.filtrado = true; this.setLista(e ? [e] : []); },
      error: (err) => this.sinResultados(err, `No hay ninguna empresa con el RUC ${this.busquedaRuc.trim()}.`),
    });
  }

  buscarPorId(): void {
    if (!this.busquedaId.trim()) return;
    this.loading = true;
    this.service.obtenerPorId(this.busquedaId.trim()).subscribe({
      next: (e) => { this.filtrado = true; this.setLista(e ? [e] : []); },
      error: (err) => this.sinResultados(err, 'No hay ninguna empresa con ese ID.'),
    });
  }

  private setLista(data: EmpresaModel[]): void {
    this.empresas = [...data];
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
