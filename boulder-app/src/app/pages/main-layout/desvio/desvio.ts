import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { DesviacionService } from '../../../services/desviacion.service';
import { SurveyMedicionService } from '../../../services/survey-medicion.service';
import { SondajeService } from '../../../services/sondaje.service';
import {
  GraficoTrayectoria,
  PuntoTrayectoria,
} from '../../../components/grafico-trayectoria/grafico-trayectoria';
import {
  DesviacionMaquina,
  DesviacionPerforista,
  DesviacionSondaje,
  Sondaje,
  TrayectoriaSondaje,
} from '../../../models/operacional';

/** Las cuatro miradas sobre el mismo dato de desviación. */
type Vista = 'sondaje' | 'perforista' | 'maquina' | 'trayectoria';

/** Lo que se edita del seguimiento, sin arrastrar el resto de la medición. */
interface FormSeguimiento {
  interpretacion: string;
  instruccion: string;
  responsableSeguimiento: string;
  estadoSeguimiento: string;
}

/**
 * Desvíos: cuánto se apartó cada sondaje de su trayectoria planificada, y qué
 * se decidió hacer al respecto.
 *
 * El mismo juego de mediciones se puede leer de tres maneras y cada una
 * responde una pregunta distinta: por sondaje, qué agujero se está yendo; por
 * perforista y por máquina, si el desvío es del terreno o de quien perfora.
 *
 * Al elegir una medición se abre lo que hoy circula por WhatsApp: las dos
 * gráficas de planificado contra medido, y debajo la interpretación, la
 * instrucción para la máquina y a quién le toca el seguimiento. Sin esa parte
 * la pantalla informa pero no reemplaza al mensaje, porque lo que la gente lee
 * del mensaje es la instrucción, no el número.
 */
@Component({
  selector: 'app-desvio',
  standalone: true,
  imports: [CommonModule, FormsModule, GraficoTrayectoria],
  templateUrl: './desvio.html',
  styleUrl: './desvio.scss',
})
export class Desvio implements OnInit {

  private readonly service = inject(DesviacionService);
  private readonly medicionService = inject(SurveyMedicionService);
  private readonly sondajeService = inject(SondajeService);

  readonly vista = signal<Vista>('sondaje');
  readonly cargando = signal(false);
  readonly error = signal<string | null>(null);

  readonly porSondaje = signal<DesviacionSondaje[]>([]);
  readonly porPerforista = signal<DesviacionPerforista[]>([]);
  readonly porMaquina = signal<DesviacionMaquina[]>([]);

  readonly seleccionada = signal<DesviacionSondaje | null>(null);
  readonly trayectoria = signal<TrayectoriaSondaje | null>(null);
  readonly cargandoTrayectoria = signal(false);
  readonly guardando = signal(false);
  readonly aviso = signal<string | null>(null);

  /** Sondajes que tienen trayectoria medida, para la vista de curvas. */
  readonly sondajes = signal<Sondaje[]>([]);

  filtroCodigo = '';
  sondajeGrafico = '';

  form: FormSeguimiento = {
    interpretacion: '',
    instruccion: '',
    responsableSeguimiento: '',
    estadoSeguimiento: 'sin_seguimiento',
  };

  /** Serie de azimut medido para la gráfica. */
  readonly serieAzimut = computed<PuntoTrayectoria[]>(() =>
    (this.trayectoria()?.estaciones ?? [])
      .map(e => ({ profundidad: e.profundidad, valor: e.azimut })));

  /** Serie de inclinación medida para la gráfica. */
  readonly serieInclinacion = computed<PuntoTrayectoria[]>(() =>
    (this.trayectoria()?.estaciones ?? [])
      .map(e => ({ profundidad: e.profundidad, valor: e.inclinacion })));

  ngOnInit(): void {
    this.cargar();
  }

  cambiarVista(vista: Vista): void {
    this.vista.set(vista);
    this.seleccionada.set(null);
    this.trayectoria.set(null);
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    const fallo = () => {
      this.error.set('No se pudieron cargar las desviaciones.');
      this.cargando.set(false);
    };

    switch (this.vista()) {
      case 'sondaje':
        this.service.porSondaje(this.filtroCodigo || undefined).subscribe({
          next: d => { this.porSondaje.set(d); this.cargando.set(false); },
          error: fallo,
        });
        break;
      case 'perforista':
        this.service.porPerforista().pipe(catchError(() => of([] as DesviacionPerforista[])))
          .subscribe(d => { this.porPerforista.set(d); this.cargando.set(false); });
        break;
      case 'maquina':
        this.service.porMaquina().pipe(catchError(() => of([] as DesviacionMaquina[])))
          .subscribe(d => { this.porMaquina.set(d); this.cargando.set(false); });
        break;
      case 'trayectoria':
        if (this.sondajes().length) {
          this.cargando.set(false);
          break;
        }
        this.sondajeService.listar().pipe(catchError(() => of([] as Sondaje[])))
          .subscribe(lista => {
            this.sondajes.set(lista);
            this.cargando.set(false);
          });
        break;
    }
  }

  limpiarFiltro(): void {
    this.filtroCodigo = '';
    this.cargar();
  }

