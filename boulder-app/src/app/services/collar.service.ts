import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { CrudService } from '../core/crud.service';
import { Collar } from '../models/operacional';

/** Acceso REST a collares de sondaje (`/api/operacional/collares`). */
@Injectable({ providedIn: 'root' })
export class CollarService extends CrudService<Collar> {
  protected readonly resourceUrl = `${environment.apiBase}/operacional/collares`;

  obtenerPorCodigo(codigo: string): Observable<Collar> {
    return this.http.get<Collar>(`${this.resourceUrl}/codigo/${codigo}`);
  }
}
