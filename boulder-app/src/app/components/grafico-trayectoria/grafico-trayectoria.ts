import { CommonModule } from '@angular/common';
import { Component, computed, input } from '@angular/core';

/** Un punto medido: profundidad contra el valor del ángulo. */
export interface PuntoTrayectoria {
  profundidad: number;
  valor: number;
}

interface Marca {
  pos: number;
  etiqueta: string;
}

/** Geometría ya resuelta que la plantilla solo tiene que pintar. */
interface Trazo {
  ancho: number;
  alto: number;
  margenIzq: number;
  margenSup: number;
  areaAncho: number;
  areaAlto: number;
  lineaMedida: string;
  areaDesvio: string;
  yPlan: number;
  marcasX: Marca[];
  marcasY: Marca[];
  hayDatos: boolean;
}

/**
 * Gráfica de trayectoria: lo planificado contra lo medido a lo largo de la
 * profundidad.
 *
 * Reemplaza a las dos gráficas de Excel que hoy se comparten como imagen. El
 * plan es una recta horizontal —el sondaje se planifica con un ángulo de
 * partida— y lo medido es la curva; el área sombreada entre ambas es el
 * desvío, que es lo que se mira.
 *
 * Va en SVG a mano y no con una librería de gráficos a propósito: son dos
 * series y un eje, el paquete ya excede el presupuesto de bundle en 355 kB, y
 * así la gráfica usa los mismos colores del sistema de diseño que el resto de
 * la pantalla.
 */
@Component({
  selector: 'app-grafico-trayectoria',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './grafico-trayectoria.html',
  styleUrl: './grafico-trayectoria.scss',
})
export class GraficoTrayectoria {

  readonly titulo = input.required<string>();
  readonly unidad = input<string>('°');
  readonly plan = input<number | null | undefined>(null);
  readonly puntos = input.required<PuntoTrayectoria[]>();

  /** Color del trazo medido; por defecto el azul de marca. */
  readonly color = input<string>('var(--bo-brand-500)');

  readonly trazo = computed<Trazo>(() => {
    const ancho = 720;
    const alto = 240;
    const margenIzq = 46;
    const margenDer = 12;
    const margenSup = 12;
    const margenInf = 26;
    const areaAncho = ancho - margenIzq - margenDer;
    const areaAlto = alto - margenSup - margenInf;

    const base: Trazo = {
      ancho, alto, margenIzq, margenSup, areaAncho, areaAlto,
      lineaMedida: '', areaDesvio: '', yPlan: margenSup + areaAlto / 2,
      marcasX: [], marcasY: [], hayDatos: false,
    };

    const puntos = this.puntos();
    if (!puntos?.length) {
      return base;
    }

    const plan = this.plan();
    const profundidades = puntos.map(p => p.profundidad);
    const valores = puntos.map(p => p.valor);

    const xMin = 0;
    const xMax = Math.max(...profundidades) || 1;

    // El plan entra en el rango aunque quede fuera de lo medido: si no, la
    // recta planificada se dibujaría pegada al borde o directamente afuera.
    let yMin = Math.min(...valores);
    let yMax = Math.max(...valores);
    if (plan !== null && plan !== undefined) {
      yMin = Math.min(yMin, plan);
      yMax = Math.max(yMax, plan);
    }

    // Un margen del diez por ciento, y un mínimo de un grado para que una
    // serie casi plana no se dibuje como una línea temblorosa amplificada.
    const rango = Math.max(yMax - yMin, 1);
    const holgura = rango * 0.1;
    yMin -= holgura;
    yMax += holgura;

    const ex = (v: number) => margenIzq + ((v - xMin) / (xMax - xMin)) * areaAncho;
    const ey = (v: number) => margenSup + areaAlto - ((v - yMin) / (yMax - yMin)) * areaAlto;

    const lineaMedida = puntos
      .map((p, i) => `${i === 0 ? 'M' : 'L'}${ex(p.profundidad).toFixed(1)},${ey(p.valor).toFixed(1)}`)
      .join(' ');

    let areaDesvio = '';
    let yPlan = base.yPlan;
    if (plan !== null && plan !== undefined) {
      yPlan = ey(plan);
      // El área se cierra volviendo por la recta del plan, así que representa
      // exactamente la separación acumulada entre lo medido y lo planificado.
      areaDesvio = `${lineaMedida} L${ex(puntos[puntos.length - 1].profundidad).toFixed(1)},${yPlan.toFixed(1)}`
                 + ` L${ex(puntos[0].profundidad).toFixed(1)},${yPlan.toFixed(1)} Z`;
    }

    const marcasX: Marca[] = [];
    for (let i = 0; i <= 5; i++) {
      const v = xMin + ((xMax - xMin) * i) / 5;
      marcasX.push({ pos: ex(v), etiqueta: v.toFixed(0) });
    }

    const marcasY: Marca[] = [];
    for (let i = 0; i <= 4; i++) {
      const v = yMin + ((yMax - yMin) * i) / 4;
      marcasY.push({ pos: ey(v), etiqueta: v.toFixed(1) });
    }

    return { ...base, lineaMedida, areaDesvio, yPlan, marcasX, marcasY, hayDatos: true };
  });
}
