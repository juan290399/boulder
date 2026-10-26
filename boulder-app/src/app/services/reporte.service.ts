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

/** Reporte diario consolidado (`/api/reportes/diario`, pantalla P11). */
@Injectable({ providedIn: 'root' })
export class ReporteService {

  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBase}/reportes/diario`;

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
}
