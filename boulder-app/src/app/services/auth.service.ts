import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { Credenciales, Registro, Usuario } from '../models/base';

const TOKEN_KEY = 'token';
const ROLE_KEY = 'user_role';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly http = inject(HttpClient);
  private readonly authUrl = `${environment.apiBase}/auth`;

  login(credentials: Credenciales): Observable<{ token: string }> {
    return this.http.post<{ token: string }>(`${this.authUrl}/login`, credentials);
  }

  /**
   * Auto-registro. La cuenta se crea INACTIVA y sin roles: hasta que un
   * administrador la habilite, el login responderá "usuario inactivo".
   */
  register(data: Registro): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.authUrl}/register`, data);
  }

  saveToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  /**
   * Hay token y todavía no ha expirado.
   *
   * <p>Comprobar solo la presencia del token deja pasar sesiones vencidas: el
   * guard aprueba la navegación y el fallo aparece después, como un 401 seco
   * en mitad de una pantalla ya dibujada.</p>
   */
  isAuthenticated(): boolean {
    return this.getToken() !== null && !this.isTokenExpired();
  }

  /** `exp` del JWT en milisegundos, o null si el token no lo trae. */
  getExpiration(): number | null {
    const exp = this.getTokenPayload()?.['exp'];
    // El claim viene en segundos desde epoch, no en milisegundos.
    return typeof exp === 'number' ? exp * 1000 : null;
  }

  isTokenExpired(): boolean {
    const exp = this.getExpiration();
    // Un token sin `exp` no se puede dar por caducado; el backend lo rechazará
    // si corresponde.
    return exp !== null && Date.now() >= exp;
  }

  /**
   * Rol con el que el usuario está actuando, siempre que el JWT lo respalde.
   *
   * <p>El rol elegido vive en localStorage, que es del cliente y por tanto
   * editable: sin contrastarlo contra el token, escribir
   * `localStorage.setItem('user_role','ADMIN')` en la consola bastaba para
   * abrir el panel de administración.</p>
   */
  getEffectiveRole(): string | null {
    const seleccionado = this.getSelectedRole();
    if (!seleccionado) {
      return null;
    }
    return this.getRolesFromToken().includes(seleccionado) ? seleccionado : null;
  }

  /** El JWT incluye este rol, con independencia del que esté seleccionado. */
  hasRole(codigo: string): boolean {
    return this.getRolesFromToken().includes(codigo);
  }

  /**
   * Roles reales del JWT emitido por el backend.
   *
   * MyUserDetailsService emite las authorities como `ROLE_<codigo>` (convención
   * interna de Spring Security), así que aquí se quita el prefijo para volver
   * al código tal como está en `base.rol`: ADMIN, GEOLOGO, VISITANTE...
   */
  getRolesFromToken(): string[] {
    const payload = this.getTokenPayload();
    const roles = payload?.['roles'];
    if (!Array.isArray(roles)) {
      return [];
    }
    return roles
      .filter((r): r is string => typeof r === 'string')
      .map((r) => (r.startsWith('ROLE_') ? r.slice(5) : r));
  }

  /**
   * Nombre y apellidos para mostrar, tomados del claim `nombre`.
   *
   * <p>Cae al `sub` (el identificador de acceso) si el token no trae el claim,
   * que es el caso de los emitidos antes de añadirlo.</p>
   */
  getNombreFromToken(): string | null {
    const nombre = this.getTokenPayload()?.['nombre'];
    return typeof nombre === 'string' && nombre.length > 0
      ? nombre
      : this.getUsuarioFromToken();
  }

  /** Iniciales para el avatar: "Carla Mendoza" -> "CM". */
  getInicialesFromToken(): string {
    const nombre = this.getNombreFromToken();
    if (!nombre) {
      return '?';
    }
    return nombre
      .split(/\s+/)
      .slice(0, 2)
      .map((parte) => parte.charAt(0).toUpperCase())
      .join('');
  }

  /** `sub` del JWT: el nombre de usuario autenticado. */
  getUsuarioFromToken(): string | null {
    const payload = this.getTokenPayload();
    return typeof payload?.['sub'] === 'string' ? payload['sub'] : null;
  }

  private getTokenPayload(): Record<string, unknown> | null {
    const token = this.getToken();
    if (!token) {
      return null;
    }

    try {
      const payloadBase64 = token.split('.')[1];
      // El payload viene en base64url: hay que normalizarlo antes de atob().
      const normalized = payloadBase64.replace(/-/g, '+').replace(/_/g, '/');
      return JSON.parse(atob(normalized));
    } catch (error) {
      console.error('Error parseando JWT:', error);
      return null;
    }
  }

  getSelectedRole(): string | null {
    return localStorage.getItem(ROLE_KEY);
  }

  setSelectedRole(rol: string): void {
    localStorage.setItem(ROLE_KEY, rol);
  }

  /**
   * Cierra la sesión borrando únicamente las claves propias.
   *
   * <p>`localStorage.clear()` arrasa con todo el almacenamiento del origen,
   * incluido lo que guarden otras partes de la aplicación.</p>
   */
  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(ROLE_KEY);
  }
}
