import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { Usuario } from '../models/base';

/**
 * La cuenta del usuario que inició sesión (`/api/cuenta`, pantalla P14).
 *
 * No hay identificador en la URL: el backend toma el usuario del token, así que
 * desde aquí no se puede tocar otra cuenta aunque se quisiera.
 */
@Injectable({ providedIn: 'root' })
export class CuentaService {

  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBase}/cuenta`;

  miCuenta(): Observable<Usuario> {
    return this.http.get<Usuario>(this.baseUrl);
  }

  cambiarContrasena(actual: string, nueva: string): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/contrasena`, { actual, nueva });
  }
}
