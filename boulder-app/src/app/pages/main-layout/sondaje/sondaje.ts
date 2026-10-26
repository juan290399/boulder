import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
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

@Component({
  selector: 'app-sondaje',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './sondaje.html',
  styleUrl: './sondaje.scss',
})
export class Sondaje implements OnInit {
  private readonly service = inject(SondajeService);
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

  busquedaCodigo = '';
  busquedaId = '';
  loading = false;
  mensaje = '';

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
      error: (err) => console.error('No se pudieron cargar los catálogos', err),
    });
    this.listar();
  }

  listar(): void {
    this.loading = true;
    this.service.listar().subscribe({
      next: (data) => this.setLista(data),
      error: (err) => this.fallo('No se pudo cargar el listado de sondajes', err),
    });
  }

  guardar(): void {
    const op$ = this.form.id
      ? this.service.actualizar(this.form.id, this.form)
      : this.service.crear(this.form);
    op$.subscribe({
      next: () => {
        this.mensaje = this.form.id ? 'Sondaje actualizado' : 'Sondaje registrado';
        this.limpiar();
        this.listar();
      },
      error: (err) => this.fallo('No se pudo guardar el sondaje', err),
    });
  }

  buscarPorCodigo(): void {
    if (!this.busquedaCodigo.trim()) return;
    this.loading = true;
    this.service.obtenerPorCodigo(this.busquedaCodigo.trim()).subscribe({
      next: (s) => this.setLista(s ? [s] : []),
      error: (err) => this.fallo('Sin resultados por código', err),
    });
  }

  buscarPorId(): void {
    if (!this.busquedaId.trim()) return;
    this.loading = true;
    this.service.obtenerPorId(this.busquedaId.trim()).subscribe({
      next: (s) => this.setLista(s ? [s] : []),
      error: (err) => this.fallo('Sin resultados por ID', err),
    });
  }

  nombreProyecto(id?: string): string {
    return this.proyectos.find((p) => p.id === id)?.nombre ?? '—';
  }

  editar(sondaje: SondajeModel): void {
    this.form = { ...sondaje };
    this.cdr.detectChanges();
  }

  limpiar(): void {
    this.form = FORM_VACIO();
    this.cdr.detectChanges();
  }

  private setLista(data: SondajeModel[]): void {
    this.sondajes = [...data];
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
