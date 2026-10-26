import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map, shareReplay } from 'rxjs';

import { environment } from '../../environments/environment';

/** Entrada de cualquiera de los diccionarios de geología. */
export interface CatalogoGeologia {
  id: string;
  tipo: string;
  codigo: string;
  nombre: string;
  grupoId?: string | null;
  colorR?: number | null;
  colorG?: number | null;
  colorB?: number | null;
  numeroInterno?: number | null;
  estaActivo?: boolean;
}

/** Un tramo de quicklog tal como lo sube la aplicación móvil. */
/** Última sincronización de un tablet de GeoLog. */
export interface DispositivoSync {
  dispositivo: string;
  usuario: string | null;
  versionApp: string | null;
  ultimoContacto: string;
  ultimaSubida: string | null;
  filasSubidas: number;
}

export interface Quicklog {
  id: string;
  numeroInterno?: number;
  sondajeId: string;
  desde: number;
  hasta: number;
  longitud: number;
  litoId: string;
  altTipo1Id?: string | null;
  altIntensidad1Id?: string | null;
  altTipo2Id?: string | null;
  altIntensidad2Id?: string | null;
  minTipo1Id?: string | null;
  minTipo2Id?: string | null;
  minTipo3Id?: string | null;
  minEstilo1Id?: string | null;
  minEstilo2Id?: string | null;
  minEstilo3Id?: string | null;
}

/**
 * Lectura del logueo que sube la aplicación móvil (`/api/logueo`) y de los
 * diccionarios de geología (`/api/geologia/catalogos`).
 *
 * El backend devuelve el quicklog con identificadores crudos: `litoId`,
 * `altTipo1Id` y así. Sin el diccionario al lado, la pantalla sería una pared
 * de UUID. Por eso el catálogo se pide una sola vez, se indexa por id y se
 * comparte con `shareReplay`: son unos cientos de entradas que no cambian
 * durante la sesión y las necesita cada fila de la tabla.
 */
@Injectable({ providedIn: 'root' })
export class LogueoService {

  private readonly http = inject(HttpClient);
  private readonly logueoUrl = `${environment.apiBase}/logueo`;
  private readonly catalogoUrl = `${environment.apiBase}/geologia/catalogos`;

  private catalogo$?: Observable<Map<string, CatalogoGeologia>>;

  /** Diccionario completo indexado por id, resuelto una vez por sesión. */
  catalogoPorId(): Observable<Map<string, CatalogoGeologia>> {
    if (!this.catalogo$) {
      this.catalogo$ = this.http.get<CatalogoGeologia[]>(this.catalogoUrl).pipe(
        map(entradas => new Map(entradas.map(e => [e.id, e]))),
        shareReplay({ bufferSize: 1, refCount: false }),
      );
    }
    return this.catalogo$;
  }

  /** Tablets de GeoLog y cuándo sincronizó cada una. */
  dispositivos(): Observable<DispositivoSync[]> {
    return this.http.get<DispositivoSync[]>(`${this.logueoUrl}/dispositivos`);
  }

  quicklog(sondajeId: string): Observable<Quicklog[]> {
    const params = new HttpParams().set('sondajeId', sondajeId);
    return this.http.get<Quicklog[]>(`${this.logueoUrl}/quicklog`, { params });
  }
}
