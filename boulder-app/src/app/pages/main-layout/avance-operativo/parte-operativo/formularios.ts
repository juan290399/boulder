import { Type } from '@angular/core';

import {
  AvanceDensidadService,
  AvanceFotografiaService,
  AvanceLogueoService,
  AvanceMuestreoService,
  AvanceOperativoApi,
  CorteDensidadService,
  CorteMuestraService,
  RecepcionCajaService,
} from '../../../../services/avance-operativo.service';
import { AvanceOperativo } from '../../../../models/operacional';

/** Cómo se captura y se muestra un campo del parte. */
export type TipoCampo = 'texto' | 'metros' | 'entero' | 'decimal' | 'porcentaje' | 'opcion' | 'linea' | 'calculado';

export interface CampoParte {
  campo: string;
  etiqueta: string;
  tipo: TipoCampo;
  requerido?: boolean;
  /** Ancho en la rejilla de 12 columnas, en pantalla mediana. */
  ancho?: number;
  opciones?: { valor: string; texto: string }[];
  ayuda?: string;
}

/**
 * Descripción declarativa del formulario de un parte de testigo.
 *
 * Los siete partes comparten cabecera (fecha, turno, observaciones) y difieren
 * en el detalle. En vez de siete formularios casi iguales, uno solo se arma con
 * esta descripción: agregar un parte nuevo es agregar una entrada, sin tocar el
 * componente.
 */
export interface FormularioParte {
  servicio: Type<AvanceOperativoApi<AvanceOperativo>>;
  campos: CampoParte[];
  /** Tramos y rangos que no pueden terminar antes de empezar: [desde, hasta, nombre]. */
  rangos: [string, string, string][];
  /** Lo que se hereda del último parte tal cual: el responsable, la línea… */
  copia: string[];
  /**
   * Lo que continúa donde terminó el último parte: el campo nuevo toma el valor
   * del campo indicado del anterior (en cajas, el siguiente número).
   */
  continua: Record<string, string>;
  /** Totales que se deducen de un rango mientras se escribe; se pueden corregir a mano. */
  deriva: { campo: string; desde: string; hasta: string; cajas?: boolean }[];
}

const RESPONSABLE: CampoParte = { campo: 'responsable', etiqueta: 'Responsable', tipo: 'texto', ancho: 6 };
const LINEA: CampoParte = { campo: 'tipoLinea', etiqueta: 'Línea', tipo: 'linea', ancho: 3 };
const DESDE = (requerido = true): CampoParte => ({ campo: 'desde', etiqueta: 'Desde (m)', tipo: 'metros', requerido, ancho: 3 });
const HASTA = (requerido = true): CampoParte => ({ campo: 'hasta', etiqueta: 'Hasta (m)', tipo: 'metros', requerido, ancho: 3 });
const CAJAS: CampoParte[] = [
  { campo: 'boxDesde', etiqueta: 'Caja desde', tipo: 'entero', ancho: 2 },
  { campo: 'boxHasta', etiqueta: 'Caja hasta', tipo: 'entero', ancho: 2 },
  { campo: 'totalCajas', etiqueta: 'Total de cajas', tipo: 'entero', ancho: 2 },
  { campo: 'marcadoDesde', etiqueta: 'Marcado desde (m)', tipo: 'metros', ancho: 2 },
  { campo: 'marcadoHasta', etiqueta: 'Marcado hasta (m)', tipo: 'metros', ancho: 2 },
  { campo: 'totalMetraje', etiqueta: 'Metraje (m)', tipo: 'metros', ancho: 2 },
];
const RANGOS_CAJAS: [string, string, string][] = [
  ['boxDesde', 'boxHasta', 'Cajas'],
  ['marcadoDesde', 'marcadoHasta', 'Marcado'],
];
const DERIVA_CAJAS = [
  { campo: 'totalCajas', desde: 'boxDesde', hasta: 'boxHasta', cajas: true },
  { campo: 'totalMetraje', desde: 'marcadoDesde', hasta: 'marcadoHasta' },
];
const TRAMO: [string, string, string][] = [['desde', 'hasta', 'Tramo']];

