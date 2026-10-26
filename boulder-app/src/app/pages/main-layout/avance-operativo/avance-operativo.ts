import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { catchError, debounceTime } from 'rxjs/operators';

import { EventosService } from '../../../services/eventos.service';

import { FrenteAvanceService } from '../../../services/frente-avance.service';
import {
  AvanceDensidadService,
  AvanceFotografiaService,
  AvanceLogueoService,
  AvanceMuestreoService,
  AvanceOperativoApi,
  CorteDensidadService,
  CorteMuestraService,
  RecepcionCajaService,
} from '../../../services/avance-operativo.service';
import { AvanceOperativo, FrenteAvance } from '../../../models/operacional';

/** Descriptor de cada uno de los siete partes, para la pestaña de detalle. */
interface TipoParte {
  clave: string;
  etiqueta: string;
  icono: string;
  api: AvanceOperativoApi<AvanceOperativo>;
  /** Columnas propias del parte, además de la cabecera común. */
  columnas: { campo: string; titulo: string; numerica?: boolean }[];
}

/**
 * Avances operativos: la pantalla principal del geólogo.
 *
 * El dato que organiza la pantalla no es cuánto se hizo sino hasta dónde llegó
 * cada etapa. Un sondaje perforado a 300 m con el logueo en 240 y el muestreo
 * en 180 tiene dos frentes atrasados, y esa distancia es lo que decide de qué
 * hay que ocuparse hoy. Por eso lo primero que se ve es el frente por sondaje,
 * y recién al elegir uno aparecen los partes que lo componen.
 */
@Component({
  selector: 'app-avance-operativo',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './avance-operativo.html',
  styleUrl: './avance-operativo.scss',
})
export class AvanceOperativoPage implements OnInit {

  private readonly frenteService = inject(FrenteAvanceService);
  private readonly eventos = inject(EventosService);
  private readonly destroyRef = inject(DestroyRef);

  readonly cargando = signal(false);
  readonly error = signal<string | null>(null);
  readonly soloActivos = signal(true);
  readonly frente = signal<FrenteAvance[]>([]);

  readonly seleccionado = signal<FrenteAvance | null>(null);
  readonly tipoActivo = signal<string>('logueo');
  readonly partes = signal<AvanceOperativo[]>([]);
  readonly cargandoPartes = signal(false);

