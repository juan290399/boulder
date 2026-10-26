import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { Plantilla, ResultadoImportacion } from '../models/importacion';

/**
 * Carga masiva desde las plantillas de Excel (`/api/importacion`).
 *
 * El parámetro `aplicar` es lo único que separa probar de cargar. Va en falso
 * por omisión, del lado del servidor también: el camino corto tiene que ser el
 * que no toca la base.
 */
@Injectable({ providedIn: 'root' })
export class ImportacionService {

  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBase}/importacion`;

  plantillas(): Observable<Plantilla[]> {
    return this.http.get<Plantilla[]>(`${this.baseUrl}/plantillas`);
  }

  importar(plantilla: string, archivo: File, aplicar: boolean): Observable<ResultadoImportacion> {
    const cuerpo = new FormData();
    cuerpo.append('archivo', archivo, archivo.name);
    const params = new HttpParams().set('aplicar', aplicar);
    // Sin Content-Type a mano: el navegador tiene que poner el boundary del
    // multipart, y si se lo fijamos nosotros lo pone sin él y el servidor no
    // encuentra la parte.
    return this.http.post<ResultadoImportacion>(`${this.baseUrl}/${plantilla}`, cuerpo, { params });
  }
}
