/**
 * Configuración de entorno por defecto (producción).
 * En desarrollo se reemplaza por `environment.development.ts`
 * mediante `fileReplacements` en angular.json.
 */
export const environment = {
  production: true,
  /** Mismo origen: el jar de producción sirve la aplicación y el API. */
  apiBase: '/api',
};
