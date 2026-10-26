import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { CrudService } from '../core/crud.service';
import { Empresa } from '../models/operacional';

/** Acceso REST a empresas (`/api/operacional/empresas`). */
@Injectable({ providedIn: 'root' })
export class EmpresaService extends CrudService<Empresa> {
  protected readonly resourceUrl = `${environment.apiBase}/operacional/empresas`;

  /**
   * El backend indexa la búsqueda por RUC, no por código: `EmpresaRepository`
   * solo declara `findByRuc`. Antes esto apuntaba a `/codigo/{codigo}`, una
   * ruta que no existe, así que el buscador devolvía 404.
   */
  obtenerPorRuc(ruc: string): Observable<Empresa> {
    return this.http.get<Empresa>(`${this.resourceUrl}/ruc/${ruc}`);
  }
}
