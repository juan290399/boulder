/**
 * Modelos de la API operacional de Boulder.
 * Espejo de los DTO del backend (`com.boulder.boulder.dto`).
 */

export interface Empresa {
  id?: string;
  numeroInterno?: number;
  codigo: string;
  nombre: string;
  ruc?: string;
  direccion?: string;
  telefono?: string;
  correo?: string;
}

export interface Proyecto {
  id?: string;
  numeroInterno?: number;
  empresaId?: string;
  codigo: string;
  nombre: string;
  ubicacion?: string;
  srid?: number;
}

export interface Maquina {
  id?: string;
  numeroInterno?: number;
  codigo: string;
  nombre: string;
  tipo?: string;
  alcancePq?: number;
  alcanceHq?: number;
  alcanceNq?: number;
  marca?: string;
  motor?: string;
  fechaCompra?: string;
  fechaLlegada?: string;
  fechaInicio?: string;
  estado?: string;
  notas?: string;
}

export interface Plataforma {
  id?: string;
  numeroInterno?: number;
  proyectoId?: string;
  codigo: string;
  nombre: string;
  este?: number;
  norte?: number;
  elevacion?: number;
  ejeMayor?: number;
  ejeMinor?: number;
  ejeZ?: number;
  area?: number;
  numeroPozas?: number;
  estado?: string;
  tipo?: string;
  ubicacion?: string;
  notas?: string;
}

export interface Collar {
  id?: string;
  numeroInterno?: number;
  codigo: string;
  nombre: string;
  este?: number;
  norte?: number;
  elevacion?: number;
  fechaMedicion?: string;
  campania?: string;
  notas?: string;
}

export interface ProgramaSondaje {
  id?: string;
  numeroInterno?: number;
  proyectoId?: string;
  plataformaId?: string;
  codigo: string;
  codigoOriginal?: string;
  nombre: string;
  este?: number;
  norte?: number;
  elevacion?: number;
  profundidad?: number;
  azimut?: number;
  inclinacion?: number;
  plano?: string;
  punto?: string;
  prioridad?: string;
  zona?: string;
  flanco?: string;
  notas?: string;
}

export interface Sondaje {
  id?: string;
  numeroInterno?: number;
  proyectoId?: string;
  programaSondajeId?: string;
  collarId?: string;
  plataformaId?: string;
  maquinaId?: string;
  codigo: string;
  nombre: string;
  azimutSetup?: number;
  inclinacionSetup?: number;
  profundidadActual?: number;
  profundidadFinal?: number;
  medicionUtilizada?: number;
  medicionesTotales?: number;
  fechaInicio?: string;
  fechaFin?: string;
  campania?: string;
  notas?: string;
}

export interface Survey {
  id?: string;
  numeroInterno?: number;
  sondajeId?: string;
  profundidad?: number;
  azimut?: number;
  inclinacion?: number;
  metodoMedicion?: string;
  medicionUtilizada?: number;
  qc?: string;
}

// `Perforista` se unificó en `Empleado` (models/base.ts): el catálogo de
// personal pasó de `operacional.prf_perforista` a `base.empleado` y ahora
// cubre también geólogos, ayudantes de geología y laboratorio.
//
// `DesviacionPerforista`, más abajo, SÍ se queda: es analítica de perforación
// ("desviación por perforista"), no un catálogo de personas.

export interface AvancePersonal {
  id?: string;
  perforistaId?: string;
  nombre: string;
  cargo?: string;
  codigoMaquina?: string;
}

export interface AvancePerforacion {
  id?: string;
  numeroInterno?: number;
  sondajeId?: string;
  sondajeCodigo?: string;
  fecha: string;
  turno?: string;
  responsable?: string;
  maquinaId?: string;
  maquinaCodigo?: string;
  plataformaId?: string;
  plataformaCodigo?: string;
  desde: number;
  hasta: number;
  avance: number;
  casingTipo?: string;
  casingDesde?: number;
  casingHasta?: number;
  retornoPct?: number;
  linea?: string;
  comentarios?: string;
  estado?: string;
  observaciones?: string;
  personal: AvancePersonal[];
}

