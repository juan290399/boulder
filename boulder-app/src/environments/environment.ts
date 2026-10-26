/**
 * Configuración de entorno por defecto (producción).
 * En desarrollo se reemplaza por `environment.development.ts`
 * mediante `fileReplacements` en angular.json.
 */
export const environment = {
  production: true,
  /** Base común de la API REST del backend Boulder. */
  apiBase: 'http://localhost:8080/api',
};
