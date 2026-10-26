import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { CatalogoGeologia } from './logueo.service';

/** Qué columnas opcionales tiene un catálogo y cuántos caracteres admite su código. */
export interface EstructuraCatalogo {
  columnas: string[];
  largoCodigo: number;
}

/**
 * Mantenimiento de los diccionarios geológicos (`/api/geologia/catalogos`,
 * pantalla P15). Leer lo puede cualquier rol; escribir, solo ADMIN: el backend lo
 * exige con `@PreAuthorize`, esta capa no decide nada de permisos.
 */
@Injectable({ providedIn: 'root' })
export class DiccionarioService {

  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBase}/geologia/catalogos`;

  listar(): Observable<CatalogoGeologia[]> {
    return this.http.get<CatalogoGeologia[]>(this.baseUrl);
  }

  estructura(): Observable<Record<string, EstructuraCatalogo>> {
    return this.http.get<Record<string, EstructuraCatalogo>>(`${this.baseUrl}/estructura`);
  }

  crear(tipo: string, datos: Partial<CatalogoGeologia>): Observable<CatalogoGeologia> {
    return this.http.post<CatalogoGeologia>(`${this.baseUrl}/${encodeURIComponent(tipo)}`, datos);
  }

  actualizar(tipo: string, id: string, datos: Partial<CatalogoGeologia>): Observable<CatalogoGeologia> {
    return this.http.put<CatalogoGeologia>(`${this.baseUrl}/${encodeURIComponent(tipo)}/${id}`, datos);
  }

  cambiarEstado(tipo: string, id: string, activo: boolean): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/${encodeURIComponent(tipo)}/${id}/estado`, null, {
      params: new HttpParams().set('activo', activo),
    });
  }
}
