import { Injectable, inject, signal } from '@angular/core';
import { Observable, Subject, filter, share } from 'rxjs';

import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

/** Aviso de que cambiaron datos de uno o más sondajes. Espejo de `CambioDatos`. */
export interface CambioDatos {
  /** `quicklog`, `litologia`, `avance`, `perforacion`… */
  recurso: string;
  /** Sondajes afectados. Vacío = cambio general (una importación): recargar todo. */
  sondajes: string[];
  /** `tablet`, `web` o `importacion`. */
  origen: string;
  filas: number;
  momento: string;
}

export type EstadoCanal = 'conectado' | 'reconectando' | 'desconectado';

/**
 * Canal de avisos en tiempo real (`GET /api/eventos`, Server-Sent Events).
 *
 * Se abre con `fetch` y no con `EventSource` porque `EventSource` no deja poner
 * la cabecera `Authorization`, y la alternativa —el token en la URL— lo deja
 * escrito en el registro de cualquier proxy.
 *
 * Una sola conexión para toda la aplicación, compartida por todas las
 * pantallas: se abre cuando la primera se suscribe y se cierra cuando se va la
 * última. Si se cae, reintenta con espera creciente hasta 30 s; cuando vuelve,
 * emite un aviso general para que cada pantalla recargue lo que se perdió
 * mientras estuvo desconectada.
 */
@Injectable({ providedIn: 'root' })
export class EventosService {

  private readonly auth = inject(AuthService);
  private readonly url = `${environment.apiBase}/eventos`;

  readonly estado = signal<EstadoCanal>('desconectado');

  private readonly flujo$: Observable<CambioDatos> = new Observable<CambioDatos>(obs => {
    const corte = new AbortController();
    let espera = 1000;
    let vivo = true;
    let conectadoAntes = false;

    const conectar = async (): Promise<void> => {
      while (vivo) {
        const token = this.auth.getToken();
        if (!token) {
          this.estado.set('desconectado');
          return;
        }
        try {
          const resp = await fetch(this.url, {
            headers: { Authorization: `Bearer ${token}`, Accept: 'text/event-stream' },
            signal: corte.signal,
          });
          if (resp.status === 401 || resp.status === 403) {
            // Token vencido: reintentar no lo arregla. El interceptor y los
            // guards se encargan de mandar al login.
            this.estado.set('desconectado');
            return;
          }
          if (!resp.ok || !resp.body) {
            throw new Error('HTTP ' + resp.status);
          }
          this.estado.set('conectado');
          espera = 1000;
          if (conectadoAntes) {
            // Pudo perderse algo durante el corte: que cada pantalla recargue.
            obs.next({ recurso: '*', sondajes: [], origen: 'reconexion', filas: 0, momento: new Date().toISOString() });
          }
          conectadoAntes = true;
          await this.leer(resp.body, obs);
        } catch (e) {
          if (!vivo || corte.signal.aborted) {
            return;
          }
        }
        this.estado.set('reconectando');
        await new Promise(r => setTimeout(r, espera));
        espera = Math.min(espera * 2, 30000);
      }
    };

    conectar();
    return () => {
      vivo = false;
      corte.abort();
      this.estado.set('desconectado');
    };
  }).pipe(share());

  /** Todos los avisos. */
  cambios(): Observable<CambioDatos> {
    return this.flujo$;
  }

  /**
   * Avisos que tocan un recurso y, opcionalmente, un sondaje. Un aviso sin
   * sondajes o de reconexión siempre pasa: puede afectar a cualquiera.
   */
  cambiosDe(recursos: string[], sondajeId?: () => string | null): Observable<CambioDatos> {
    return this.flujo$.pipe(filter(c => {
      if (c.recurso !== '*' && !recursos.includes(c.recurso)) {
        return false;
      }
      const id = sondajeId?.();
      return !id || c.sondajes.length === 0 || c.sondajes.includes(id);
    }));
  }

  /** Lee el flujo SSE y emite cada evento `cambio`. */
  private async leer(cuerpo: ReadableStream<Uint8Array>, obs: Subject<CambioDatos> | { next: (c: CambioDatos) => void }): Promise<void> {
    const lector = cuerpo.getReader();
    const texto = new TextDecoder();
    let buffer = '';
    for (;;) {
      const { value, done } = await lector.read();
      if (done) {
        return;
      }
      buffer += texto.decode(value, { stream: true });
      // Los eventos se separan con una línea en blanco.
      let fin: number;
      while ((fin = buffer.search(/\r?\n\r?\n/)) >= 0) {
        const bloque = buffer.slice(0, fin);
        buffer = buffer.slice(fin).replace(/^\r?\n\r?\n/, '');
        let nombre = 'message';
        const datos: string[] = [];
        for (const linea of bloque.split(/\r?\n/)) {
          if (linea.startsWith(':')) { continue; }            // latido
          if (linea.startsWith('event:')) { nombre = linea.slice(6).trim(); }
          if (linea.startsWith('data:')) { datos.push(linea.slice(5).trim()); }
        }
        if (nombre === 'cambio' && datos.length) {
          try {
            obs.next(JSON.parse(datos.join('\n')) as CambioDatos);
          } catch {
            // Un evento malformado no debe cortar el canal.
          }
        }
      }
    }
  }
}
