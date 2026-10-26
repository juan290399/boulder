import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { CrudService } from '../core/crud.service';
import { AvancePerforacion } from '../models/operacional';

/** Acceso REST a los partes diarios de avance de perforación (`/api/operacional/avances-perforacion`). */
@Injectable({ providedIn: 'root' })
export class AvancePerforacionService extends CrudService<AvancePerforacion> {
  protected readonly resourceUrl = `${environment.apiBase}/operacional/avances-perforacion`;

  /** Lista opcionalmente filtrada por sondaje o por fecha (YYYY-MM-DD). */
  buscar(filtro?: { sondajeId?: string; fecha?: string }): Observable<AvancePerforacion[]> {
    let params = new HttpParams();
    if (filtro?.sondajeId) {
      params = params.set('sondajeId', filtro.sondajeId);
    }
    if (filtro?.fecha) {
      params = params.set('fecha', filtro.fecha);
    }
    return this.http.get<AvancePerforacion[]>(this.resourceUrl, { params });
  }

  /**
   * Borrador del turno siguiente: arranca donde terminó el último parte, con su
   * máquina, plataforma y dotación. No guarda nada.
   */
  sugerencia(sondajeId: string): Observable<AvancePerforacion> {
    return this.http.get<AvancePerforacion>(`${this.resourceUrl}/sugerencia`, {
      params: new HttpParams().set('sondajeId', sondajeId),
    });
  }
}
