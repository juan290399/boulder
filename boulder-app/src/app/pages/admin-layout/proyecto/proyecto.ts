import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ProyectoService } from '../../../services/proyecto.service';
import { EmpresaService } from '../../../services/empresa.service';
import { Proyecto as ProyectoModel, Empresa } from '../../../models/operacional';

const FORM_VACIO = (): ProyectoModel => ({
  empresaId: '',
  codigo: '',
  nombre: '',
  ubicacion: '',
  srid: 32718,
});

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

  proyectos: ProyectoModel[] = [];
  empresas: Empresa[] = [];
  form: ProyectoModel & { id?: string } = FORM_VACIO();

  busquedaCodigo = '';
  busquedaId = '';
  loading = false;
  mensaje = '';

  ngOnInit(): void {
    this.empresaService.listar().subscribe({
      next: (data) => {
        this.empresas = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('No se pudieron cargar las empresas', err),
    });
    this.listar();
  }

  listar(): void {
    this.loading = true;
    this.service.listar().subscribe({
      next: (data) => this.setLista(data),
      error: (err) => this.fallo('No se pudo cargar el listado de proyectos', err),
    });
  }

  guardar(): void {
    const op$ = this.form.id
      ? this.service.actualizar(this.form.id, this.form)
      : this.service.crear(this.form);
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
    if (!this.busquedaCodigo.trim()) return;
    this.loading = true;
    this.service.obtenerPorCodigo(this.busquedaCodigo.trim()).subscribe({
      next: (p) => this.setLista(p ? [p] : []),
      error: (err) => this.fallo('Sin resultados por código', err),
    });
  }

  buscarPorId(): void {
    if (!this.busquedaId.trim()) return;
    this.loading = true;
    this.service.obtenerPorId(this.busquedaId.trim()).subscribe({
      next: (p) => this.setLista(p ? [p] : []),
      error: (err) => this.fallo('Sin resultados por ID', err),
    });
  }

  nombreEmpresa(id?: string): string {
    return this.empresas.find((e) => e.id === id)?.nombre ?? '—';
  }

  editar(proyecto: ProyectoModel): void {
    this.form = { ...proyecto };
    this.cdr.detectChanges();
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

  private fallo(msg: string, err: unknown): void {
    console.error(msg, err);
    this.mensaje = msg;
    this.loading = false;
    this.cdr.detectChanges();
  }
}
