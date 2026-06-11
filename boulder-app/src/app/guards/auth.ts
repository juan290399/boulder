import { inject } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  Router,
  RouterStateSnapshot
} from '@angular/router';

import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (
  route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot
) => {

  const router = inject(Router);
  const authService = inject(AuthService);

  const token = authService.getToken();

  if (!token) {
    console.warn('Sin token');
    return router.createUrlTree(['/auth/login']);
  }

  const rolesRequeridos = route.data['roles'] as string[];

  if (!rolesRequeridos || rolesRequeridos.length === 0) {
    return true;
  }

  const rolSeleccionado = authService.getSelectedRole();

  console.log('Rol seleccionado:', rolSeleccionado);
  console.log('Roles requeridos:', rolesRequeridos);

  if (!rolSeleccionado) {
    return router.createUrlTree(['/auth/seleccionar-rol']);
  }

  const tienePermiso = rolesRequeridos.some(r =>
    r.replace('ROLE_', '').toUpperCase() ===
    rolSeleccionado.replace('ROLE_', '').toUpperCase()
  );

  console.log('Tiene permiso:', tienePermiso);

  if (tienePermiso) {
    return true;
  }

  return router.createUrlTree(['/maquinas']);
};