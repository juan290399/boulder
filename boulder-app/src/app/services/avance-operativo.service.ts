import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { CrudService } from '../core/crud.service';
import {
  AvanceDensidad,
  AvanceFotografia,
  AvanceLogueo,
  AvanceMuestreo,
  AvanceOperativo,
  CorteDensidad,
  CorteMuestra,
  RecepcionCaja,
} from '../models/operacional';

/** Filtros que aceptan por igual los siete listados de partes. */
export interface FiltroAvance {
  sondajeId?: string;
  fecha?: string;
  desde?: string;
  hasta?: string;
}

/**
 * Acceso REST a los partes de avance operativo (UC16 a UC20).
 *
 * Se apoya en {@link CrudService}, que ya resuelve los cinco verbos contra la
 * convención de rutas del backend; lo único que agrega es el filtrado, que en
 * los siete partes es idéntico. Las subclases de abajo solo declaran su
 * segmento de ruta.
 */
export abstract class AvanceOperativoApi<T extends AvanceOperativo> extends CrudService<T> {

  /** Segmento bajo /api/operacional que atiende este parte. */
  protected abstract readonly segmento: string;

  protected get resourceUrl(): string {
    return `${environment.apiBase}/operacional/${this.segmento}`;
  }

  /**
   * Los filtros son excluyentes y el backend los resuelve por precedencia:
   * sondaje, después rango, después fecha suelta. Mandar varios no es un
   * error, pero solo el primero tiene efecto.
   */
  override listar(filtro: FiltroAvance = {}): Observable<T[]> {
    let params = new HttpParams();
    if (filtro.sondajeId) { params = params.set('sondajeId', filtro.sondajeId); }
    if (filtro.fecha)     { params = params.set('fecha', filtro.fecha); }
    if (filtro.desde)     { params = params.set('desde', filtro.desde); }
    if (filtro.hasta)     { params = params.set('hasta', filtro.hasta); }
    return this.http.get<T[]>(this.resourceUrl, { params });
  }
}

@Injectable({ providedIn: 'root' })
export class AvanceLogueoService extends AvanceOperativoApi<AvanceLogueo> {
  protected readonly segmento = 'avances-logueo';
}

@Injectable({ providedIn: 'root' })
export class RecepcionCajaService extends AvanceOperativoApi<RecepcionCaja> {
  protected readonly segmento = 'recepcion-cajas';
}

@Injectable({ providedIn: 'root' })
export class AvanceFotografiaService extends AvanceOperativoApi<AvanceFotografia> {
  protected readonly segmento = 'avances-fotografia';
}

@Injectable({ providedIn: 'root' })
export class CorteMuestraService extends AvanceOperativoApi<CorteMuestra> {
  protected readonly segmento = 'cortes-muestra';
}

@Injectable({ providedIn: 'root' })
export class AvanceMuestreoService extends AvanceOperativoApi<AvanceMuestreo> {
  protected readonly segmento = 'avances-muestreo';
}

@Injectable({ providedIn: 'root' })
export class CorteDensidadService extends AvanceOperativoApi<CorteDensidad> {
  protected readonly segmento = 'cortes-densidad';
}

@Injectable({ providedIn: 'root' })
export class AvanceDensidadService extends AvanceOperativoApi<AvanceDensidad> {
  protected readonly segmento = 'avances-densidad';
}
