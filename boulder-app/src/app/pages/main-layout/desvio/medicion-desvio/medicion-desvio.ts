import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, input, output, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { EmpleadoService } from '../../../../services/empleado.service';
import { MaquinaService } from '../../../../services/maquina.service';
import { SondajeService } from '../../../../services/sondaje.service';
import { SurveyMedicionService } from '../../../../services/survey-medicion.service';
import { Maquina, Sondaje, SurveyMedicion } from '../../../../models/operacional';
import { Empleado } from '../../../../models/base';

/** Lo que el formulario edita: la medición con los números que pueden faltar. */
type Formulario = Omit<SurveyMedicion, 'sondajeId' | 'profundidad'> & {
  sondajeId: string | null;
  profundidad: number | null;
};

/** Vista previa del desvío, con el mismo cálculo que `Desviacion` del backend. */
interface Previa {
  azimut: number;
  sentidoAzimut: string | null;
  inclinacion: number;
  sentidoInclinacion: string | null;
  total: number;
  metros: number;
}

/**
 * Formulario de una medición de desvío (pantalla P07, RF08, CU10).
 *
 * Reemplaza el mensaje de WhatsApp con la lectura del survey: se escriben el
 * azimut y la inclinación medidos y el desvío se calcula contra el plan (el
 * setup del sondaje, corregible). La vista previa repite el cálculo del backend
 * para que la decisión se tome viendo el número; lo que se guarda lo calcula el
 * backend.
 */
@Component({
  selector: 'app-medicion-desvio',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './medicion-desvio.html',
  styleUrl: '../../../../components/formulario-parte.scss',
})
export class MedicionDesvioForm implements OnInit {

  private readonly servicio = inject(SurveyMedicionService);
  private readonly sondajeService = inject(SondajeService);
  private readonly maquinaService = inject(MaquinaService);
  private readonly empleadoService = inject(EmpleadoService);

  /** Sondaje con el que se abre; se puede cambiar en el formulario. */
  readonly sondajeInicial = input<string | null>(null);

  readonly guardado = output<SurveyMedicion>();
  readonly cancelado = output<void>();