  /**
   * Carga la trayectoria del sondaje elegido en la vista de curvas.
   *
   * Esta vista existe porque la trayectoria es una propiedad del sondaje y no
   * del evento de desvío. Hoy los dos conjuntos no se tocan: 27 sondajes
   * tienen estaciones de survey y 4 tienen evento de desvío registrado, y
   * ninguno está en las dos listas. Sin esta vista las curvas quedarían
   * escritas y sin forma de verlas.
   */
  graficarSondaje(): void {
    if (!this.sondajeGrafico) {
      this.trayectoria.set(null);
      return;
    }
    this.cargandoTrayectoria.set(true);
    this.service.trayectoria(this.sondajeGrafico)
      .pipe(catchError(() => of(null)))
      .subscribe(t => {
        this.trayectoria.set(t);
        this.cargandoTrayectoria.set(false);
      });
  }

  seleccionar(fila: DesviacionSondaje): void {
    // Se compara la fila en sí y no su medicionId: la vista puede devolver el
    // id vacío —lo hacía antes de exponerlo—, y entonces `undefined ===
    // undefined` daba verdadero y el panel se cerraba en el mismo clic que lo
    // abría, sin ningún error a la vista.
    if (this.seleccionada() === fila) {
      this.seleccionada.set(null);
      this.trayectoria.set(null);
      return;
    }

    this.seleccionada.set(fila);
    this.aviso.set(null);
    this.form = {
      interpretacion: fila.interpretacion ?? '',
      instruccion: fila.instruccion ?? '',
      responsableSeguimiento: fila.responsableSeguimiento ?? '',
      estadoSeguimiento: fila.estadoSeguimiento ?? 'sin_seguimiento',
    };

    if (!fila.sondajeId) {
      this.trayectoria.set(null);
      return;
    }

    this.cargandoTrayectoria.set(true);
    this.service.trayectoria(fila.sondajeId)
      .pipe(catchError(() => of(null)))
      .subscribe(t => {
        this.trayectoria.set(t);
        this.cargandoTrayectoria.set(false);
      });
  }

  /**
   * Guarda el seguimiento sin tocar el resto de la medición.
   *
   * Se relee la medición completa y se le aplican los cuatro campos antes de
   * devolverla: el endpoint es un PUT y reemplaza el recurso entero, así que
   * mandar solo lo editado borraría todo lo demás.
   */
  guardarSeguimiento(): void {
    const fila = this.seleccionada();
    const id = fila?.medicionId;
    if (!id) { return; }

    this.guardando.set(true);
    this.aviso.set(null);

    this.medicionService.obtenerPorId(id).subscribe({
      next: medicion => {
        const actualizada = {
          ...medicion,
          interpretacion: this.form.interpretacion || null,
          instruccion: this.form.instruccion || null,
          responsableSeguimiento: this.form.responsableSeguimiento || null,
          estadoSeguimiento: this.form.estadoSeguimiento,
        };
        this.medicionService.actualizar(id, actualizada).subscribe({
          next: () => {
            this.guardando.set(false);
            this.aviso.set('Seguimiento guardado.');
            this.cargar();
          },
          error: () => {
            this.guardando.set(false);
            this.error.set('No se pudo guardar el seguimiento.');
          },
        });
      },
      error: () => {
        this.guardando.set(false);
        this.error.set('No se pudo leer la medición para actualizarla.');
      },
    });
  }

  /**
   * Severidad de una desviación total, en metros.
   *
   * Los cortes siguen la práctica habitual en sondaje diamantino: hasta 3 m de
   * separación respecto del trazo planificado se considera dentro de
   * tolerancia, y a partir de 8 m el agujero puede estar fuera del objetivo
   * geológico. Son valores de referencia y ninguno de los reportes de la
   * campaña los declara; cuando se fijen los propios, se cambian acá.
   */
  tonoDesvio(metros: number | null | undefined): string {
    if (metros === null || metros === undefined) { return 'chip'; }
    const abs = Math.abs(metros);
    if (abs <= 3) { return 'chip chip--ok'; }
    if (abs <= 8) { return 'chip chip--warn'; }
    return 'chip chip--danger';
  }

  tonoDecision(decision: string | null | undefined): string {
    if (!decision) { return 'chip'; }
    const d = decision.toLowerCase();
    if (d.includes('contin')) { return 'chip chip--ok'; }
    if (d.includes('correg') || d.includes('cuna')) { return 'chip chip--warn'; }
    if (d.includes('aband') || d.includes('cerra')) { return 'chip chip--danger'; }
    return 'chip chip--info';
  }

  tonoSeguimiento(estado: string | null | undefined): string {
    switch (estado) {
      case 'pendiente': return 'chip chip--danger';
      case 'en_curso':  return 'chip chip--warn';
      case 'cerrado':   return 'chip chip--ok';
      default:          return 'chip';
    }
  }

  etiquetaSeguimiento(estado: string | null | undefined): string {
    switch (estado) {
      case 'pendiente': return 'Pendiente';
      case 'en_curso':  return 'En curso';
      case 'cerrado':   return 'Cerrado';
      default:          return 'Sin seguimiento';
    }
  }
}
