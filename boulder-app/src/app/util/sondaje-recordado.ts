/**
 * Último sondaje mirado en este navegador, compartido por Quicklog y Fotos de
 * testigo: quien revisa el quicklog de un sondaje suele pasar a sus fotos.
 * Es una comodidad de quien usa el equipo, no un dato del sistema; si el
 * navegador no permite guardar, simplemente no se recuerda.
 */
const CLAVE = 'boulder.sondaje';

export function leerSondajeRecordado(): string | null {
  try {
    return localStorage.getItem(CLAVE);
  } catch {
    return null;
  }
}

export function guardarSondajeRecordado(codigo: string): void {
  try {
    if (codigo) {
      localStorage.setItem(CLAVE, codigo);
    } else {
      localStorage.removeItem(CLAVE);
    }
  } catch {
    /* sin almacenamiento: no pasa nada */
  }
}
