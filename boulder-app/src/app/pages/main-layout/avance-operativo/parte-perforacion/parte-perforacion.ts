import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, input, output, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { AvancePerforacionService } from '../../../../services/avance-perforacion.service';
import { EmpleadoService } from '../../../../services/empleado.service';
import { MaquinaService } from '../../../../services/maquina.service';
import { PlataformaService } from '../../../../services/plataforma.service';
import { AvancePerforacion, AvancePersonal, Maquina, Plataforma } from '../../../../models/operacional';
import { Empleado } from '../../../../models/base';

/** Lo que el formulario edita: el parte sin el avance, que se calcula. */
type Formulario = Omit<AvancePerforacion, 'avance' | 'desde' | 'hasta'> & {
  desde: number | null;
  hasta: number | null;
};

/**
 * Formulario del parte de perforación por turno (pantalla P04, RF05).
 *
 * Reemplaza el mensaje de WhatsApp de cierre de turno. Se abre precargado con
 * el borrador que arma el backend (desde = el fondo del último parte, con la
 * misma máquina y la misma cuadrilla), así que en un turno normal solo se
 * escribe la profundidad final.
 *
 * El avance no se teclea: es hasta menos desde. Las reglas se revisan aquí
 * para avisar mientras se escribe, pero quien decide es el backend
 * (`ParteTurno`), el mismo que revisa la carga desde Excel.
 */
@Component({
  selector: 'app-parte-perforacion',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './parte-perforacion.html',
  styleUrl: '../formulario-parte.scss',
})
export class PartePerforacionForm implements OnInit {

  private readonly servicio = inject(AvancePerforacionService);
  private readonly maquinaService = inject(MaquinaService);
  private readonly plataformaService = inject(PlataformaService);
  private readonly empleadoService = inject(EmpleadoService);

  /** El sondaje del parte. */
  readonly sondajeId = input.required<string>();
  readonly sondajeCodigo = input<string>('');
  /** El parte a corregir; sin él, el formulario arma uno nuevo. */
  readonly parte = input<AvancePerforacion | null>(null);

  readonly guardado = output<AvancePerforacion>();
  readonly cancelado = output<void>();

