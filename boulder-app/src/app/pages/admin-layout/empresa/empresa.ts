import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { EmpresaService } from '../../../services/empresa.service';
import { Empresa as EmpresaModel } from '../../../models/operacional';

const FORM_VACIO = (): EmpresaModel => ({
  codigo: '',
  nombre: '',
  ruc: '',
  direccion: '',
  telefono: '',
  correo: '',
});

@Component({
  selector: 'app-empresa',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './empresa.html',
  styleUrl: './empresa.scss',
})
export class Empresa implements OnInit {
  private readonly service = inject(EmpresaService);
  private readonly cdr = inject(ChangeDetectorRef);

  empresas: EmpresaModel[] = [];
  form: EmpresaModel & { id?: string } = FORM_VACIO();

  busquedaRuc = '';
  busquedaId = '';
  /** Arranca en true: el listado se pide en ngOnInit. */
  loading = true;
  mensaje = '';
  error = '';
  /** El listado muestra el resultado de una búsqueda, no todas las empresas. */
  filtrado = false;

  ngOnInit(): void {
    this.listar();
  }

  listar(): void {
    this.loading = true;
    this.filtrado = false;
    this.busquedaRuc = '';
    this.busquedaId = '';
    this.service.listar().subscribe({
      next: (data) => this.setLista(data),
      error: (err) => this.fallo('No se pudo cargar el listado de empresas', err),
    });
  }

  guardar(): void {
    this.limpiarAvisos();
    const op$ = this.form.id
      ? this.service.actualizar(this.form.id, this.form)
      : this.service.crear(this.form);
    op$.subscribe({
      next: () => {
        this.mensaje = this.form.id ? 'Empresa actualizada' : 'Empresa creada';
        this.limpiar();
        this.listar();
      },
      error: (err) => this.fallo('No se pudo guardar la empresa', err),
    });
  }

  buscarPorRuc(): void {
    if (!this.busquedaRuc.trim()) return;
    this.loading = true;
    this.service.obtenerPorRuc(this.busquedaRuc.trim()).subscribe({
      next: (e) => { this.filtrado = true; this.setLista(e ? [e] : []); },
      error: (err) => this.sinResultados(err, `No hay ninguna empresa con el RUC ${this.busquedaRuc.trim()}`),
    });
  }

  buscarPorId(): void {
    if (!this.busquedaId.trim()) return;
    this.loading = true;
    this.service.obtenerPorId(this.busquedaId.trim()).subscribe({
      next: (e) => { this.filtrado = true; this.setLista(e ? [e] : []); },
      error: (err) => this.sinResultados(err, 'No hay ninguna empresa con ese ID'),
    });
  }

  editar(empresa: EmpresaModel): void {
    this.form = { ...empresa };
    this.limpiarAvisos();
    this.cdr.detectChanges();
    document.getElementById('emp-formulario')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    document.getElementById('emp-codigo')?.focus({ preventScroll: true });
  }

  limpiar(): void {
    this.form = FORM_VACIO();
    this.cdr.detectChanges();
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
      this.setLista([]);
      this.mensaje = texto;
      return;
    }
    this.fallo('No se pudo hacer la búsqueda', err);
  }

  /** Prefiere el mensaje del servidor, que dice qué campo corregir. */
  private fallo(msg: string, err: unknown): void {
    console.error(msg, err);
    let detalle = '';
    if (err instanceof HttpErrorResponse) {
      const errores = err.error?.errores;
      detalle = err.status === 403
        ? 'No tienes permiso para esta operación.'
        : errores && typeof errores === 'object'
          ? Object.values(errores as Record<string, string>).join(' · ')
          : (err.error?.message ?? '');
    }
    this.error = detalle ? `${msg}: ${detalle}` : msg;
    this.loading = false;
    this.cdr.detectChanges();
  }
}
