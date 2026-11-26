import { HttpErrorResponse } from '@angular/common/http';

import { mensajeDeError, nombreCortoSondaje } from './interfaz';

describe('mensajeDeError', () => {
  const error = (status: number, cuerpo: unknown) => new HttpErrorResponse({ status, error: cuerpo });

  it('usa el mensaje del servidor', () => {
    expect(mensajeDeError(error(400, { message: 'El RUC debe tener 11 dígitos.' }), 'No se pudo guardar'))
      .toBe('No se pudo guardar: El RUC debe tener 11 dígitos.');
  });

  it('también lee la clave «mensaje» que usan las fotos y la importación', () => {
    expect(mensajeDeError(error(400, { mensaje: 'La imagen supera los 20 MB.' }), 'No se pudo subir'))
      .toBe('No se pudo subir: La imagen supera los 20 MB.');
  });

  it('quita los nombres de campo del texto de @Valid y deja la frase en minúscula', () => {
    const cuerpo = { errores: 'codigo: El código es obligatorio; nombres: Los nombres son obligatorios' };
    expect(mensajeDeError(error(400, cuerpo), 'No se pudo guardar'))
      .toBe('No se pudo guardar: El código es obligatorio; los nombres son obligatorios');
  });

  it('une los mensajes cuando los errores llegan como mapa', () => {
    const cuerpo = { errores: { codigo: 'El código es obligatorio', nombres: 'Los nombres son obligatorios' } };
    expect(mensajeDeError(error(400, cuerpo), 'X')).toBe('X: El código es obligatorio; los nombres son obligatorios');
  });

  it('explica el 403 y la falta de conexión', () => {
    expect(mensajeDeError(error(403, null), 'X')).toBe('X: No tienes permiso para esta operación.');
    expect(mensajeDeError(error(0, null), 'X')).toBe('X: No hay conexión con el servidor.');
  });

  it('deja solo el contexto si no hay detalle', () => {
    expect(mensajeDeError(new Error('raro'), 'No se pudo cargar')).toBe('No se pudo cargar');
  });
});

describe('nombreCortoSondaje', () => {
  it('acorta el código canónico y deja igual el resto', () => {
    expect(nombreCortoSondaje('DDH-COND-N-007-26')).toBe('DDH-007');
    expect(nombreCortoSondaje('SONDAJE-RARO')).toBe('SONDAJE-RARO');
    expect(nombreCortoSondaje(null)).toBe('—');
  });
});
