import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { Usuario } from '../models/base';

/**
 * Administración de cuentas (`/api/base/usuarios`).
 *
 * Todos los endpoints requieren sesión iniciada. No hay DELETE en el backend:
 * las cuentas se desactivan con `cambiarEstado`, no se borran.
 */
@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly http = inject(HttpClient);
  private readonly resourceUrl = `${environment.apiBase}/base/usuarios`;

  listar(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.resourceUrl);
  }

  obtenerPorId(id: string): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.resourceUrl}/${id}`);
  }

  obtenerPorUsuario(username: string): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.resourceUrl}/username/${username}`);
  }

  crear(data: Usuario): Observable<Usuario> {
    return this.http.post<Usuario>(this.resourceUrl, data);
  }

  actualizar(id: string, data: Usuario): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.resourceUrl}/${id}`, data);
  }

  /**
   * Reemplaza el conjunto completo de roles de la cuenta.
   *
   * Es un reemplazo, no un alta incremental: los roles que no vayan en la lista
   * quedan revocados, y una lista vacía los quita todos. Los códigos son los de
   * `base.rol.codigo` (ADMIN, GEOLOGO...), sin el prefijo `ROLE_`.
   *
   * El backend rechaza que un administrador se quite ADMIN a sí mismo, y
   * cualquier cambio que dejaría al sistema sin administradores activos.
   */
  asignarRoles(id: string, codigos: string[]): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.resourceUrl}/${id}/roles`, codigos);
  }

  /** Activa o suspende una cuenta. */
  cambiarEstado(id: string, activo: boolean): Observable<void> {
    const params = new HttpParams().set('activo', activo);
    return this.http.patch<void>(`${this.resourceUrl}/${id}/estado`, null, { params });
  }
}
