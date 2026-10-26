import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { CrudService } from '../core/crud.service';
import { SurveyMedicion } from '../models/operacional';

/** Acceso REST a los eventos de medición de desviación (`/api/operacional/survey-mediciones`). */
@Injectable({ providedIn: 'root' })
export class SurveyMedicionService extends CrudService<SurveyMedicion> {
  protected readonly resourceUrl = `${environment.apiBase}/operacional/survey-mediciones`;

  /** Lista opcionalmente filtrada por sondaje. */
  buscar(sondajeId?: string): Observable<SurveyMedicion[]> {
    let params = new HttpParams();
    if (sondajeId) {
      params = params.set('sondajeId', sondajeId);
    }
    return this.http.get<SurveyMedicion[]>(this.resourceUrl, { params });
  }
}