export interface SurveyMedicion {
  id?: string;
  numeroInterno?: number;
  sondajeId: string;
  maquinaId?: string;
  perforistaId?: string;
  profundidad: number;
  nroMedicion?: number;
  fechaMedicion: string;
  fechaHora?: string;
  responsable?: string;
  metodo?: string;
  instrumento?: string;
  azimutMedido?: number;
  inclinacionMedida?: number;
  azimutPlan?: number;
  inclinacionPlan?: number;
  desvAzimut?: number;
  desvAzimutSentido?: string;
  desvInclinacion?: number;
  desvInclinacionSentido?: string;
  desvMetros?: number;
  desvTotal?: number;
  esPlanificada?: boolean;
  decision?: string;
  observaciones?: string;

  /** Por qué se desvió; la lectura geológica, no el número. */
  interpretacion?: string | null;

  /** Qué hacer; la indicación operativa para la máquina. */
  instruccion?: string | null;

  responsableSeguimiento?: string | null;

  /** sin_seguimiento | pendiente | en_curso | cerrado */
  estadoSeguimiento?: string;
}

export interface DesviacionPerforista {
  perforistaId: string;
  perforistaCodigo: string;
  perforista: string;
  nMediciones: number;
  desvAzPromAbs: number;
  desvAzMaxAbs: number;
  desvDipPromAbs: number;
  desvDipMaxAbs: number;
  desvMetrosProm: number;
  desvMetrosMax: number;
  nCorrecciones: number;
}

export interface DesviacionMaquina {
  maquinaId: string;
  maquinaCodigo: string;
  maquinaNombre: string;
  nMediciones: number;
  desvAzPromAbs: number;
  desvAzMaxAbs: number;
  desvDipPromAbs: number;
  desvDipMaxAbs: number;
  desvMetrosProm: number;
  desvMetrosMax: number;
  nCorrecciones: number;
}

export interface DesviacionSondaje {
  sondajeCodigo: string;
  sondajeNombre: string;
  profundidad: number;
  nroMedicion?: number;
  fechaMedicion: string;
  maquinaCodigo?: string;
  perforista?: string;
  desvAzimut?: number;
  desvAzimutSentido?: string;
  desvInclinacion?: number;
  desvInclinacionSentido?: string;
  desvMetros?: number;
  desvTotal?: number;
  decision?: string;

  /** Identificadores, para pedir la trayectoria y editar el seguimiento. */
  medicionId?: string;
  sondajeId?: string;

  /** Por qué se desvió; la lectura geológica, no el número. */
  interpretacion?: string | null;

  /** Qué hacer; la indicación operativa para la máquina. */
  instruccion?: string | null;

  responsableSeguimiento?: string | null;

  /** sin_seguimiento | pendiente | en_curso | cerrado */
  estadoSeguimiento?: string;
}

/** Una estación de la trayectoria medida de un sondaje. */
export interface EstacionSurvey {
  profundidad: number;
  azimut: number;
  inclinacion: number;
  qc?: string | null;
}

/**
 * Trayectoria de un sondaje: lo planificado contra lo medido.
 *
 * El plan llega como dos escalares y no como serie porque eso es lo que hay:
 * el sondaje se planifica con un azimut y una inclinación de partida, y el
 * trazo planificado es esa recta.
 */
export interface TrayectoriaSondaje {
  sondajeId: string;
  sondajeCodigo: string;
  sondajeNombre?: string | null;
  azimutPlan?: number | null;
  inclinacionPlan?: number | null;
  profundidadActual?: number | null;
  profundidadFinal?: number | null;
  estaciones: EstacionSurvey[];
  desvAzimutUltimo?: number | null;
  desvInclinacionUltimo?: number | null;
  profundidadUltima?: number | null;
}

