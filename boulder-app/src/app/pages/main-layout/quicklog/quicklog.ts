import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { Visor } from '../../../components/visor/visor';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, debounceTime } from 'rxjs/operators';

import { CatalogoGeologia, LogueoService, Quicklog as TramoQuicklog } from '../../../services/logueo.service';
import { ESCALAS, FotoStrip, StripLog, TramoStrip } from '../../../components/strip-log/strip-log';
import { EventosService } from '../../../services/eventos.service';
import { Foto, FotoService } from '../../../services/foto.service';
import { SondajeService } from '../../../services/sondaje.service';
import { Sondaje } from '../../../models/operacional';
import { ReporteService } from '../../../services/reporte.service';
import { mensajeDeError } from '../../../util/interfaz';
import { guardarSondajeRecordado, leerSondajeRecordado } from '../../../util/sondaje-recordado';

/**
 * Quicklog: el logueo rápido que sube la aplicación móvil desde el almacén de
 * testigos.
 *
 * El backend devuelve los tramos con identificadores crudos —`litoId`,
 * `altTipo1Id`, `minTipo1Id`— porque así están en la tabla. Sin resolverlos
 * contra el diccionario de geología la pantalla sería una pared de UUID, así
 * que se piden las dos cosas a la vez y se cruzan acá.
 */
@Component({
  selector: 'app-quicklog',
  standalone: true,
  imports: [CommonModule, FormsModule, Visor, StripLog],
  templateUrl: './quicklog.html',
  styleUrl: './quicklog.scss',
})
export class Quicklog implements OnInit {

  private readonly logueoService = inject(LogueoService);
  private readonly sondajeService = inject(SondajeService);
  private readonly eventos = inject(EventosService);
  private readonly fotoService = inject(FotoService);
  private readonly reporteService = inject(ReporteService);
  readonly descargando = signal(false);

  /** Fotos del sondaje con tramo, para mostrar la del metro que se describe. */
  readonly fotos = signal<Foto[]>([]);
  readonly miniaturas = signal<Record<string, string>>({});
  readonly ampliada = signal<{ foto: Foto; url: string | null } | null>(null);
  private readonly destroyRef = inject(DestroyRef);
  private readonly router = inject(Router);
  private readonly ruta = inject(ActivatedRoute);

  /** Vista del quicklog: el strip log a escala o la tabla de tramos. */
  readonly vista = signal<'strip' | 'tabla'>('strip');
  readonly escalas = ESCALAS;
  readonly escala = signal(500);
  /** Tramo elegido en el strip log, para la ficha de detalle. */
  readonly seleccionado = signal<TramoQuicklog | null>(null);

  readonly sondajes = signal<Sondaje[]>([]);
  readonly tramos = signal<TramoQuicklog[]>([]);
  readonly catalogo = signal<Map<string, CatalogoGeologia>>(new Map());

  readonly cargando = signal(false);
  readonly error = signal<string | null>(null);

  sondajeId = '';

  /** Metros descritos y tramo más profundo alcanzado. */
  readonly resumen = computed(() => {
    const filas = this.tramos();
    const metros = filas.reduce((t, f) => t + Number(f.longitud ?? 0), 0);
    const hasta = filas.reduce((m, f) => Math.max(m, Number(f.hasta ?? 0)), 0);
    const litologias = new Set(filas.map(f => f.litoId).filter(Boolean)).size;
    // Lo que falta describir entre la boca y el tramo más profundo.
    const huecos = Math.max(0, hasta - metros);
    return { tramos: filas.length, metros, hasta, litologias, huecos };
  });

  ngOnInit(): void {
    forkJoin({
      sondajes: this.sondajeService.listar().pipe(catchError(() => of([] as Sondaje[]))),
      catalogo: this.logueoService.catalogoPorId()
        .pipe(catchError(() => of(new Map<string, CatalogoGeologia>()))),
    }).subscribe(({ sondajes, catalogo }) => {
      this.sondajes.set(sondajes);
      this.catalogo.set(catalogo);
      this.aplicarSondajePedido();
    });

    // ?sondaje=CÓDIGO elige el sondaje (se puede enlazar y volver con Atrás).
    // Sin parámetro se retoma el último que se miró en este navegador.
    this.ruta.queryParamMap
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.aplicarSondajePedido());

