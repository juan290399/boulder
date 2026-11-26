/**
 * Cómo se presentan los roles en la interfaz: nombre corto, ícono de Bootstrap
 * Icons y alcance en una frase. Lo usan la elección de rol y la pantalla de
 * roles del panel de administración.
 */
export interface PresentacionRol {
  etiqueta: string;
  icono: string;
  detalle: string;
}

export const ROLES: Record<string, PresentacionRol> = {
  ADMIN: { etiqueta: 'Administrador', icono: 'bi-shield-lock', detalle: 'Cuentas, roles y datos maestros.' },
  GEOLOGO: { etiqueta: 'Geólogo', icono: 'bi-person-badge', detalle: 'Revisa los partes y mantiene los catálogos.' },
  ASISTENTE_GEOLOGIA: { etiqueta: 'Asistente de geología', icono: 'bi-clipboard-check', detalle: 'Registra los partes del turno.' },
  VISITANTE: { etiqueta: 'Visitante', icono: 'bi-eye', detalle: 'Consulta avances y reportes, sin editar.' },
};

export function presentacionRol(codigo: string): PresentacionRol {
  return ROLES[codigo] ?? { etiqueta: codigo, icono: 'bi-person', detalle: '' };
}

/**
 * Qué permite cada rol, en el mismo orden de menos a más. Refleja las reglas
 * que aplican el backend (SecurityConfig y PermisosEscritura) y el cliente
 * (AuthService.puedeRegistrar / puedeMantenerMaestros, adminGuard): si cambian
 * allá, se actualiza aquí.
 */
export const PERMISOS: { accion: string; roles: string[] }[] = [
  {
    accion: 'Consultar avances, sondajes, desvíos, quicklog, fotos y reportes',
    roles: ['ADMIN', 'GEOLOGO', 'ASISTENTE_GEOLOGIA', 'VISITANTE'],
  },
  {
    accion: 'Registrar partes, mediciones de desvío y fotos; cargar plantillas Excel',
    roles: ['ADMIN', 'GEOLOGO', 'ASISTENTE_GEOLOGIA'],
  },
  {
    accion: 'Mantener sondajes, plataformas y máquinas; borrar partes',
    roles: ['ADMIN', 'GEOLOGO'],
  },
  {
    accion: 'Administrar cuentas, empresas, proyectos, empleados y diccionarios',
    roles: ['ADMIN'],
  },
];
