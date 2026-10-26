import { CommonModule } from '@angular/common';
import { Component, Injector, OnInit, inject, input, output, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

import { AvanceOperativo } from '../../../../models/operacional';
import { CampoParte, FORMULARIOS, FormularioParte } from './formularios';

/** El parte mientras se edita: cualquier campo del detalle, más la cabecera. */
type Formulario = Record<string, unknown> & { fecha: string; turno?: string | null; observaciones?: string | null };

/**
 * Formulario único de los siete partes de testigo (pantallas P05 a P07).
 *
 * Los campos salen de {@link FORMULARIOS}; este componente solo sabe pintar un
 * campo según su tipo, precargar el parte nuevo a partir del último y revisar
 * los rangos. Las reglas definitivas las aplica el backend.
 */
@Component({
  selector: 'app-parte-operativo',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './parte-operativo.html',
  styleUrl: '../../../../components/formulario-parte.scss',
})
export class ParteOperativoForm implements OnInit {

  private readonly injector = inject(Injector);

  /** Clave del parte en {@link FORMULARIOS}: recepcion, logueo, fotografia… */
  readonly clave = input.required<string>();
  readonly etiqueta = input<string>('');
  readonly sondajeId = input.required<string>();
  readonly sondajeCodigo = input<string>('');
  /** El parte a corregir; sin él, se arma uno nuevo. */
  readonly parte = input<AvanceOperativo | null>(null);
  /** Los partes ya cargados de este tipo, en orden; el último da la precarga. */
  readonly anteriores = input<AvanceOperativo[]>([]);

  readonly guardado = output<AvanceOperativo>();
  readonly cancelado = output<void>();

  readonly hoyIso = this.hoy();
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);

  /** Diámetros de la línea diamantina; el 3 es triple tubo. */
  readonly lineas = ['PQ', 'PQ3', 'HQ', 'HQ3', 'NQ', 'NQ3', 'BQ'];

  config!: FormularioParte;
  f: Formulario = { fecha: this.hoyIso, turno: 'dia' };

  ngOnInit(): void {
    this.config = FORMULARIOS[this.clave()];
    const existente = this.parte();
    this.f = existente
      ? { ...(existente as unknown as Formulario) }
      // Todos los campos arrancan en null, no ausentes: así los combos muestran «—».
      : { fecha: this.hoyIso, turno: 'dia', ...Object.fromEntries(this.config.campos.map(c => [c.campo, null])),
          ...this.precarga() };
  }

  get editando(): boolean {
    return !!this.parte()?.id;
  }

  /**
   * Lo que se hereda del último parte: los campos que se repiten tal cual y los
   * que continúan (el tramo nuevo empieza donde terminó el anterior; la caja,
   * en la siguiente).
   */
  private precarga(): Record<string, unknown> {
    const lista = this.anteriores();
    const ultimo = lista.length ? (lista[lista.length - 1] as unknown as Record<string, unknown>) : null;
    if (!ultimo) { return {}; }

    const datos: Record<string, unknown> = {};
    for (const campo of this.config.copia) {
      if (ultimo[campo] != null) { datos[campo] = ultimo[campo]; }
    }
    for (const [campo, origen] of Object.entries(this.config.continua)) {
      const valor = ultimo[origen];
      if (typeof valor === 'number') {
        datos[campo] = this.tipoDe(campo) === 'entero' ? valor + 1 : valor;
      }
    }
    return datos;
  }

  valor(campo: string): unknown {
    return this.f[campo];
  }

  numero(campo: string): number | null {
    const v = this.f[campo];
    return typeof v === 'number' ? v : null;
  }

  /** Al cambiar un extremo de un rango, se recalcula el total que depende de él. */
  cambiar(campo: string, valor: unknown): void {
    this.f[campo] = valor === '' ? null : valor;
    for (const d of this.config.deriva) {
      if (d.desde !== campo && d.hasta !== campo) { continue; }
      const desde = this.f[d.desde];
      const hasta = this.f[d.hasta];
      // Con el rango invertido no se deduce nada: el aviso del rango ya lo señala.
      if (typeof desde === 'number' && typeof hasta === 'number' && hasta >= desde) {
        // Las cajas se cuentan incluyendo las dos puntas: de la 12 a la 18 son 7.
        this.f[d.campo] = d.cajas ? hasta - desde + 1 : Math.round((hasta - desde) * 100) / 100;
      }
    }
  }

  avisos(): string[] {
    const avisos: string[] = [];
    for (const [desde, hasta, nombre] of this.config.rangos) {
      const a = this.f[desde];
      const b = this.f[hasta];
      if (typeof a === 'number' && typeof b === 'number' && b < a) {
        avisos.push(`${nombre}: termina en ${b}, antes de empezar (${a}).`);
      }
    }
    for (const c of this.config.campos) {
      const v = this.f[c.campo];
      if (typeof v !== 'number') { continue; }
      if (v < 0) { avisos.push(`${c.etiqueta}: no puede ser negativo.`); }
      if (c.tipo === 'porcentaje' && v > 100) { avisos.push(`${c.etiqueta}: no puede pasar de 100.`); }
    }
    return avisos;
  }

  completo(): boolean {
    const faltan = this.config.campos.some(c => c.requerido && (this.f[c.campo] == null || this.f[c.campo] === ''));
    return !!this.f.fecha && !faltan && !this.avisos().length;
  }

  guardar(): void {
    if (!this.completo() || this.guardando()) { return; }
    this.guardando.set(true);
    this.error.set(null);

    const datos = { ...this.f, sondajeId: this.sondajeId() } as unknown as AvanceOperativo;
    const api = this.injector.get(this.config.servicio);
    const id = this.parte()?.id;
    (id ? api.actualizar(id, datos) : api.crear(datos)).subscribe({
      next: guardado => {
        this.guardando.set(false);
        this.guardado.emit(guardado);
      },
      error: (e: HttpErrorResponse) => {
        this.guardando.set(false);
        this.error.set(e.error?.message ?? 'No se pudo guardar el parte.');
      },
    });
  }

  ancho(c: CampoParte): string {
    return `col-12 col-sm-6 col-md-${c.ancho ?? 3}`;
  }

  private tipoDe(campo: string): string | undefined {
    return this.config.campos.find(c => c.campo === campo)?.tipo;
  }

  private hoy(): string {
    // Fecha local, no UTC: a las 8 p. m. en Lima, toISOString ya da el día siguiente.
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }
}
