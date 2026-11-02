import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';

import { AuthService } from '../services/auth.service';
import { adminGuard, authGuard } from './auth';

describe('guards de autenticación', () => {
  const auth = {
    autenticado: false,
    efectivo: null as string | null,
    roles: [] as string[],
    isAuthenticated() { return this.autenticado; },
    getEffectiveRole() { return this.efectivo; },
    hasRole(r: string) { return this.roles.includes(r); },
    logout() { /* sin estado que limpiar en la prueba */ },
  };

  const ruta = (roles?: string[]) => ({ data: roles ? { roles } : {} }) as unknown as ActivatedRouteSnapshot;
  const estado = { url: '/reportes' } as RouterStateSnapshot;
  const destino = (r: unknown) => TestBed.inject(Router).serializeUrl(r as UrlTree);

  beforeEach(() => {
    auth.autenticado = false;
    auth.efectivo = null;
    auth.roles = [];
    TestBed.configureTestingModule({ providers: [provideRouter([]), { provide: AuthService, useValue: auth }] });
  });

  it('sin sesión manda al login y recuerda a dónde iba', () => {
    const r = TestBed.runInInjectionContext(() => authGuard(ruta(), estado));
    expect(destino(r)).toBe('/auth/login?redirectTo=%2Freportes');
  });

  it('con sesión deja pasar a una ruta sin roles', () => {
    auth.autenticado = true;
    expect(TestBed.runInInjectionContext(() => authGuard(ruta(), estado))).toBe(true);
  });

  it('una ruta con roles exige el rol con el que se está actuando', () => {
    auth.autenticado = true;
    auth.efectivo = 'VISITANTE';
    const r = TestBed.runInInjectionContext(() => authGuard(ruta(['GEOLOGO']), estado));
    expect(destino(r)).toBe('/sondajes');
  });

  it('un ADMIN que actúa con otro rol va a elegir rol, no a la zona operativa', () => {
    auth.autenticado = true;
    auth.efectivo = 'GEOLOGO';
    auth.roles = ['ADMIN', 'GEOLOGO'];
    const r = TestBed.runInInjectionContext(() => adminGuard(ruta(), estado));
    expect(destino(r)).toBe('/auth/seleccionar-rol');
  });

  it('el panel de administración abre con el rol ADMIN activo', () => {
    auth.autenticado = true;
    auth.efectivo = 'ADMIN';
    expect(TestBed.runInInjectionContext(() => adminGuard(ruta(), estado))).toBe(true);
  });
});
