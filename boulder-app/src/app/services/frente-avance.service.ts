import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { FrenteAvance } from '../models/operacional';

/**
 * Consulta consolidada del frente de avance
 * (`/api/operacional/frente-avance`).
 *
 * Devuelve, por sondaje, hasta qué profundidad llegó cada una de las siete
 * etapas del flujo de testigo. Es la fuente de la pantalla de avances.
 */
@Injectable({ providedIn: 'root' })
export class FrenteAvanceService {

  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBase}/operacional/frente-avance`;

  consultar(soloActivos = true): Observable<FrenteAvance[]> {
    const params = new HttpParams().set('soloActivos', soloActivos);
    return this.http.get<FrenteAvance[]>(this.baseUrl, { params });
  }
}
