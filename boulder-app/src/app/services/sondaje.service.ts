import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { CrudService } from '../core/crud.service';
import { Sondaje } from '../models/operacional';

/** Acceso REST a sondajes ejecutados (`/api/operacional/sondajes`). */
@Injectable({ providedIn: 'root' })
export class SondajeService extends CrudService<Sondaje> {
  protected readonly resourceUrl = `${environment.apiBase}/operacional/sondajes`;

  obtenerPorCodigo(codigo: string): Observable<Sondaje> {
    return this.http.get<Sondaje>(`${this.resourceUrl}/codigo/${codigo}`);
  }

  listarPorProyecto(proyectoId: string): Observable<Sondaje[]> {
    return this.http.get<Sondaje[]>(`${this.resourceUrl}/proyecto/${proyectoId}`);
  }

  listarPorPlataforma(plataformaId: string): Observable<Sondaje[]> {
    return this.http.get<Sondaje[]>(`${this.resourceUrl}/plataforma/${plataformaId}`);
  }

  listarPorMaquina(maquinaId: string): Observable<Sondaje[]> {
    return this.http.get<Sondaje[]>(`${this.resourceUrl}/maquina/${maquinaId}`);
  }
}
