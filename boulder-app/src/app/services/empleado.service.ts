import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { Cargo, Empleado } from '../models/base';

/**
 * Catálogo de personal (`/api/base/empleados`).
 *
 * Sustituye al antiguo `PerforistaService`: la tabla pasó de
 * `operacional.prf_perforista` a `base.empleado` y ahora incluye geólogos,
 * ayudantes de geología y personal de laboratorio.
 *
 * Las lecturas están abiertas a cualquier sesión, porque los partes las
 * necesitan para sus desplegables; las escrituras exigen rol ADMIN. No hay
 * DELETE: los partes y las mediciones referencian estas filas, así que se da
 * de baja con `cambiarEstado`.
 */
@Injectable({ providedIn: 'root' })
export class EmpleadoService {
  private readonly http = inject(HttpClient);
  private readonly resourceUrl = `${environment.apiBase}/base/empleados`;

  listar(): Observable<Empleado[]> {
    return this.http.get<Empleado[]>(this.resourceUrl);
  }

  /**
   * Solo los activos de un área (`perforacion`, `geologia`...). Es lo que se
   * usa para poblar un desplegable de personal en un parte.
   */
  listarPorArea(area: string): Observable<Empleado[]> {
    const params = new HttpParams().set('area', area);
    return this.http.get<Empleado[]>(this.resourceUrl, { params });
  }

  obtenerPorId(id: string): Observable<Empleado> {
    return this.http.get<Empleado>(`${this.resourceUrl}/${id}`);
  }

  obtenerPorCodigo(codigo: string): Observable<Empleado> {
    return this.http.get<Empleado>(`${this.resourceUrl}/codigo/${codigo}`);
  }

  crear(data: Empleado): Observable<Empleado> {
    return this.http.post<Empleado>(this.resourceUrl, data);
  }

  actualizar(id: string, data: Empleado): Observable<Empleado> {
    return this.http.put<Empleado>(`${this.resourceUrl}/${id}`, data);
  }

  cambiarEstado(id: string, activo: boolean): Observable<void> {
    const params = new HttpParams().set('activo', activo);
    return this.http.patch<void>(`${this.resourceUrl}/${id}/estado`, null, { params });
  }

  listarCargos(): Observable<Cargo[]> {
    return this.http.get<Cargo[]>(`${this.resourceUrl}/cargos`);
  }
}