  readonly hoyIso = this.hoy();
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);

  readonly maquinas = signal<Maquina[]>([]);
  readonly plataformas = signal<Plataforma[]>([]);
  readonly empleados = signal<Empleado[]>([]);

  /** Diámetros de la línea diamantina; el 3 es triple tubo. Se sugieren, no se imponen. */
  readonly lineas = ['PQ', 'PQ3', 'HQ', 'HQ3', 'NQ', 'NQ3', 'BQ'];
  readonly cargos = ['Perforista', 'Ayudante', 'Supervisor'];

  f: Formulario = this.vacio();

  ngOnInit(): void {
    const existente = this.parte();
    // Si un catálogo no carga, el formulario igual sirve: solo pierde ese combo.
    forkJoin({
      maquinas: this.maquinaService.listar().pipe(catchError(() => of([] as Maquina[]))),
      plataformas: this.plataformaService.listar().pipe(catchError(() => of([] as Plataforma[]))),
      empleados: this.empleadoService.listarPorArea('perforacion').pipe(catchError(() => of([] as Empleado[]))),
      base: existente ? of(existente) : this.servicio.sugerencia(this.sondajeId()),
    }).subscribe({
      next: ({ maquinas, plataformas, empleados, base }) => {
        this.maquinas.set(maquinas);
        this.plataformas.set(plataformas);
        this.empleados.set(empleados);
        this.f = {
          ...this.vacio(),
          ...this.sinNulos(base),
          // Uno nuevo es de hoy; uno que se corrige conserva su fecha y turno.
          fecha: existente ? base.fecha : this.hoyIso,
          turno: existente ? base.turno : 'dia',
          hasta: existente ? base.hasta : null,
          personal: (base.personal ?? []).map(p => ({ ...p })),
        };
        this.cargando.set(false);
      },
      error: (e: HttpErrorResponse) => {
        this.error.set(e.error?.message ?? 'No se pudo preparar el parte.');
        this.cargando.set(false);
      },
    });
  }

  get editando(): boolean {
    return !!this.parte()?.id;
  }

  /** Metros del turno, redondeados al centímetro como en la base. */
  avance(): number | null {
    if (this.f.desde == null || this.f.hasta == null) { return null; }
    return Math.round((this.f.hasta - this.f.desde) * 100) / 100;
  }

  /** Las mismas reglas que `ParteTurno` del backend, para avisar antes de enviar. */
  avisos(): string[] {
    const { desde, hasta, retornoPct, casingDesde, casingHasta } = this.f;
    const avisos: string[] = [];
    if (desde != null && desde < 0) { avisos.push('La profundidad inicial no puede ser negativa.'); }
    if (desde != null && hasta != null && hasta < desde) {
      avisos.push(`La profundidad final (${hasta}) es menor que la inicial (${desde}).`);
    }
    if (retornoPct != null && (retornoPct < 0 || retornoPct > 100)) {
      avisos.push('El retorno de agua es un porcentaje entre 0 y 100.');
    }
    if (casingDesde != null && casingHasta != null && casingHasta < casingDesde) {
      avisos.push('El casing termina antes de empezar.');
    }
    if (casingHasta != null && hasta != null && casingHasta > hasta) {
      avisos.push(`El casing baja a ${casingHasta} m y el pozo llega a ${hasta} m.`);
    }
    return avisos;
  }

  completo(): boolean {
    return !!this.f.fecha && this.f.desde != null && this.f.hasta != null && !this.avisos().length;
  }

  agregarIntegrante(): void {
    this.f.personal = [...this.f.personal, { nombre: '', cargo: 'Ayudante' }];
  }

  quitarIntegrante(i: number): void {
    this.f.personal = this.f.personal.filter((_, j) => j !== i);
  }

  /**
   * Si el nombre escrito coincide con un empleado dado de alta, se enlaza; si
   * no, se guarda el texto igual. Exigir el alta previa de cada ayudante dejaría
   * el parte sin cargar.
   */
  enlazar(p: AvancePersonal): void {
    const nombre = p.nombre.trim().toLowerCase();
    p.perforistaId = this.empleados().find(e => this.nombreCompleto(e).toLowerCase() === nombre)?.id;
  }

  nombreCompleto(e: Empleado): string {
    return [e.nombres, e.apellidos].filter(Boolean).join(' ');
  }

  guardar(): void {
    if (!this.completo() || this.guardando()) { return; }
    this.guardando.set(true);
    this.error.set(null);

    const datos: AvancePerforacion = {
      ...this.f,
      sondajeId: this.sondajeId(),
      desde: this.f.desde!,
      hasta: this.f.hasta!,
      avance: this.avance()!,
      personal: this.f.personal.filter(p => p.nombre.trim()),
    };
    const id = this.parte()?.id;
    const peticion = id ? this.servicio.actualizar(id, datos) : this.servicio.crear(datos);

    peticion.subscribe({
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

  /**
   * El backend manda `null` en lo vacío y los combos usan `undefined` para «—»:
   * sin esto, una plataforma sin asignar deja el combo en blanco en vez de «—».
   */
  private sinNulos(parte: AvancePerforacion): AvancePerforacion {
    return Object.fromEntries(Object.entries(parte).map(([k, v]) => [k, v ?? undefined])) as AvancePerforacion;
  }

  private vacio(): Formulario {
    return { fecha: this.hoyIso, turno: 'dia', desde: null, hasta: null, personal: [] };
  }

  private hoy(): string {
    // Fecha local, no UTC: a las 8 p. m. en Lima, toISOString ya da el día siguiente.
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }
}