  /** Los siete partes, en el orden real del flujo de testigo. */
  readonly tipos: TipoParte[] = [
    {
      clave: 'recepcion', etiqueta: 'Recepción de cajas', icono: 'bi-box-seam',
      api: inject(RecepcionCajaService),
      columnas: [
        { campo: 'responsable', titulo: 'Responsable' },
        { campo: 'boxDesde', titulo: 'Caja desde', numerica: true },
        { campo: 'boxHasta', titulo: 'Caja hasta', numerica: true },
        { campo: 'totalCajas', titulo: 'Cajas', numerica: true },
        { campo: 'marcadoHasta', titulo: 'Marcado hasta', numerica: true },
        { campo: 'totalMetraje', titulo: 'Metraje', numerica: true },
      ],
    },
    {
      clave: 'logueo', etiqueta: 'Logueo', icono: 'bi-journal-text',
      api: inject(AvanceLogueoService),
      columnas: [
        { campo: 'tipoLogueo', titulo: 'Tipo' },
        { campo: 'geologo', titulo: 'Geólogo' },
        { campo: 'desde', titulo: 'Desde', numerica: true },
        { campo: 'hasta', titulo: 'Hasta', numerica: true },
        { campo: 'avance', titulo: 'Avance', numerica: true },
        { campo: 'pctCompletado', titulo: '% compl.', numerica: true },
      ],
    },
    {
      clave: 'fotografia', etiqueta: 'Fotografía', icono: 'bi-camera',
      api: inject(AvanceFotografiaService),
      columnas: [
        { campo: 'responsable', titulo: 'Responsable' },
        { campo: 'secoHumedo', titulo: 'Pasada' },
        { campo: 'boxDesde', titulo: 'Caja desde', numerica: true },
        { campo: 'boxHasta', titulo: 'Caja hasta', numerica: true },
        { campo: 'totalCajas', titulo: 'Cajas', numerica: true },
        { campo: 'nroFotos', titulo: 'Fotos', numerica: true },
      ],
    },
    {
      clave: 'corte', etiqueta: 'Corte geoquímico', icono: 'bi-scissors',
      api: inject(CorteMuestraService),
      columnas: [
        { campo: 'responsable', titulo: 'Responsable' },
        { campo: 'desde', titulo: 'Desde', numerica: true },
        { campo: 'hasta', titulo: 'Hasta', numerica: true },
        { campo: 'tipoLinea', titulo: 'Línea' },
        { campo: 'metrajeEstimado', titulo: 'Metraje', numerica: true },
        { campo: 'nroMuestras', titulo: 'Muestras', numerica: true },
      ],
    },
    {
      clave: 'muestreo', etiqueta: 'Muestreo', icono: 'bi-bag',
      api: inject(AvanceMuestreoService),
      columnas: [
        { campo: 'responsable', titulo: 'Responsable' },
        { campo: 'estadoMuestreo', titulo: 'Estado' },
        { campo: 'avance', titulo: 'Avance', numerica: true },
        { campo: 'nroMuestras', titulo: 'Muestras', numerica: true },
        { campo: 'nroMuestrasEmbolsadas', titulo: 'Embolsadas', numerica: true },
        { campo: 'nroCostales', titulo: 'Costales', numerica: true },
      ],
    },
    {
      clave: 'corte-densidad', etiqueta: 'Corte de densidad', icono: 'bi-rulers',
      api: inject(CorteDensidadService),
      columnas: [
        { campo: 'responsable', titulo: 'Responsable' },
        { campo: 'desde', titulo: 'Desde', numerica: true },
        { campo: 'hasta', titulo: 'Hasta', numerica: true },
        { campo: 'totalMuestras', titulo: 'Muestras', numerica: true },
        { campo: 'totalMetraje', titulo: 'Metraje', numerica: true },
        { campo: 'tipoLinea', titulo: 'Línea' },
      ],
    },
    {
      clave: 'densidad', etiqueta: 'Estufa', icono: 'bi-thermometer-half',
      api: inject(AvanceDensidadService),
      columnas: [
        { campo: 'responsable', titulo: 'Responsable' },
        { campo: 'etapa', titulo: 'Etapa' },
        { campo: 'nroMuestras', titulo: 'Muestras', numerica: true },
        { campo: 'temperaturaC', titulo: 'Temp. °C', numerica: true },
        { campo: 'tipoLinea', titulo: 'Línea' },
      ],
    },
  ];

  /** Indicadores de cabecera, derivados del frente ya cargado. */
  readonly resumen = computed(() => {
    const filas = this.frente();
    const activos = filas.filter(f => f.activo).length;

    // Solo cuentan los sondajes que tienen algo perforado: los que aun no
    // arrancaron inflarian el rezago con metros que nadie debia loguear.
    const conAvance = filas.filter(f => (f.etapas[0]?.profundidad ?? 0) > 0);

    const rezagoLogueo = conAvance
      .reduce((t, f) => t + (f.etapas.find(e => e.clave === 'logueo')?.rezago ?? 0), 0);

    const muestras = filas.reduce((t, f) => t + (f.muestrasEmbolsadas ?? 0), 0);
    const detenidos = filas.filter(f => f.activo && (f.diasSinMovimiento ?? 0) > 7).length;

    return { activos, rezagoLogueo, muestras, detenidos };
  });

  get tipoActual(): TipoParte {
    return this.tipos.find(t => t.clave === this.tipoActivo()) ?? this.tipos[1];
  }

  ngOnInit(): void {
    this.cargarFrente();

    // En vivo: cualquier parte o tramo nuevo mueve el frente. Se espera un
    // segundo para juntar la ráfaga de una sincronización o una importación
    // en una sola recarga.
    this.eventos.cambiosDe(['avance', 'quicklog', 'litologia', 'alteracion', 'mineralizacion', 'estructural',
      'perforacion', 'almacen', 'logueo', 'desvio'])
      .pipe(debounceTime(1000), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.cargarFrente(true));
  }

