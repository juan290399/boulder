import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map, shareReplay } from 'rxjs';

import { environment } from '../../environments/environment';

/** Una foto de cajas de testigo. Espejo de la entidad `Foto`. */
export interface Foto {
  id: string;
  sondajeId: string;
  cajaDesde: number | null;
  cajaHasta: number | null;
  desde: number | null;
  hasta: number | null;
  condicion: 'seco' | 'humedo' | null;
  tomadaEn: string | null;
  autor: string | null;
  dispositivo: string | null;
  archivoOriginal: string | null;
  bytes: number;
  ancho: number | null;
  alto: number | null;
  creadoEn: string;
}

/** Lo que acompaña a un archivo al subirlo. */
export interface MetadatosFoto {
  sondajeId: string;
  cajaDesde?: number | null;
  cajaHasta?: number | null;
  desde?: number | null;
  hasta?: number | null;
  condicion?: 'seco' | 'humedo' | null;
  observaciones?: string | null;
}

/**
 * Fotos de testigo (`/api/fotos`).
 *
 * Las imágenes se piden con `HttpClient` —que lleva el token por el
 * interceptor— y se convierten en URL locales (`blob:`). Un `<img src>` directo
 * al API no mandaría el token, y ponerlo en la URL lo dejaría en el historial
 * y en el registro de cualquier proxy. Las URL ya creadas se reutilizan: una
 * miniatura se baja una sola vez por sesión.
 */
@Injectable({ providedIn: 'root' })
export class FotoService {

  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiBase}/fotos`;
  private readonly cache = new Map<string, Observable<string>>();

  listar(sondajeId: string): Observable<Foto[]> {
    return this.http.get<Foto[]>(this.url, { params: new HttpParams().set('sondajeId', sondajeId) });
  }

  /** URL local de la miniatura o del original, lista para un `<img>`. */
  imagen(id: string, tamanio: 'miniatura' | 'contenido'): Observable<string> {
    const clave = `${id}/${tamanio}`;
    let obs = this.cache.get(clave);
    if (!obs) {
      obs = this.http.get(`${this.url}/${id}/${tamanio}`, { responseType: 'blob' })
        .pipe(map(b => URL.createObjectURL(b)), shareReplay(1));
      this.cache.set(clave, obs);
    }
    return obs;
  }

  /**
   * Sube un archivo. El id lo pone el navegador: si la conexión se corta y se
   * reintenta, el servidor reconoce la foto y no la duplica.
   */
  subir(archivo: File, m: MetadatosFoto, id: string = crypto.randomUUID()): Observable<Foto> {
    const cuerpo = new FormData();
    cuerpo.append('archivo', archivo, archivo.name);
    cuerpo.append('sondajeId', m.sondajeId);
    const opcionales: [string, unknown][] = [
      ['cajaDesde', m.cajaDesde], ['cajaHasta', m.cajaHasta], ['desde', m.desde], ['hasta', m.hasta],
      ['condicion', m.condicion], ['observaciones', m.observaciones],
    ];
    for (const [k, v] of opcionales) {
      if (v !== null && v !== undefined && v !== '') {
        cuerpo.append(k, String(v));
      }
    }
    return this.http.put<Foto>(`${this.url}/${id}`, cuerpo);
  }

  eliminar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}

/**
 * Lee cajas, tramo y condición del nombre del archivo, si sigue la convención
 * habitual de las fotos de testigo:
 * `DDH-VERO-S-040-26_C133-142_247.25-283.95_S.jpg`.
 *
 * Cada parte es opcional; lo que no se reconoce queda en blanco para que el
 * usuario lo complete. No intenta adivinar: un número suelto no se toma por
 * profundidad.
 */
export function leerNombreFoto(nombre: string): Partial<MetadatosFoto> {
  const base = nombre.replace(/\.[^.]+$/, '');
  const resultado: Partial<MetadatosFoto> = {};
  const cajas = base.match(/(?:^|[_\s-])C(?:AJ(?:A|AS)?)?[_-]?(\d{1,4})[-_](\d{1,4})(?=$|[_\s])/i);
  if (cajas) {
    resultado.cajaDesde = Number(cajas[1]);
    resultado.cajaHasta = Number(cajas[2]);
  }
  const tramo = base.match(/(?:^|_)(\d{1,4}(?:[.,]\d{1,2})?)-(\d{1,4}(?:[.,]\d{1,2})?)(?:m)?(?=$|_)/i);
  if (tramo) {
    resultado.desde = Number(tramo[1].replace(',', '.'));
    resultado.hasta = Number(tramo[2].replace(',', '.'));
  }
  const cond = base.match(/_(S|H|SECO|HUMEDO|HÚMEDO)$/i);
  if (cond) {
    resultado.condicion = cond[1].toUpperCase().startsWith('S') ? 'seco' : 'humedo';
  }
  return resultado;
}
