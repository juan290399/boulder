import { inject } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  Router,
  RouterStateSnapshot
} from '@angular/router';

import { AuthService } from '../services/auth.service';

/** Adonde va a parar quien tiene sesión válida pero no permiso para la ruta. */
const RUTA_OPERATIVA = '/sondajes';

/**
 * Exige sesión válida y, si la ruta declara `data.roles`, que el rol con el que
 * el usuario está actuando sea uno de ellos.
 *
 * <p>
 * El rol se valida contra el JWT, no contra localStorage: el rol seleccionado
 * es una preferencia del cliente y cualquiera puede reescribirla desde la
 * consola del navegador. Ver {@code AuthService.getEffectiveRole()}.
 * </p>
 *
 * <p>
 * Esto es una comodidad de navegación, no la barrera de seguridad. Quien
 * decide es el backend: {@code /api/base/**} exige ROLE_ADMIN y responde 403
 * aunque el usuario consiga dibujar la pantalla.
 * </p>
 */
export const authGuard: CanActivateFn = (
  route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot
) => {

  const router = inject(Router);
  const authService = inject(AuthService);

  if (!authService.isAuthenticated()) {
    // Cubre tanto la ausencia de token como la sesión caducada. Se limpia lo
    // que quede para no dejar un rol seleccionado huérfano de token.
    authService.logout();
    return router.createUrlTree(['/auth/login'], {
      queryParams: { redirectTo: state.url },
    });
  }

  const rolesRequeridos = (route.data['roles'] as string[] | undefined) ?? [];

  if (rolesRequeridos.length === 0) {
    return true;
  }

  const rolActivo = authService.getEffectiveRole();

  if (!rolActivo) {
    return router.createUrlTree(['/auth/seleccionar-rol']);
  }

  const normalizar = (r: string) => r.replace(/^ROLE_/, '').toUpperCase();
  const permitido = rolesRequeridos.some(r => normalizar(r) === normalizar(rolActivo));

  return permitido ? true : router.createUrlTree([RUTA_OPERATIVA]);
};

/**
 * Atajo para las rutas de administración: equivale a declarar
 * `data: { roles: ['ADMIN'] }`, pero deja la intención a la vista en
 * `app.routes.ts`.
 */
export const adminGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const authService = inject(AuthService);

  if (!authService.isAuthenticated()) {
    authService.logout();
    return router.createUrlTree(['/auth/login'], {
      queryParams: { redirectTo: state.url },
    });
  }

  if (authService.getEffectiveRole() === 'ADMIN') {
    return true;
  }

  // Tiene el rol pero está actuando con otro: que lo cambie en vez de
  // mandarlo a la zona operativa como si no tuviera permiso.
  if (authService.hasRole('ADMIN')) {
    return router.createUrlTree(['/auth/seleccionar-rol']);
  }

  return router.createUrlTree([RUTA_OPERATIVA]);
};

/**
 * Ruta de salida: cierra la sesión y devuelve al login.
 *
 * <p>
 * Antes {@code /auth/logout} era un simple {@code redirectTo}, así que no
 * borraba nada: bastaba con volver atrás en el navegador para seguir dentro.
 * </p>
 */
export const logoutGuard: CanActivateFn = () => {
  const router = inject(Router);
  inject(AuthService).logout();
  return router.createUrlTree(['/auth/login']);
};
