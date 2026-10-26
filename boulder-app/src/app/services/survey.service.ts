import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { CrudService } from '../core/crud.service';
import { Survey } from '../models/operacional';

/** Acceso REST a la trayectoria (survey) de los sondajes (`/api/operacional/surveys`). */
@Injectable({ providedIn: 'root' })
export class SurveyService extends CrudService<Survey> {
  protected readonly resourceUrl = `${environment.apiBase}/operacional/surveys`;

  listarPorSondaje(sondajeId: string): Observable<Survey[]> {
    return this.http.get<Survey[]>(`${this.resourceUrl}/sondaje/${sondajeId}`);
  }
}
