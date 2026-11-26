import { CommonModule } from '@angular/common';
import { Component, DestroyRef, Injector, OnInit, afterNextRender, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { DesviacionService } from '../../../services/desviacion.service';
import { SurveyMedicionService } from '../../../services/survey-medicion.service';
import { SondajeService } from '../../../services/sondaje.service';
import {
  GraficoTrayectoria,
  PuntoTrayectoria,
} from '../../../components/grafico-trayectoria/grafico-trayectoria';
import { MedicionDesvioForm } from './medicion-desvio/medicion-desvio';
import {
  DesviacionMaquina,
  DesviacionPerforista,
  DesviacionSondaje,
  Sondaje,
  SurveyMedicion,
  TrayectoriaSondaje,
} from '../../../models/operacional';
import { AuthService } from '../../../services/auth.service';
import { irA, mensajeDeError } from '../../../util/interfaz';

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
  imports: [CommonModule, FormsModule, GraficoTrayectoria, MedicionDesvioForm],
  templateUrl: './desvio.html',
  styleUrl: './desvio.scss',
})
export class Desvio implements OnInit {

  private readonly service = inject(DesviacionService);
  readonly puedeRegistrar = inject(AuthService).puedeRegistrar();
  private readonly medicionService = inject(SurveyMedicionService);
  private readonly sondajeService = inject(SondajeService);
  private readonly router = inject(Router);
  private readonly ruta = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly injector = inject(Injector);
  /** Medición pedida en la URL (?medicion=) que espera a que cargue la lista. */
  private medicionPedida: string | null = null;

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
  /** Error del formulario de seguimiento, mostrado junto a él. */
  readonly errorSeguimiento = signal<string | null>(null);
  /** Filtro en vivo de la vista por sondaje. */
  readonly filtro = signal('');

  /** Mediciones filtradas en el navegador: son pocas y ya vienen todas. */
  readonly medicionesVisibles = computed(() => {
    const q = this.filtro().trim().toLowerCase();
    return !q ? this.porSondaje() : this.porSondaje().filter(d =>
      [d.sondajeCodigo, d.sondajeNombre, d.maquinaCodigo, d.perforista]
        .some(v => (v ?? '').toLowerCase().includes(q)));
  });

  /** Formulario de medición nueva abierto, y el aviso de la última registrada. */
  readonly nuevaMedicion = signal(false);
  readonly avisoAlta = signal<string | null>(null);

  /** Sondajes que tienen trayectoria medida, para la vista de curvas. */
  readonly sondajes = signal<Sondaje[]>([]);

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
    // La URL manda: ?medicion=ID abre su detalle y sin él se ve la lista.
    this.ruta.queryParamMap
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(params => {
        this.medicionPedida = params.get('medicion');
        this.aplicarMedicionPedida();
      });
    this.cargar();
  }

  cambiarVista(vista: Vista): void {
    this.vista.set(vista);
    this.trayectoria.set(null);
    this.cargar();
  }

  abrirNuevaMedicion(): void {
    this.avisoAlta.set(null);
    this.nuevaMedicion.set(true);
    // El formulario aparece arriba de la tabla: vista y foco a su primer campo,
    // una vez que Angular lo dibujó.
    // (el propio formulario lleva el foco a su primer campo).
    afterNextRender(() => irA('form-nueva-medicion'), { injector: this.injector });
  }

  cerrarNuevaMedicion(): void {
    this.nuevaMedicion.set(false);
    setTimeout(() => document.getElementById('btn-nueva-medicion')?.focus());
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
        this.service.porSondaje().subscribe({
          next: d => {
            this.porSondaje.set(d);
            this.cargando.set(false);
            // El detalle abierto toma la fila recién cargada (su seguimiento
            // pudo cambiar); si se abrió desde la URL, se busca ahora.
            const abierta = this.seleccionada();
            if (abierta) {
              const actual = d.find(x => x.medicionId === abierta.medicionId);
              if (actual) { this.seleccionada.set(actual); }
            }
            this.aplicarMedicionPedida();
          },
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

  medicionGuardada(m: SurveyMedicion): void {
    this.cerrarNuevaMedicion();
    const desvio = m.desvMetros != null ? ` · separación estimada ${m.desvMetros} m` : '';
    this.avisoAlta.set(`Medición N.º ${m.nroMedicion} a ${m.profundidad} m registrada${desvio}.`);
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

  /** Abre el detalle de la medición: solo cambia la URL, y la URL abre el detalle. */
  seleccionar(fila: DesviacionSondaje): void {
    if (fila.medicionId) {
      this.router.navigate([], { relativeTo: this.ruta, queryParams: { medicion: fila.medicionId } });
    } else {
      this.abrirDetalle(fila);
    }
  }

  volverALista(): void {
    if (this.ruta.snapshot.queryParamMap.has('medicion')) {
      this.router.navigate([], { relativeTo: this.ruta, queryParams: {} });
    } else {
      this.cerrarDetalle();
    }
  }

  /** Sincroniza la vista con ?medicion= una vez que la lista está cargada. */
  private aplicarMedicionPedida(): void {
    const id = this.medicionPedida;
    const abierta = this.seleccionada();
    if (!id) {
      if (abierta) { this.cerrarDetalle(); }
      return;
    }
    if (abierta?.medicionId === id) { return; }
    const fila = this.porSondaje().find(d => d.medicionId === id);
    if (fila) {
      this.abrirDetalle(fila);
    }
  }

  private cerrarDetalle(): void {
    const id = this.seleccionada()?.medicionId;
    this.seleccionada.set(null);
    this.trayectoria.set(null);
    this.aviso.set(null);
    this.errorSeguimiento.set(null);
    // De vuelta en la lista, el foco regresa a la fila de la medición que se veía.
    setTimeout(() => irA('med-' + id, 'med-' + id));
  }

  private abrirDetalle(fila: DesviacionSondaje): void {
    this.vista.set('sondaje');
    this.nuevaMedicion.set(false);
    this.seleccionada.set(fila);
    this.aviso.set(null);
    this.errorSeguimiento.set(null);
    // La pantalla cambia de contenido: arriba de todo y el foco en el título.
    setTimeout(() => {
      window.scrollTo({ top: 0 });
      document.getElementById('detalle-medicion-titulo')?.focus({ preventScroll: true });
    });
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
    this.aviso.set(null);
    this.errorSeguimiento.set(null);
    if (!id) {
      this.errorSeguimiento.set('Esta medición no tiene identificador; no se puede guardar su seguimiento.');
      return;
    }

    this.guardando.set(true);

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
          error: (e: unknown) => {
            this.guardando.set(false);
            this.errorSeguimiento.set(mensajeDeError(e, 'No se pudo guardar el seguimiento'));
          },
        });
      },
      error: (e: unknown) => {
        this.guardando.set(false);
        this.errorSeguimiento.set(mensajeDeError(e, 'No se pudo leer la medición para actualizarla'));
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
