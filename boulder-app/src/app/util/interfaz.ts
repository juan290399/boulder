import { HttpErrorResponse } from '@angular/common/http';

/**
 * Texto para el usuario a partir de un error HTTP del backend.
 *
 * GlobalExceptionHandler responde `{ message, errores? }` (las fotos y la
 * importación responden `{ mensaje }`), donde `errores` llega
 * como mapa campo -> mensaje o como texto «campo: mensaje; ...». Al usuario le
 * sirve el mensaje, no el nombre del campo en Java, así que se quita.
 *
 * @param err      lo que entregó el `subscribe`
 * @param contexto qué se intentaba hacer, ej. «No se pudo guardar la empresa»
 */
export function mensajeDeError(err: unknown, contexto: string): string {
  let detalle = '';
  if (err instanceof HttpErrorResponse) {
    if (err.status === 403) {
      detalle = 'No tienes permiso para esta operación.';
    } else if (err.status === 0) {
      detalle = 'No hay conexión con el servidor.';
    } else {
      const errores = err.error?.errores;
      detalle = errores && typeof errores === 'object'
        ? Object.values(errores as Record<string, string>).join('; ')
        : String(errores ?? err.error?.message ?? err.error?.mensaje ?? '');
      detalle = detalle
        .replace(/(^|; )\w+: /g, '$1')
        .replace(/; (\p{Lu})/gu, (_m: string, c: string) => '; ' + c.toLowerCase());
    }
  }
  return detalle ? `${contexto}: ${detalle}` : contexto;
}

/**
 * Nombre corto de un sondaje para tablas angostas: «DDH-COND-N-007-26» → «DDH-007».
 * Si el código no tiene la forma canónica, se devuelve tal cual.
 */
export function nombreCortoSondaje(codigo: string | null | undefined): string {
  if (!codigo) { return '—'; }
  const m = codigo.match(/^DDH-[A-Z]+-[A-Z]-(\d{3})-\d{2}$/);
  return m ? `DDH-${m[1]}` : codigo;
}

/**
 * Lleva la vista a una sección (normalmente el formulario de edición) y el foco
 * a su primer campo. Respeta «reducir movimiento» del sistema operativo.
 */
export function irA(idSeccion: string, idFoco?: string): void {
  const reducir = typeof matchMedia === 'function' && matchMedia('(prefers-reduced-motion: reduce)').matches;
  document.getElementById(idSeccion)?.scrollIntoView({ behavior: reducir ? 'auto' : 'smooth', block: 'start' });
  if (idFoco) {
    document.getElementById(idFoco)?.focus({ preventScroll: true });
  }
}
