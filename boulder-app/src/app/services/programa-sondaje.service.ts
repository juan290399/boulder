import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { CrudService } from '../core/crud.service';
import { ProgramaSondaje } from '../models/operacional';

/** Acceso REST a programas de sondaje (`/api/operacional/programas-sondaje`). */
@Injectable({ providedIn: 'root' })
export class ProgramaSondajeService extends CrudService<ProgramaSondaje> {
  protected readonly resourceUrl = `${environment.apiBase}/operacional/programas-sondaje`;

  obtenerPorCodigo(codigo: string): Observable<ProgramaSondaje> {
    return this.http.get<ProgramaSondaje>(`${this.resourceUrl}/codigo/${codigo}`);
  }

  listarPorProyecto(proyectoId: string): Observable<ProgramaSondaje[]> {
    return this.http.get<ProgramaSondaje[]>(`${this.resourceUrl}/proyecto/${proyectoId}`);
  }

  listarPorPlataforma(plataformaId: string): Observable<ProgramaSondaje[]> {
    return this.http.get<ProgramaSondaje[]>(`${this.resourceUrl}/plataforma/${plataformaId}`);
  }
}
