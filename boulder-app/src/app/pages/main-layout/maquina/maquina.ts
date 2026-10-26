import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { MaquinaService } from '../../../services/maquina.service';
import { Maquina as MaquinaModel } from '../../../models/operacional';

const FORM_VACIO = (): MaquinaModel => ({
  codigo: '',
  nombre: '',
  tipo: '',
  marca: '',
  motor: '',
  estado: 'operativa',
});

@Component({
  selector: 'app-maquina',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './maquina.html',
  styleUrl: './maquina.scss',
})
export class Maquina implements OnInit {
  private readonly service = inject(MaquinaService);
  private readonly cdr = inject(ChangeDetectorRef);

  maquinas: MaquinaModel[] = [];
  form: MaquinaModel & { id?: string } = FORM_VACIO();

  busquedaCodigo = '';
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
      error: (err) => this.fallo('No se pudo cargar el listado de máquinas', err),
    });
  }

  guardar(): void {
    const op$ = this.form.id
      ? this.service.actualizar(this.form.id, this.form)
      : this.service.crear(this.form);
    op$.subscribe({
      next: () => {
        this.mensaje = this.form.id ? 'Máquina actualizada' : 'Máquina creada';
        this.limpiar();
        this.listar();
      },
      error: (err) => this.fallo('No se pudo guardar la máquina', err),
    });
  }

  buscarPorCodigo(): void {
    if (!this.busquedaCodigo.trim()) return;
    this.loading = true;
    this.service.obtenerPorCodigo(this.busquedaCodigo.trim()).subscribe({
      next: (m) => this.setLista(m ? [m] : []),
      error: (err) => this.fallo('Sin resultados por código', err),
    });
  }

  buscarPorId(): void {
    if (!this.busquedaId.trim()) return;
    this.loading = true;
    this.service.obtenerPorId(this.busquedaId.trim()).subscribe({
      next: (m) => this.setLista(m ? [m] : []),
      error: (err) => this.fallo('Sin resultados por ID', err),
    });
  }

  editar(maquina: MaquinaModel): void {
    this.form = { ...maquina };
    this.cdr.detectChanges();
  }

  limpiar(): void {
    this.form = FORM_VACIO();
    this.cdr.detectChanges();
  }

  private setLista(data: MaquinaModel[]): void {
    this.maquinas = [...data];
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
