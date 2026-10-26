/**
 * Modelos del importador de plantillas.
 * Espejo de `com.boulder.boulder.dto.importacion`.
 */

/** Una plantilla que el backend sabe leer. */
export interface Plantilla {
  clave: string;
  archivo: string;
  descripcion: string;
}

/** Un renglón que no se pudo cargar, con el motivo. */
export interface ErrorFila {
  /** Número de fila tal como lo muestra Excel. */
  fila: number;
  referencia: string;
  motivo: string;
}

/** Lo que pasó con una hoja del libro. */
export interface ResultadoHoja {
  hoja: string;
  destino: string;
  leidas: number;
  aceptadas: number;
  omitidas: number;
  rechazadas: number;
  errores: ErrorFila[];
  advertencia?: string;
}

/** El informe completo de una corrida. */
export interface ResultadoImportacion {
  plantilla: string;
  archivo: string;
  /** `false` cuando fue una simulación: se validó y no se escribió nada. */
  aplicado: boolean;
  momento: string;
  totalLeidas: number;
  totalAceptadas: number;
  totalOmitidas: number;
  totalRechazadas: number;
  hojas: ResultadoHoja[];
}