/* ---------------------------------------------------------------------------
 * Avances operativos (CU12 a CU18)
 *
 * Los siete partes del flujo posterior a la perforación comparten cabecera, y
 * el backend los expone con la misma forma. Se refleja esa herencia acá para
 * que la pantalla de avances pueda tratarlos de manera uniforme.
 * ------------------------------------------------------------------------ */

/** Cabecera común de todos los partes de avance operativo. */
export interface AvanceOperativo {
  id?: string;
  numeroInterno?: number;
  sondajeId?: string | null;
  /** Código tal como se escribió en el parte de papel. */
  sondajeCodigo?: string | null;
  /** Código canónico del sondaje relacionado; lo resuelve el backend. */
  sondajeCodigoReal?: string | null;
  fecha: string;
  turno?: string | null;
  estado?: string;
  observaciones?: string | null;
}

/** Parte de avance de logueo (CU18). */
export interface AvanceLogueo extends AvanceOperativo {
  tipoLogueo: string;
  desde: number;
  hasta: number;
  avance: number;
  geologo?: string | null;
  pctCompletado?: number | null;
}

/** Recepción de cajas de campo (CU12). */
export interface RecepcionCaja extends AvanceOperativo {
  responsable?: string | null;
  boxDesde?: number | null;
  boxHasta?: number | null;
  totalCajas?: number | null;
  marcadoDesde?: number | null;
  marcadoHasta?: number | null;
  totalMetraje?: number | null;
}

/** Registro fotográfico de cajas (CU13). */
export interface AvanceFotografia extends AvanceOperativo {
  responsable?: string | null;
  boxDesde?: number | null;
  boxHasta?: number | null;
  totalCajas?: number | null;
  marcadoDesde?: number | null;
  marcadoHasta?: number | null;
  totalMetraje?: number | null;
  secoHumedo?: string | null;
  nroFotos?: number | null;
  rutaArchivos?: string | null;
}

/** Corte de testigo para muestreo geoquímico (CU14). */
export interface CorteMuestra extends AvanceOperativo {
  responsable?: string | null;
  desde: number;
  hasta: number;
  tipoLinea?: string | null;
  metrajeEstimado?: number | null;
  nroMuestras?: number | null;
}

/** Avance de muestreo geoquímico (CU17). */
export interface AvanceMuestreo extends AvanceOperativo {
  responsable?: string | null;
  estadoMuestreo: string;
  desde?: number | null;
  hasta?: number | null;
  avance?: number | null;
  nroMuestras?: number | null;
  nroMuestrasEmbolsadas?: number | null;
  nroCostales?: number | null;
  nroLotes?: number | null;
}

/** Corte de testigo para ensayo de densidad (CU15). */
export interface CorteDensidad extends AvanceOperativo {
  responsable?: string | null;
  desde: number;
  hasta: number;
  totalMuestras: number;
  totalMetraje?: number | null;
  tipoLinea?: string | null;
}

/** Procesamiento de densidad en estufa (CU16). */
export interface AvanceDensidad extends AvanceOperativo {
  responsable?: string | null;
  etapa: string;
  nroMuestras: number;
  temperaturaC?: number | null;
  tipoLinea?: string | null;
  desde?: number | null;
  hasta?: number | null;
}

/** Una etapa del flujo de testigo dentro del frente de avance. */
export interface EtapaAvance {
  clave: string;
  nombre: string;
  profundidad: number | null;
  rezago: number | null;
  pctSobrePerforado: number | null;
}

/** Estado consolidado de un sondaje a lo largo de las siete etapas. */
export interface FrenteAvance {
  sondajeId: string;
  sondajeCodigo: string;
  sondajeNombre?: string | null;
  profundidadActual?: number | null;
  profundidadFinal?: number | null;
  fechaInicio?: string | null;
  fechaFin?: string | null;
  ultimaActividad?: string | null;
  activo: boolean;
  diasSinMovimiento?: number | null;
  muestrasEmbolsadas?: number | null;
  cajasRecibidas?: number | null;
  pctProgramado?: number | null;
  etapas: EtapaAvance[];
  etapaCritica?: string | null;
  rezagoMaximo?: number | null;
}
