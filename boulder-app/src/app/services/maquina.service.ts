import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { CrudService } from '../core/crud.service';
import { Maquina } from '../models/operacional';

/** Acceso REST a máquinas de perforación (`/api/operacional/maquinas`). */
@Injectable({ providedIn: 'root' })
export class MaquinaService extends CrudService<Maquina> {
  protected readonly resourceUrl = `${environment.apiBase}/operacional/maquinas`;

  obtenerPorCodigo(codigo: string): Observable<Maquina> {
    return this.http.get<Maquina>(`${this.resourceUrl}/codigo/${codigo}`);
  }
}