  /** @param silencioso recarga en vivo, sin cartel de carga que haga parpadear la tabla. */
  cargarFrente(silencioso = false): void {
    if (!silencioso) {
      this.cargando.set(true);
    }
    this.error.set(null);
    this.frenteService.consultar(this.soloActivos()).subscribe({
      next: filas => {
        this.frente.set(filas);
        this.cargando.set(false);
        // Si el sondaje abierto dejo de estar en la lista, se cierra el detalle.
        const abierto = this.seleccionado();
        if (abierto && !filas.some(f => f.sondajeId === abierto.sondajeId)) {
          this.seleccionado.set(null);
        }
      },
      error: () => {
        this.error.set('No se pudo cargar el frente de avance.');
        this.cargando.set(false);
      },
    });
  }

  alternarActivos(): void {
    this.soloActivos.update(v => !v);
    this.cargarFrente();
  }

  seleccionar(fila: FrenteAvance): void {
    if (this.seleccionado()?.sondajeId === fila.sondajeId) {
      this.seleccionado.set(null);
      return;
    }
    this.seleccionado.set(fila);
    this.cargarPartes();
  }

  elegirTipo(clave: string): void {
    this.tipoActivo.set(clave);
    this.cargarPartes();
  }

  private cargarPartes(): void {
    const fila = this.seleccionado();
    if (!fila) { return; }

    this.cargandoPartes.set(true);
    this.partes.set([]);

    // Un fallo aca no debe tumbar la pantalla entera: el frente ya se mostro y
    // el detalle es secundario.
    this.tipoActual.api.listar({ sondajeId: fila.sondajeId })
      .pipe(catchError(() => of([] as AvanceOperativo[])))
      .subscribe(lista => {
        this.partes.set(lista);
        this.cargandoPartes.set(false);
      });
  }

  /** Lee una columna del parte sin que la plantilla tenga que tipar cada uno. */
  valor(parte: AvanceOperativo, campo: string): unknown {
    return (parte as unknown as Record<string, unknown>)[campo];
  }

  /**
   * Color de la barra segun que tan atras va la etapa.
   *
   * Los cortes son de criterio operativo, no estadistico: por encima del 90 %
   * la etapa sigue el ritmo de la perforacion, por debajo del 60 % acumulo un
   * atraso que ya no se recupera en un turno.
   */
  tonoEtapa(pct: number | null): string {
    if (pct === null || pct === undefined) { return 'track__fill--danger'; }
    if (pct >= 90) { return 'track__fill--ok'; }
    if (pct >= 60) { return ''; }
    return 'track__fill--warn';
  }

  tonoDias(dias: number | null | undefined): string {
    if (dias === null || dias === undefined) { return 'chip'; }
    if (dias <= 2) { return 'chip chip--ok'; }
    if (dias <= 7) { return 'chip chip--warn'; }
    return 'chip chip--danger';
  }

  /**
   * Abreviatura de dos letras para el pie de cada barra.
   *
   * La inicial sola no alcanza desde que el logueo son tres etapas: geológico,
   * geomecánico y estructural empiezan las tres con L.
   */
  abrevia(clave: string): string {
    const abreviaturas: Record<string, string> = {
      perforacion: 'PE',
      recepcion:   'RC',
      logueo:      'LG',
      geomecanico: 'LM',
      estructural: 'LE',
      fotografia:  'FO',
      corte:       'CT',
      muestreo:    'MU',
      densidad:    'DN',
    };
    return abreviaturas[clave] ?? clave.slice(0, 2).toUpperCase();
  }

  nombreEtapa(fila: FrenteAvance, clave: string | null | undefined): string {
    if (!clave) { return 'Al dia'; }
    return fila.etapas.find(e => e.clave === clave)?.nombre ?? clave;
  }
}
