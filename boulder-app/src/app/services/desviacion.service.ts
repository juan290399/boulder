import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import {
  DesviacionMaquina,
  DesviacionPerforista,
  DesviacionSondaje,
  TrayectoriaSondaje,
} from '../models/operacional';

/**
 * Consultas de solo lectura para la analítica de desviaciones de sondaje
 * (`/api/operacional/desviaciones`), respaldadas por las vistas
 * `operacional.vw_desviacion_*` del backend.
 */
@Injectable({ providedIn: 'root' })
export class DesviacionService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBase}/operacional/desviaciones`;

  porPerforista(): Observable<DesviacionPerforista[]> {
    return this.http.get<DesviacionPerforista[]>(`${this.baseUrl}/perforista`);
  }

  porMaquina(): Observable<DesviacionMaquina[]> {
    return this.http.get<DesviacionMaquina[]>(`${this.baseUrl}/maquina`);
  }

  porSondaje(codigo?: string): Observable<DesviacionSondaje[]> {
    let params = new HttpParams();
    if (codigo) {
      params = params.set('codigo', codigo);
    }
    return this.http.get<DesviacionSondaje[]>(`${this.baseUrl}/sondaje`, { params });
  }

  /**
   * Trayectoria planificada contra medida, para graficar el desvío.
   *
   * Reemplaza a la planilla con las dos gráficas comparativas que hoy se
   * comparte como imagen por WhatsApp.
   */
  trayectoria(sondajeId: string): Observable<TrayectoriaSondaje> {
    return this.http.get<TrayectoriaSondaje>(`${this.baseUrl}/trayectoria/${sondajeId}`);
  }
}