  readonly hoyIso = this.hoy();
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);

  readonly sondajes = signal<Sondaje[]>([]);
  readonly maquinas = signal<Maquina[]>([]);
  readonly empleados = signal<Empleado[]>([]);

  readonly metodos = ['Reflex EZ-Shot', 'Reflex EZ-Trac', 'Giroscopio', 'Orientación giroscópica'];
  readonly decisiones = [
    { valor: 'continuar', texto: 'Continuar' },
    { valor: 'corregir', texto: 'Corregir' },
    { valor: 'evaluar', texto: 'Evaluar' },
  ];

  f: Formulario = { sondajeId: null, profundidad: null, fechaMedicion: this.hoyIso };

  ngOnInit(): void {
    forkJoin({
      sondajes: this.sondajeService.listar(),
      maquinas: this.maquinaService.listar().pipe(catchError(() => of([] as Maquina[]))),
      empleados: this.empleadoService.listarPorArea('perforacion').pipe(catchError(() => of([] as Empleado[]))),
    }).subscribe({
      next: ({ sondajes, maquinas, empleados }) => {
        this.sondajes.set([...sondajes].sort((a, b) => a.codigo.localeCompare(b.codigo)));
        this.maquinas.set(maquinas);
        this.empleados.set(empleados);
        if (this.sondajeInicial()) {
          this.elegirSondaje(this.sondajeInicial());
        }
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar los sondajes.');
        this.cargando.set(false);
      },
    });
  }

  /** Al elegir el sondaje, el plan y la máquina se toman de su setup. */
  elegirSondaje(id: string | null): void {
    this.f.sondajeId = id;
    const s = this.sondajes().find(x => x.id === id);
    this.f.azimutPlan = s?.azimutSetup;
    this.f.inclinacionPlan = s?.inclinacionSetup;
    this.f.maquinaId = s?.maquinaId ?? this.f.maquinaId;
  }

  /** Cuando hay medidos y plan, el mismo cálculo que el backend. */
  previa(): Previa | null {
    const { profundidad, azimutMedido, inclinacionMedida, azimutPlan, inclinacionPlan } = this.f;
    if ([profundidad, azimutMedido, inclinacionMedida, azimutPlan, inclinacionPlan].some(v => v == null)) {
      return null;
    }
    const azimut = (((azimutMedido! - azimutPlan!) % 360) + 540) % 360 - 180;
    const inclinacion = inclinacionMedida! - inclinacionPlan!;
    const a = this.direccion(azimutMedido!, inclinacionMedida!);
    const b = this.direccion(azimutPlan!, inclinacionPlan!);
    const punto = a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    const cruz = [a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]];
    const total = Math.atan2(Math.hypot(...cruz), punto) * 180 / Math.PI;
    return {
      azimut,
      sentidoAzimut: Math.abs(azimut) < 1e-9 ? null : azimut > 0 ? 'derecha' : 'izquierda',
      inclinacion,
      sentidoInclinacion: Math.abs(inclinacion) < 1e-9 ? null : inclinacion > 0 ? 'arriba' : 'abajo',
      total,
      metros: profundidad! * Math.sin(total * Math.PI / 180),
    };
  }

  /** Los mismos cortes que la lista de desvíos: hasta 3 m en tolerancia, desde 8 m fuera del objetivo. */
  tono(metros: number): string {
    const abs = Math.abs(metros);
    if (abs <= 3) { return 'chip chip--ok'; }
    if (abs <= 8) { return 'chip chip--warn'; }
    return 'chip chip--danger';
  }

  avisos(): string[] {
    const avisos: string[] = [];
    const { profundidad, azimutMedido, inclinacionMedida, azimutPlan, inclinacionPlan } = this.f;
    if (profundidad != null && profundidad < 0) { avisos.push('La profundidad no puede ser negativa.'); }
    for (const [nombre, v] of [['El azimut medido', azimutMedido], ['El azimut planificado', azimutPlan]] as const) {
      if (v != null && (v < 0 || v > 360)) { avisos.push(`${nombre} va de 0 a 360°.`); }
    }
    for (const [nombre, v] of [['La inclinación medida', inclinacionMedida], ['La inclinación planificada', inclinacionPlan]] as const) {
      if (v != null && (v < -90 || v > 90)) { avisos.push(`${nombre} va de −90 a 90°.`); }
    }
    return avisos;
  }

  completo(): boolean {
    return !!this.f.sondajeId && this.f.profundidad != null && !!this.f.fechaMedicion && !this.avisos().length;
  }

  /** El perforista se enlaza si el nombre coincide con un empleado; si no, queda el texto. */
  enlazarPerforista(): void {
    const nombre = (this.f.responsable ?? '').trim().toLowerCase();
    this.f.perforistaId = this.empleados()
      .find(e => [e.nombres, e.apellidos].filter(Boolean).join(' ').toLowerCase() === nombre)?.id;
  }

  nombreCompleto(e: Empleado): string {
    return [e.nombres, e.apellidos].filter(Boolean).join(' ');
  }

  guardar(): void {
    if (!this.completo() || this.guardando()) { return; }
    this.guardando.set(true);
    this.error.set(null);

    // Los desvíos no se mandan: con medidos y plan los calcula el backend.
    const datos = {
      ...this.f,
      desvAzimut: undefined, desvAzimutSentido: undefined, desvInclinacion: undefined,
      desvInclinacionSentido: undefined, desvTotal: undefined, desvMetros: undefined,
    } as SurveyMedicion;
    this.servicio.crear(datos).subscribe({
      next: guardada => {
        this.guardando.set(false);
        this.guardado.emit(guardada);
      },
      error: (e: HttpErrorResponse) => {
        this.guardando.set(false);
        this.error.set(e.error?.message ?? 'No se pudo guardar la medición.');
      },
    });
  }

  /** Vector unitario (este, norte, arriba); la inclinación es negativa hacia abajo. */
  private direccion(azimut: number, inclinacion: number): number[] {
    const az = azimut * Math.PI / 180;
    const inc = inclinacion * Math.PI / 180;
    return [Math.cos(inc) * Math.sin(az), Math.cos(inc) * Math.cos(az), Math.sin(inc)];
  }

  private hoy(): string {
    // Fecha local, no UTC: a las 8 p. m. en Lima, toISOString ya da el día siguiente.
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }
}
