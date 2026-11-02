import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';

export interface FilaSondajeReporte {
  codigo: string;
  perforado: number;
  logueado: number;
  cajas: number;
  muestras: number;
}

export interface PuntoSerie {
  fecha: string;
  metros: number;
}

/** Espejo del record `ReporteDiario` del backend. */
export interface ReporteDiario {
  fecha: string;
  sondajes: FilaSondajeReporte[];
  ultimosDias: PuntoSerie[];
  totalPerforado: number;
  totalLogueado: number;
  totalCajas: number;
  totalMuestras: number;
}

export interface FilaDiaReporte {
  fecha: string;
  perforado: number;
  logueado: number;
  cajas: number;
  muestras: number;
}

export interface FilaDesvioReporte {
  sondaje: string;
  profundidad: number | null;
  fecha: string;
  desvTotal: number | null;
  desvMetros: number | null;
  decision: string | null;
}

/** Espejo del record `ReportePeriodo` del backend. */
export interface ReportePeriodo {
  desde: string;
  hasta: string;
  sondajes: FilaSondajeReporte[];
  dias: FilaDiaReporte[];
  desvios: FilaDesvioReporte[];
  totalPerforado: number;
  totalLogueado: number;
  totalCajas: number;
  totalMuestras: number;
}

/** Reportes consolidados: diario (`/api/reportes/diario`, P11) y de periodo (`/api/reportes/periodo`). */
@Injectable({ providedIn: 'root' })
export class ReporteService {

  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBase}/reportes/diario`;
  private readonly periodoUrl = `${environment.apiBase}/reportes/periodo`;

  diario(fecha: string): Observable<ReporteDiario> {
    return this.http.get<ReporteDiario>(this.baseUrl, { params: new HttpParams().set('fecha', fecha) });
  }

  /**
   * El Excel llega como binario. Se pide con HttpClient, y no con un enlace, para
   * que el interceptor agregue el token: un `<a href>` iría sin cabecera.
   */
  excel(fecha: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/excel`, {
      params: new HttpParams().set('fecha', fecha),
      responseType: 'blob',
    });
  }

  periodo(desde: string, hasta: string): Observable<ReportePeriodo> {
    return this.http.get<ReportePeriodo>(this.periodoUrl, { params: this.rango(desde, hasta) });
  }

  excelPeriodo(desde: string, hasta: string): Observable<Blob> {
    return this.http.get(`${this.periodoUrl}/excel`, { params: this.rango(desde, hasta), responseType: 'blob' });
  }

  /** Hoja de registro de quicklog de un sondaje (`/api/reportes/quicklog/excel`). */
  hojaQuicklog(sondajeId: string): Observable<Blob> {
    return this.http.get(`${environment.apiBase}/reportes/quicklog/excel`, {
      params: new HttpParams().set('sondajeId', sondajeId),
      responseType: 'blob',
    });
  }

  private rango(desde: string, hasta: string): HttpParams {
    return new HttpParams().set('desde', desde).set('hasta', hasta);
  }
}
