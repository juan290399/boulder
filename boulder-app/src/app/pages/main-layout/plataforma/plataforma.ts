import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { PlataformaService } from '../../../services/plataforma.service';
import { ProyectoService } from '../../../services/proyecto.service';
import { Plataforma as PlataformaModel, Proyecto } from '../../../models/operacional';

const FORM_VACIO = (): PlataformaModel => ({
  proyectoId: '',
  codigo: '',
  nombre: '',
  este: undefined,
  norte: undefined,
  elevacion: undefined,
  numeroPozas: 0,
  estado: 'activa',
  tipo: '',
  ubicacion: '',
  notas: '',
});

@Component({
  selector: 'app-plataforma',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './plataforma.html',
  styleUrl: './plataforma.scss',
})
export class Plataforma implements OnInit {
  private readonly service = inject(PlataformaService);
  private readonly proyectoService = inject(ProyectoService);
  private readonly cdr = inject(ChangeDetectorRef);

  plataformas: PlataformaModel[] = [];
  proyectos: Proyecto[] = [];
  form: PlataformaModel & { id?: string } = FORM_VACIO();

  busquedaCodigo = '';
  busquedaId = '';
  loading = false;
  mensaje = '';

  ngOnInit(): void {
    this.proyectoService.listar().subscribe({
      next: (data) => {
        this.proyectos = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('No se pudieron cargar los proyectos', err),
    });
    this.listar();
  }

  listar(): void {
    this.loading = true;
    this.service.listar().subscribe({
      next: (data) => this.setLista(data),
      error: (err) => this.fallo('No se pudo cargar el listado de plataformas', err),
    });
  }

  guardar(): void {
    const op$ = this.form.id
      ? this.service.actualizar(this.form.id, this.form)
      : this.service.crear(this.form);
    op$.subscribe({
      next: () => {
        this.mensaje = this.form.id ? 'Plataforma actualizada' : 'Plataforma creada';
        this.limpiar();
        this.listar();
      },
      error: (err) => this.fallo('No se pudo guardar la plataforma', err),
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

  nombreProyecto(id?: string): string {
    return this.proyectos.find((p) => p.id === id)?.nombre ?? '—';
  }

  editar(plataforma: PlataformaModel): void {
    this.form = { ...plataforma };
    this.cdr.detectChanges();
  }

  limpiar(): void {
    this.form = FORM_VACIO();
    this.cdr.detectChanges();
  }

  private setLista(data: PlataformaModel[]): void {
    this.plataformas = [...data];
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