    // En vivo: cuando un tablet sube quicklog del sondaje que se está
    // mirando, se recarga sin que nadie toque nada. El debounce agrupa la
    // ráfaga de avisos que genera una sincronización de varios lotes.
    this.eventos.cambiosDe(['foto'], () => this.sondajeId || null)
      .pipe(debounceTime(500), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.cargarFotos());

    this.eventos.cambiosDe(['quicklog'], () => this.sondajeId || null)
      .pipe(debounceTime(400), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => { if (this.sondajeId) { this.cargar(true); } });
  }

  cargarFotos(): void {
    if (!this.sondajeId) {
      this.fotos.set([]);
      return;
    }
    this.fotoService.listar(this.sondajeId).pipe(catchError(() => of([] as Foto[])))
      .subscribe(f => {
        const conTramo = f.filter(x => x.desde !== null && x.hasta !== null);
        this.fotos.set(conTramo);
        for (const foto of conTramo) {
          if (!this.miniaturas()[foto.id]) {
            this.fotoService.imagen(foto.id, 'miniatura')
              .subscribe(url => this.miniaturas.update(m => ({ ...m, [foto.id]: url })));
          }
        }
      });
  }

  /**
   * La foto que muestra cada tramo: la que más metros comparte con él,
   * prefiriendo la de testigo húmedo, que es la que deja ver la roca.
   * Es un computed y no un método del template para no recalcular el cruce
   * en cada ciclo de detección de cambios.
   */
  readonly fotoPorTramo = computed(() => {
    const mapa = new Map<string, Foto>();
    const fotos = this.fotos();
    for (const t of this.tramos()) {
      const d = Number(t.desde), h = Number(t.hasta);
      let mejor: Foto | null = null;
      let mejorPuntaje = 0;
      for (const f of fotos) {
        const solape = Math.min(h, Number(f.hasta)) - Math.max(d, Number(f.desde));
        const puntaje = solape + (f.condicion === 'humedo' ? 0.001 : 0);
        if (solape > 0 && puntaje > mejorPuntaje) {
          mejor = f;
          mejorPuntaje = puntaje;
        }
      }
      if (mejor) {
        mapa.set(t.id, mejor);
      }
    }
    return mapa;
  });

  ampliar(foto: Foto): void {
    this.ampliada.set({ foto, url: null });
    this.fotoService.imagen(foto.id, 'contenido').subscribe(url => {
      if (this.ampliada()?.foto.id === foto.id) {
        this.ampliada.set({ foto, url });
      }
    });
  }

  /** El selector solo cambia la URL; la URL elige el sondaje. */
  elegirSondaje(id: string): void {
    const codigo = this.sondajes().find(s => s.id === id)?.codigo;
    this.router.navigate([], { relativeTo: this.ruta, queryParams: { sondaje: codigo ?? null } });
  }

  private aplicarSondajePedido(): void {
    if (!this.sondajes().length) { return; }
    let codigo = this.ruta.snapshot.queryParamMap.get('sondaje');
    if (!codigo) {
      const recordado = leerSondajeRecordado();
      if (recordado && this.sondajes().some(s => s.codigo === recordado)) {
        this.router.navigate([], { relativeTo: this.ruta, queryParams: { sondaje: recordado }, replaceUrl: true });
        return;
      }
    }
    const id = this.sondajes().find(s => s.codigo === codigo)?.id ?? '';
    if (id === this.sondajeId) { return; }
    this.sondajeId = id;
    guardarSondajeRecordado(codigo ?? '');
    this.cargar();
  }

  cargar(silencioso = false): void {
    if (!this.sondajeId) {
      this.tramos.set([]);
      this.seleccionado.set(null);
      return;
    }
    if (!silencioso) {
      this.seleccionado.set(null);
      this.cargando.set(true);
      this.cargarFotos();
    }
    this.error.set(null);
    this.logueoService.quicklog(this.sondajeId).subscribe({
      next: filas => {
        // Vienen ordenados por la base, pero el orden por profundidad es el
        // que hace legible la columna: un tramo fuera de secuencia se nota.
        this.tramos.set([...filas].sort((a, b) => Number(a.desde) - Number(b.desde)));
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el quicklog de este sondaje.');
        this.cargando.set(false);
      },
    });
  }

  /** Tramos con nombres y colores del diccionario, listos para el strip log. */
  readonly tramosStrip = computed<TramoStrip[]>(() => this.tramos().map(t => ({
    id: t.id,
    desde: Number(t.desde),
    hasta: Number(t.hasta),
    litoCodigo: this.codigo(t.litoId),
    litoNombre: this.nombre(t.litoId),
    litoColor: this.colorLito(t.litoId),
    altCodigo: t.altTipo1Id ? this.codigo(t.altTipo1Id) : null,
    altNombre: t.altTipo1Id ? this.nombre(t.altTipo1Id) : null,
    intensidadCodigo: t.altIntensidad1Id ? this.codigo(t.altIntensidad1Id) : null,
    intensidadNombre: t.altIntensidad1Id ? this.nombre(t.altIntensidad1Id) : null,
    zonaCodigo: t.minZonaId ? this.codigo(t.minZonaId) : null,
    zonaNombre: t.minZonaId ? this.nombre(t.minZonaId) : null,
    pcCu: t.pcCu !== null && t.pcCu !== undefined ? Number(t.pcCu) : null,
  })));

  /** Fotos con tramo para la columna de fotos del strip log. */
  readonly fotosStrip = computed<FotoStrip[]>(() => this.fotos().map(f => ({
    id: f.id,
    desde: Number(f.desde),
    hasta: Number(f.hasta),
    etiqueta: `Cajas ${f.cajaDesde ?? '?'}–${f.cajaHasta ?? '?'} · ${f.desde}–${f.hasta} m`,
  })));

  /** Litologías presentes, con su color, para la leyenda del strip log. */
  readonly leyenda = computed(() => {
    const vistos = new Map<string, { codigo: string; nombre: string; color: string | null }>();
    for (const t of this.tramosStrip()) {
      if (!vistos.has(t.litoCodigo)) {
        vistos.set(t.litoCodigo, { codigo: t.litoCodigo, nombre: t.litoNombre, color: t.litoColor });
      }
    }
    return [...vistos.values()];
  });

  elegirTramo(t: TramoStrip): void {
    this.seleccionado.set(this.tramos().find(x => x.id === t.id) ?? null);
  }

  ampliarPorId(id: string): void {
    const foto = this.fotos().find(f => f.id === id);
    if (foto) { this.ampliar(foto); }
  }

  codigo(id: string | null | undefined): string {
    if (!id) { return ''; }
    return (this.catalogo().get(id)?.codigo ?? '').trim();
  }

  /** Nombre legible de una entrada del diccionario, o guion si no hay valor. */
  nombre(id: string | null | undefined): string {
    if (!id) { return '—'; }
    return this.catalogo().get(id)?.nombre ?? '(desconocido)';
  }

  /**
   * Color del diccionario para la litología, si lo tiene.
   *
   * Los catálogos de geología guardan su color RGB porque es el mismo que se
   * usa en los perfiles y secciones. Reutilizarlo acá hace que la tabla se lea
   * igual que el plano.
   */
  colorLito(id: string | null | undefined): string | null {
    if (!id) { return null; }
    const e = this.catalogo().get(id);
    if (!e || e.colorR === null || e.colorR === undefined) { return null; }
    return `rgb(${e.colorR}, ${e.colorG ?? 0}, ${e.colorB ?? 0})`;
  }

  /** Descarga la hoja de registro del sondaje elegido, con el token del interceptor. */
  descargarHoja(): void {
    const sondaje = this.sondajes().find(s => s.id === this.sondajeId);
    this.descargando.set(true);
    this.reporteService.hojaQuicklog(this.sondajeId).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        const enlace = document.createElement('a');
        enlace.href = url;
        enlace.download = `quicklog_${sondaje?.codigo ?? this.sondajeId}.xlsx`;
        enlace.click();
        URL.revokeObjectURL(url);
        this.descargando.set(false);
      },
      error: (e: unknown) => {
        this.descargando.set(false);
        this.error.set(mensajeDeError(e, 'No se pudo descargar la hoja de registro'));
      },
    });
  }
}
