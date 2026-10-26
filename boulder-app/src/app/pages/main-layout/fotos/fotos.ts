import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom, of } from 'rxjs';
import { catchError, debounceTime } from 'rxjs/operators';

import { Foto, FotoService, MetadatosFoto, leerNombreFoto } from '../../../services/foto.service';
import { SondajeService } from '../../../services/sondaje.service';
import { EventosService } from '../../../services/eventos.service';
import { Sondaje } from '../../../models/operacional';

/** Un archivo elegido para subir, con sus datos editables. */
interface PorSubir {
  archivo: File;
  id: string;
  previa: string;
  cajaDesde: number | null;
  cajaHasta: number | null;
  desde: number | null;
  hasta: number | null;
  condicion: 'seco' | 'humedo' | null;
  estado: 'lista' | 'subiendo' | 'subida' | 'error';
  error?: string;
}

/**
 * Fotos de cajas de testigo, por sondaje.
 *
 * La galería se ordena por profundidad, que es como se lee el testigo, y cada
 * foto dice qué cajas y qué tramo muestra. Se puede subir desde aquí —la
 * cámara del almacén descarga a la PC— y lo que suba un tablet aparece solo,
 * por el canal en vivo.
 *
 * La subida es una foto a la vez y con el id puesto por el navegador: si la
 * conexión se corta a mitad de un lote, se vuelve a pulsar «Subir» y las que ya
 * llegaron no se duplican.
 */
@Component({
  selector: 'app-fotos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './fotos.html',
  styleUrl: './fotos.scss',
})
export class Fotos implements OnInit {

  private readonly fotoService = inject(FotoService);
  private readonly sondajeService = inject(SondajeService);
  private readonly eventos = inject(EventosService);
  private readonly destroyRef = inject(DestroyRef);

  readonly canal = this.eventos.estado;
  readonly sondajes = signal<Sondaje[]>([]);
  readonly fotos = signal<Foto[]>([]);
  readonly miniaturas = signal<Record<string, string>>({});
  readonly cargando = signal(false);
  readonly error = signal<string | null>(null);

  readonly porSubir = signal<PorSubir[]>([]);
  readonly subiendo = signal(false);
  condicionLote: 'seco' | 'humedo' | '' = '';

  readonly ampliada = signal<{ foto: Foto; url: string | null } | null>(null);

  sondajeId = '';

  readonly resumen = computed(() => {
    const f = this.fotos();
    const hasta = f.reduce((m, x) => Math.max(m, Number(x.hasta ?? 0)), 0);
    return {
      total: f.length,
      secas: f.filter(x => x.condicion === 'seco').length,
      humedas: f.filter(x => x.condicion === 'humedo').length,
      hasta,
      sinTramo: f.filter(x => x.desde === null || x.hasta === null).length,
    };
  });

  readonly listasParaSubir = computed(() => this.porSubir().filter(p => p.estado === 'lista' || p.estado === 'error').length);

  ngOnInit(): void {
    this.sondajeService.listar().pipe(catchError(() => of([] as Sondaje[])))
      .subscribe(s => this.sondajes.set(s));

    this.eventos.cambiosDe(['foto'], () => this.sondajeId || null)
      .pipe(debounceTime(500), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.cargar(true));
  }

  cargar(silencioso = false): void {
    if (!this.sondajeId) {
      this.fotos.set([]);
      return;
    }
    if (!silencioso) {
      this.cargando.set(true);
    }
    this.error.set(null);
    this.fotoService.listar(this.sondajeId).subscribe({
      next: f => {
        this.fotos.set(f);
        this.cargando.set(false);
        for (const foto of f) {
          if (!this.miniaturas()[foto.id]) {
            this.fotoService.imagen(foto.id, 'miniatura').subscribe({
              next: url => this.miniaturas.update(m => ({ ...m, [foto.id]: url })),
              error: () => { /* la tarjeta queda con su marcador */ },
            });
          }
        }
      },
      error: () => {
        this.error.set('No se pudieron cargar las fotos de este sondaje.');
        this.cargando.set(false);
      },
    });
  }

  elegirArchivos(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    const archivos = Array.from(input.files ?? []);
    input.value = '';
    const nuevos: PorSubir[] = archivos.map(archivo => {
      const leido = leerNombreFoto(archivo.name);
      return {
        archivo,
        id: crypto.randomUUID(),
        previa: URL.createObjectURL(archivo),
        cajaDesde: leido.cajaDesde ?? null,
        cajaHasta: leido.cajaHasta ?? null,
        desde: leido.desde ?? null,
        hasta: leido.hasta ?? null,
        condicion: leido.condicion ?? (this.condicionLote || null),
        estado: 'lista',
      };
    });
    this.porSubir.update(l => [...l, ...nuevos]);
  }

  quitar(p: PorSubir): void {
    URL.revokeObjectURL(p.previa);
    this.porSubir.update(l => l.filter(x => x !== p));
  }

  aplicarCondicionALote(): void {
    if (!this.condicionLote) { return; }
    this.porSubir.update(l => l.map(p => p.estado === 'subida' ? p : { ...p, condicion: this.condicionLote || null }));
  }

  /** Sube de a una: con la conexión de un campamento, varias a la vez se estorban. */
  async subirTodo(): Promise<void> {
    if (!this.sondajeId || this.subiendo()) { return; }
    this.subiendo.set(true);
    for (const p of this.porSubir()) {
      if (p.estado === 'subida') { continue; }
      this.marcar(p, { estado: 'subiendo', error: undefined });
      const m: MetadatosFoto = {
        sondajeId: this.sondajeId, cajaDesde: p.cajaDesde, cajaHasta: p.cajaHasta,
        desde: p.desde, hasta: p.hasta, condicion: p.condicion,
      };
      try {
        await firstValueFrom(this.fotoService.subir(p.archivo, m, p.id));
        this.marcar(p, { estado: 'subida' });
      } catch (e) {
        const msg = (e as HttpErrorResponse)?.error?.mensaje ?? 'No se pudo subir.';
        this.marcar(p, { estado: 'error', error: msg });
      }
    }
    this.subiendo.set(false);
    this.cargar(true);
  }

  limpiarSubidas(): void {
    for (const p of this.porSubir().filter(x => x.estado === 'subida')) {
      URL.revokeObjectURL(p.previa);
    }
    this.porSubir.update(l => l.filter(x => x.estado !== 'subida'));
  }

  ampliar(foto: Foto): void {
    this.ampliada.set({ foto, url: null });
    this.fotoService.imagen(foto.id, 'contenido').subscribe(url => {
      if (this.ampliada()?.foto.id === foto.id) {
        this.ampliada.set({ foto, url });
      }
    });
  }

  eliminar(foto: Foto): void {
    if (!confirm(`¿Quitar la foto de las cajas ${foto.cajaDesde ?? '?'}–${foto.cajaHasta ?? '?'}? El archivo se conserva en el servidor.`)) {
      return;
    }
    this.fotoService.eliminar(foto.id).subscribe({
      next: () => {
        this.ampliada.set(null);
        this.cargar(true);
      },
      error: () => this.error.set('No se pudo quitar la foto.'),
    });
  }

  tamanio(bytes: number): string {
    return bytes > 1048576 ? `${(bytes / 1048576).toFixed(1)} MB` : `${Math.round(bytes / 1024)} KB`;
  }

  private marcar(p: PorSubir, cambios: Partial<PorSubir>): void {
    this.porSubir.update(l => l.map(x => x.id === p.id ? { ...x, ...cambios } : x));
  }
}
