import { inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Servicio CRUD genérico contra un recurso REST del backend Boulder.
 *
 * Convención de endpoints (Spring):
 *   GET    {resourceUrl}
 *   GET    {resourceUrl}/{id}
 *   POST   {resourceUrl}
 *   PUT    {resourceUrl}/{id}
 *   DELETE {resourceUrl}/{id}
 */
export abstract class CrudService<T> {
  protected readonly http = inject(HttpClient);

  /** URL absoluta del recurso, p.ej. `${environment.apiBase}/operacional/maquinas`. */
  protected abstract readonly resourceUrl: string;

  listar(): Observable<T[]> {
    return this.http.get<T[]>(this.resourceUrl);
  }

  obtenerPorId(id: string): Observable<T> {
    return this.http.get<T>(`${this.resourceUrl}/${id}`);
  }

  crear(data: T): Observable<T> {
    return this.http.post<T>(this.resourceUrl, data);
  }

  actualizar(id: string, data: T): Observable<T> {
    return this.http.put<T>(`${this.resourceUrl}/${id}`, data);
  }

  eliminar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.resourceUrl}/${id}`);
  }
}
