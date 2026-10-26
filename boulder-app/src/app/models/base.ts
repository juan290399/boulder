/**
 * Modelos del esquema `base` (cuentas y autorización).
 * Espejo de los DTO del backend: RolDTO, UsuarioDTO y RegistroDTO.
 */

export interface Rol {
  id?: string;
  numeroInterno?: number;
  codigo: string;
  nombre: string;
}

export interface Usuario {
  id?: string;
  numeroInterno?: number;
  nombres: string;
  apellidos: string;
  correo: string;
  usuario: string;
  /** Solo se envía; el backend nunca lo devuelve. */
  contrasenia?: string;
  estado?: boolean;
  roles?: string[];
  /** Empleado del catálogo al que pertenece la cuenta; opcional. */
  empleadoId?: string;
  /** Solo lectura. */
  empleadoNombre?: string;
}

export interface Cargo {
  id?: string;
  numeroInterno?: number;
  codigo: string;
  nombre: string;
  /** perforacion | geologia | laboratorio | administracion */
  area: string;
  estado?: boolean;
}

/**
 * Persona que trabaja en la operación, tenga o no cuenta de acceso.
 * Sustituye al antiguo `Perforista` de models/operacional.ts.
 */
export interface Empleado {
  id?: string;
  numeroInterno?: number;
  codigo: string;
  nombres: string;
  apellidos?: string;
  documento?: string;
  empresaId?: string;
  cargoId?: string;
  /** Solo lectura: lo rellena el backend al convertir. */
  cargoNombre?: string;
  cargoArea?: string;
  email?: string;
  telefono?: string;
  fechaIngreso?: string;
  fechaSalida?: string;
  estado?: boolean;
  notas?: string;
}

/** Cuerpo de POST /api/auth/register. */
export interface Registro {
  nombres: string;
  apellidos: string;
  correo: string;
  usuario: string;
  contrasenia: string;
}

/** Cuerpo de POST /api/auth/login. */
export interface Credenciales {
  usuario: string;
  contrasenia: string;
}
