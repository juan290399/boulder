import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { CrudService } from '../core/crud.service';
import { Proyecto } from '../models/operacional';

/** Acceso REST a proyectos (`/api/operacional/proyectos`). */
@Injectable({ providedIn: 'root' })
export class ProyectoService extends CrudService<Proyecto> {
  protected readonly resourceUrl = `${environment.apiBase}/operacional/proyectos`;

  obtenerPorCodigo(codigo: string): Observable<Proyecto> {
    return this.http.get<Proyecto>(`${this.resourceUrl}/codigo/${codigo}`);
  }
}