/** Las listas cerradas son las mismas que exigen los CHECK de la base y el backend. */
export const FORMULARIOS: Record<string, FormularioParte> = {
  recepcion: {
    servicio: RecepcionCajaService,
    campos: [RESPONSABLE, ...CAJAS],
    rangos: RANGOS_CAJAS,
    copia: ['responsable'],
    continua: { boxDesde: 'boxHasta', marcadoDesde: 'marcadoHasta' },
    deriva: DERIVA_CAJAS,
  },
  logueo: {
    servicio: AvanceLogueoService,
    campos: [
      {
        campo: 'tipoLogueo', etiqueta: 'Tipo de logueo', tipo: 'opcion', requerido: true, ancho: 3,
        opciones: [
          { valor: 'geologico', texto: 'Geológico' },
          { valor: 'geotecnico', texto: 'Geotécnico' },
          { valor: 'estructural', texto: 'Estructural' },
          { valor: 'rapido', texto: 'Rápido' },
        ],
      },
      { campo: 'geologo', etiqueta: 'Geólogo', tipo: 'texto', ancho: 6 },
      { campo: 'pctCompletado', etiqueta: '% completado', tipo: 'porcentaje', ancho: 3 },
      DESDE(), HASTA(),
      { campo: 'avance', etiqueta: 'Avance', tipo: 'calculado', ancho: 3 },
    ],
    rangos: TRAMO,
    copia: ['tipoLogueo', 'geologo'],
    continua: { desde: 'hasta' },
    deriva: [{ campo: 'avance', desde: 'desde', hasta: 'hasta' }],
  },
  fotografia: {
    servicio: AvanceFotografiaService,
    campos: [
      RESPONSABLE,
      {
        campo: 'secoHumedo', etiqueta: 'Pasada', tipo: 'opcion', ancho: 3,
        opciones: [{ valor: 'seco', texto: 'Seco' }, { valor: 'humedo', texto: 'Húmedo' }],
      },
      { campo: 'nroFotos', etiqueta: 'N.º de fotos', tipo: 'entero', ancho: 3 },
      ...CAJAS,
      { campo: 'rutaArchivos', etiqueta: 'Carpeta de las fotos', tipo: 'texto', ancho: 12 },
    ],
    rangos: RANGOS_CAJAS,
    copia: ['responsable', 'secoHumedo', 'rutaArchivos'],
    continua: { boxDesde: 'boxHasta', marcadoDesde: 'marcadoHasta' },
    deriva: DERIVA_CAJAS,
  },
  corte: {
    servicio: CorteMuestraService,
    campos: [
      RESPONSABLE, LINEA,
      { campo: 'nroMuestras', etiqueta: 'N.º de muestras', tipo: 'entero', ancho: 3 },
      DESDE(), HASTA(),
      { campo: 'metrajeEstimado', etiqueta: 'Metraje (m)', tipo: 'metros', ancho: 3 },
    ],
    rangos: TRAMO,
    copia: ['responsable', 'tipoLinea'],
    continua: { desde: 'hasta' },
    deriva: [{ campo: 'metrajeEstimado', desde: 'desde', hasta: 'hasta' }],
  },
  muestreo: {
    servicio: AvanceMuestreoService,
    campos: [
      RESPONSABLE,
      {
        campo: 'estadoMuestreo', etiqueta: 'Estado', tipo: 'opcion', requerido: true, ancho: 3,
        opciones: [{ valor: 'en_proceso', texto: 'En proceso' }, { valor: 'finalizado', texto: 'Finalizado' }],
      },
      { campo: 'nroLotes', etiqueta: 'N.º de lotes', tipo: 'entero', ancho: 3 },
      DESDE(false), HASTA(false),
      { campo: 'avance', etiqueta: 'Avance', tipo: 'calculado', ancho: 3 },
      { campo: 'nroMuestras', etiqueta: 'Muestras', tipo: 'entero', ancho: 3 },
      { campo: 'nroMuestrasEmbolsadas', etiqueta: 'Embolsadas', tipo: 'entero', ancho: 3 },
      { campo: 'nroCostales', etiqueta: 'Costales', tipo: 'entero', ancho: 3 },
    ],
    rangos: TRAMO,
    copia: ['responsable', 'estadoMuestreo'],
    continua: { desde: 'hasta' },
    deriva: [{ campo: 'avance', desde: 'desde', hasta: 'hasta' }],
  },
  'corte-densidad': {
    servicio: CorteDensidadService,
    campos: [
      RESPONSABLE, LINEA,
      { campo: 'totalMuestras', etiqueta: 'Muestras', tipo: 'entero', requerido: true, ancho: 3 },
      DESDE(), HASTA(),
      { campo: 'totalMetraje', etiqueta: 'Metraje (m)', tipo: 'metros', ancho: 3 },
    ],
    rangos: TRAMO,
    copia: ['responsable', 'tipoLinea'],
    continua: { desde: 'hasta' },
    deriva: [{ campo: 'totalMetraje', desde: 'desde', hasta: 'hasta' }],
  },
  densidad: {
    servicio: AvanceDensidadService,
    campos: [
      RESPONSABLE,
      {
        campo: 'etapa', etiqueta: 'Etapa', tipo: 'opcion', requerido: true, ancho: 3,
        opciones: [
          { valor: 'corte', texto: 'Corte' },
          { valor: 'secado_1', texto: 'Secado 1' },
          { valor: 'secado_2', texto: 'Secado 2' },
          { valor: 'pesado', texto: 'Pesado' },
          { valor: 'calculo', texto: 'Cálculo' },
        ],
      },
      { campo: 'nroMuestras', etiqueta: 'Muestras', tipo: 'entero', requerido: true, ancho: 3 },
      DESDE(false), HASTA(false),
      { campo: 'temperaturaC', etiqueta: 'Temperatura (°C)', tipo: 'decimal', ancho: 3 },
      LINEA,
    ],
    rangos: TRAMO,
    copia: ['responsable', 'tipoLinea'],
    continua: {},
    deriva: [],
  },
};
