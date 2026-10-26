import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError, debounceTime } from 'rxjs/operators';

import { CatalogoGeologia, DispositivoSync, LogueoService, Quicklog as TramoQuicklog } from '../../../services/logueo.service';
import { EventosService } from '../../../services/eventos.service';
import { Foto, FotoService } from '../../../services/foto.service';
import { SondajeService } from '../../../services/sondaje.service';
import { Sondaje } from '../../../models/operacional';

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
  imports: [CommonModule, FormsModule],
  templateUrl: './quicklog.html',
  styleUrl: './quicklog.scss',
})
export class Quicklog implements OnInit {

  private readonly logueoService = inject(LogueoService);
  private readonly sondajeService = inject(SondajeService);
  private readonly eventos = inject(EventosService);
  private readonly fotoService = inject(FotoService);

  /** Fotos del sondaje con tramo, para mostrar la del metro que se describe. */
  readonly fotos = signal<Foto[]>([]);
  readonly miniaturas = signal<Record<string, string>>({});
  readonly ampliada = signal<{ foto: Foto; url: string | null } | null>(null);
  private readonly destroyRef = inject(DestroyRef);

  /** Estado del canal en vivo, para el indicador de la cabecera. */
  readonly canal = this.eventos.estado;

  /** Tablets de GeoLog y su última sincronización. */
  readonly dispositivos = signal<DispositivoSync[]>([]);

  /** Cuándo llegó el último cambio desde un tablet, para avisarlo en pantalla. */
  readonly ultimoAviso = signal<{ momento: Date; filas: number; origen: string } | null>(null);

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
    return { tramos: filas.length, metros, hasta, litologias };
  });

  ngOnInit(): void {
    forkJoin({
      sondajes: this.sondajeService.listar().pipe(catchError(() => of([] as Sondaje[]))),
      catalogo: this.logueoService.catalogoPorId()
        .pipe(catchError(() => of(new Map<string, CatalogoGeologia>()))),
    }).subscribe(({ sondajes, catalogo }) => {
      this.sondajes.set(sondajes);
      this.catalogo.set(catalogo);
    });
    this.cargarDispositivos();

    // En vivo: cuando un tablet sube quicklog del sondaje que se está
    // mirando, se recarga sin que nadie toque nada. El debounce agrupa la
    // ráfaga de avisos que genera una sincronización de varios lotes.
    this.eventos.cambiosDe(['foto'], () => this.sondajeId || null)
      .pipe(debounceTime(500), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.cargarFotos());

    this.eventos.cambiosDe(['quicklog'], () => this.sondajeId || null)
      .pipe(debounceTime(400), takeUntilDestroyed(this.destroyRef))
      .subscribe(c => {
        this.cargarDispositivos();
        if (this.sondajeId) {
          this.cargar(true);
          if (c.origen !== 'reconexion') {
            this.ultimoAviso.set({ momento: new Date(), filas: c.filas, origen: c.origen });
          }
        }
      });
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

  cargarDispositivos(): void {
    this.logueoService.dispositivos().pipe(catchError(() => of([] as DispositivoSync[])))
      .subscribe(d => this.dispositivos.set(d));
  }

  /** «hace 5 min», «hace 3 h»: lo que importa es la distancia, no la hora. */
  hace(iso: string | null): string {
    if (!iso) { return 'nunca'; }
    const min = Math.round((Date.now() - new Date(iso).getTime()) / 60000);
    if (min < 1) { return 'hace un momento'; }
    if (min < 60) { return `hace ${min} min`; }
    const h = Math.round(min / 60);
    return h < 48 ? `hace ${h} h` : `hace ${Math.round(h / 24)} días`;
  }

  /** Un tablet que no sincroniza hace más de 6 h probablemente tiene trabajo sin subir. */
  atrasado(iso: string | null): boolean {
    return !iso || Date.now() - new Date(iso).getTime() > 6 * 3600 * 1000;
  }

  /**
   * @param silencioso recarga en vivo: sin el cartel de «Cargando…», que
   *   haría parpadear la tabla cada vez que llega un tramo.
   */
  cargar(silencioso = false): void {
    if (!this.sondajeId) {
      this.tramos.set([]);
      this.ultimoAviso.set(null);
      return;
    }
    if (!silencioso) {
      this.cargando.set(true);
      this.ultimoAviso.set(null);
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
}
