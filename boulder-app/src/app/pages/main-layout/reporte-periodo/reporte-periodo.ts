import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';

import { ReportePeriodo, ReporteService } from '../../../services/reporte.service';

/** Reporte consolidado de un periodo: acumulado por sondaje y actividad por día. */
@Component({
  selector: 'app-reporte-periodo',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './reporte-periodo.html',
  styleUrl: '../reporte-diario/reporte-diario.scss',
})
export class ReportePeriodoPage implements OnInit {

  private readonly servicio = inject(ReporteService);

  /** Mismo corte de aviso que ReportePeriodo.TOLERANCIA_METROS. */
  readonly toleranciaMetros = 3;
  readonly hoyIso = ReportePeriodoPage.iso(new Date());
  readonly desde = signal(ReportePeriodoPage.iso(ReportePeriodoPage.diasAtras(6)));
  readonly hasta = signal(this.hoyIso);
  readonly reporte = signal<ReportePeriodo | null>(null);
  readonly cargando = signal(false);
  readonly descargando = signal(false);
  readonly error = signal<string | null>(null);

  readonly barras = computed(() => {
    const dias = this.reporte()?.dias ?? [];
    const max = Math.max(1, ...dias.map(d => d.perforado));
    return dias.map(d => ({ ...d, alto: (d.perforado / max) * 100 }));
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.servicio.periodo(this.desde(), this.hasta()).subscribe({
      next: r => {
        this.reporte.set(r);
        this.cargando.set(false);
      },
      error: (e: HttpErrorResponse) => {
        this.reporte.set(null);
        this.cargando.set(false);
        this.error.set(e.error?.mensaje ?? e.error?.message ?? 'No se pudo generar el reporte.');
      },
    });
  }

  cambiarDesde(f: string): void {
    this.desde.set(f);
    this.cargar();
  }

  cambiarHasta(f: string): void {
    this.hasta.set(f);
    this.cargar();
  }

  /** Atajos: últimos 7 días y mes en curso. */
  semana(): void {
    this.desde.set(ReportePeriodoPage.iso(ReportePeriodoPage.diasAtras(6)));
    this.hasta.set(this.hoyIso);
    this.cargar();
  }

  mes(): void {
    const hoy = new Date();
    this.desde.set(ReportePeriodoPage.iso(new Date(hoy.getFullYear(), hoy.getMonth(), 1)));
    this.hasta.set(this.hoyIso);
    this.cargar();
  }

  exportarExcel(): void {
    this.descargando.set(true);
    this.servicio.excelPeriodo(this.desde(), this.hasta()).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        const enlace = document.createElement('a');
        enlace.href = url;
        enlace.download = `reporte_${this.desde()}_${this.hasta()}.xlsx`;
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

  private static diasAtras(n: number): Date {
    const d = new Date();
    d.setDate(d.getDate() - n);
    return d;
  }

  /** Fecha local, no UTC. */
  private static iso(d: Date): string {
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }
}
