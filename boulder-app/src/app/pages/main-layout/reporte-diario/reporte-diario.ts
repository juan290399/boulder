import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { ReporteDiario, ReporteService } from '../../../services/reporte.service';

/**
 * Reporte diario consolidado (P11, RF15): reemplaza el consolidado que se arma
 * a mano copiando de cinco reportes.
 *
 * Se exporta a Excel (lo genera el backend con Apache POI) y a PDF con la
 * impresión del navegador: la hoja de estilos de impresión deja solo el reporte.
 */
@Component({
  selector: 'app-reporte-diario',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reporte-diario.html',
  styleUrl: './reporte-diario.scss',
})
export class ReporteDiarioPage implements OnInit {

  private readonly servicio = inject(ReporteService);

  /** Hoy, en hora local; también es el máximo del selector: no hay reportes del futuro. */
  readonly hoyIso = this.hoy();
  readonly fecha = signal(this.hoyIso);
  readonly reporte = signal<ReporteDiario | null>(null);
  readonly cargando = signal(false);
  readonly descargando = signal(false);
  readonly error = signal<string | null>(null);

  /** Altura relativa de cada barra de la serie; la más alta llena el gráfico. */
  readonly barras = computed(() => {
    const serie = this.reporte()?.ultimosDias ?? [];
    const max = Math.max(1, ...serie.map(p => p.metros));
    return serie.map(p => ({ ...p, alto: (p.metros / max) * 100 }));
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.servicio.diario(this.fecha()).subscribe({
      next: r => {
        this.reporte.set(r);
        this.cargando.set(false);
      },
      error: (e: HttpErrorResponse) => {
        this.reporte.set(null);
        this.cargando.set(false);
        this.error.set(e.error?.message ?? 'No se pudo generar el reporte.');
      },
    });
  }

  cambiarFecha(f: string): void {
    this.fecha.set(f);
    this.cargar();
  }

  exportarExcel(): void {
    this.descargando.set(true);
    this.servicio.excel(this.fecha()).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        const enlace = document.createElement('a');
        enlace.href = url;
        enlace.download = `reporte_diario_${this.fecha()}.xlsx`;
        enlace.click();
        URL.revokeObjectURL(url);
        this.descargando.set(false);
      },
      error: () => {
        this.descargando.set(false);
        this.error.set('No se pudo descargar el Excel.');
      },
    });
  }

  imprimir(): void {
    window.print();
  }

  diaCorto(iso: string): string {
    const [, m, d] = iso.split('-');
    return `${d}/${m}`;
  }

  private hoy(): string {
    // Fecha local, no UTC: a las 8 p. m. en Lima, toISOString ya da el día siguiente.
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }
}
