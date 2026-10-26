import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { CrudService } from '../core/crud.service';
import { Plataforma } from '../models/operacional';

/** Acceso REST a plataformas de perforación (`/api/operacional/plataformas`). */
@Injectable({ providedIn: 'root' })
export class PlataformaService extends CrudService<Plataforma> {
  protected readonly resourceUrl = `${environment.apiBase}/operacional/plataformas`;

  obtenerPorCodigo(codigo: string): Observable<Plataforma> {
    return this.http.get<Plataforma>(`${this.resourceUrl}/codigo/${codigo}`);
  }

  listarPorProyecto(proyectoId: string): Observable<Plataforma[]> {
    return this.http.get<Plataforma[]>(`${this.resourceUrl}/proyecto/${proyectoId}`);
  }
}
