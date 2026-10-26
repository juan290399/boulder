import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { Rol } from '../models/base';

/**
 * Catálogo de roles del sistema (`GET /api/base/roles`).
 * Solo lectura: el backend no expone alta/baja de roles.
 * Requiere sesión iniciada.
 */
@Injectable({ providedIn: 'root' })
export class RolService {
  private readonly http = inject(HttpClient);
  private readonly resourceUrl = `${environment.apiBase}/base/roles`;

  listar(): Observable<Rol[]> {
    return this.http.get<Rol[]>(this.resourceUrl);
  }
}
