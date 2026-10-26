import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

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
  loading = false;
  mensaje = '';

  ngOnInit(): void {
    this.listar();
  }

  listar(): void {
    this.loading = true;
    this.service.listar().subscribe({
      next: (data) => this.setLista(data),
      error: (err) => this.fallo('No se pudo cargar el listado de empresas', err),
    });
  }

  guardar(): void {
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
      next: (e) => this.setLista(e ? [e] : []),
      error: (err) => this.fallo('Sin resultados por RUC', err),
    });
  }

  buscarPorId(): void {
    if (!this.busquedaId.trim()) return;
    this.loading = true;
    this.service.obtenerPorId(this.busquedaId.trim()).subscribe({
      next: (e) => this.setLista(e ? [e] : []),
      error: (err) => this.fallo('Sin resultados por ID', err),
    });
  }

  editar(empresa: EmpresaModel): void {
    this.form = { ...empresa };
    this.cdr.detectChanges();
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

  private fallo(msg: string, err: unknown): void {
    console.error(msg, err);
    this.mensaje = msg;
    this.loading = false;
    this.cdr.detectChanges();
  }
}
