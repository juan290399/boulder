import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';

/** Un tramo listo para dibujar: nombres y colores ya resueltos con el diccionario. */
export interface TramoStrip {
  id: string;
  desde: number;
  hasta: number;
  litoCodigo: string;
  litoNombre: string;
  litoColor: string | null;
  altCodigo: string | null;
  altNombre: string | null;
  intensidadCodigo: string | null;
  intensidadNombre: string | null;
  zonaCodigo: string | null;
  zonaNombre: string | null;
  pcCu: number | null;
}

/** Una foto con su tramo, para la columna de fotos. */
export interface FotoStrip {
  id: string;
  desde: number;
  hasta: number;
  etiqueta: string;
}

/** Escalas del log en papel: 1 m del sondaje ocupa 1000/escala mm de pantalla. */
export const ESCALAS = [
  { valor: 200, rotulo: '1:200' },
  { valor: 500, rotulo: '1:500' },
  { valor: 1000, rotulo: '1:1000' },
];

/** 96 px por pulgada → px por metro a una escala dada. */
const pxPorMetro = (escala: number) => (1000 / escala) * (96 / 25.4);

/** Colores de alteración (no vienen en el diccionario). */
const COLOR_ALTERACION: Record<string, string> = {
  POTA: '#e79fb4', FILI: '#e6d17f', ARGI: '#efe6d2', PROP: '#8fbf7f', CLTZ: '#5f9e6e', EPZA: '#9cc06a',
  SILC: '#b5b5c8', SKPR: '#a0705a', SKRE: '#c49a6c', OXCI: '#d9822b', SUPG: '#e3a857', CARB: '#d4d4ef',
  ALBI: '#e3e3e3', INAL: '#f4f4f4',
};
/** Ancho de la barra de alteración según intensidad: más intensa, más ancha. */
const ANCHO_INTENSIDAD: Record<string, number> = {
  MDE: 0.2, DEB: 0.3, DMD: 0.4, MDD: 0.45, MOD: 0.55, MDF: 0.65, FUE: 0.75, INS: 0.9, MIN: 1,
};
const COLOR_ZONA: Record<string, string> = {
  OXID: '#d9822b', SFSC: '#3b6fb6', SFPR: '#6b4c9a', MIXT: '#9a8f3b', NMIN: '#cfd4da',
};

/** Columnas del log, en px. */
const COL = { regla: 46, lito: 132, alt: 92, zona: 34, cu: 104, fotos: 46 };
const SEP = 6;
const ARRIBA = 8;
const CU_MAX = 1.5;

/**
 * Strip log del quicklog, continuo y a escala (como el de Boulder GeoLog).
 *
 * La escala es fija en metros por milímetro, no «todo el sondaje en la
 * pantalla»: un sondaje de 600 m a 1:500 mide 4,5 m de pantalla y se recorre
 * desplazando, igual que el rollo de papel. Así un tramo de medio metro (una
 * zona de falla) sigue viéndose. Los huecos sin describir se rayan.
 *
 * Columnas: profundidad, litología (color del diccionario), alteración (el
 * ancho de la barra es la intensidad), zona mineral, % Cu y fotos de cajas.
 */
@Component({
  selector: 'app-strip-log',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './strip-log.html',
  styleUrl: './strip-log.scss',
})
export class StripLog {
  readonly tramos = input<TramoStrip[]>([]);
  readonly fotos = input<FotoStrip[]>([]);
  readonly escala = input(500);
  readonly seleccionadoId = input<string | null>(null);
  readonly seleccionar = output<TramoStrip>();
  readonly abrirFoto = output<string>();

  readonly col = COL;
  readonly arriba = ARRIBA;

  readonly x = {
    regla: 0,
    lito: COL.regla,
    alt: COL.regla + COL.lito + SEP,
    zona: COL.regla + COL.lito + COL.alt + SEP * 2,
    cu: COL.regla + COL.lito + COL.alt + COL.zona + SEP * 3,
    fotos: COL.regla + COL.lito + COL.alt + COL.zona + COL.cu + SEP * 4,
  };
  readonly ancho = this.x.fotos + COL.fotos;

  readonly pxm = computed(() => pxPorMetro(this.escala()));
  readonly fondo = computed(() => Math.max(0, ...this.tramos().map(t => t.hasta), ...this.fotos().map(f => f.hasta)));
  readonly alto = computed(() => ARRIBA + this.fondo() * this.pxm() + 24);

  /** Marcas de la regla: cada 5, 10 o 50 m según la escala. */
  readonly marcas = computed(() => {
    const paso = this.escala() <= 200 ? 5 : this.escala() <= 500 ? 10 : 50;
    const menor = paso / 5;
    const salida: { m: number; y: number; mayor: boolean }[] = [];
    for (let m = 0; m <= this.fondo() + 0.001; m += menor) {
      const redondo = Math.round(m * 1000) / 1000;
      salida.push({ m: redondo, y: this.y(redondo), mayor: Math.abs(redondo % paso) < 0.001 });
    }
    return salida;
  });

  /** Huecos sin describir entre la boca y el último tramo. */
  readonly huecos = computed(() => {
    const orden = [...this.tramos()].sort((a, b) => a.desde - b.desde);
    const salida: { desde: number; hasta: number }[] = [];
    let fin = 0;
    for (const t of orden) {
      if (t.desde - fin > 0.05) { salida.push({ desde: fin, hasta: t.desde }); }
      fin = Math.max(fin, t.hasta);
    }
    return salida;
  });

  /** Fotos agrupadas por tramo (seco y húmedo de las mismas cajas van juntas). */
  readonly fotosPorTramo = computed(() => {
    const mapa = new Map<string, FotoStrip[]>();
    for (const f of this.fotos()) {
      const k = `${f.desde}-${f.hasta}`;
      mapa.set(k, [...(mapa.get(k) ?? []), f]);
    }
    return [...mapa.values()];
  });

  y(metro: number): number {
    return ARRIBA + metro * this.pxm();
  }

  /** Alto dibujado de un tramo; nunca menos de 2 px para que no desaparezca. */
  alto_(t: { desde: number; hasta: number }): number {
    return Math.max(2, (t.hasta - t.desde) * this.pxm());
  }

  colorAlt(t: TramoStrip): string {
    return (t.altCodigo && COLOR_ALTERACION[t.altCodigo]) || '#e9ecef';
  }

  anchoAlt(t: TramoStrip): number {
    return COL.alt * (ANCHO_INTENSIDAD[t.intensidadCodigo ?? ''] ?? 0.4);
  }

  colorZona(t: TramoStrip): string {
    return (t.zonaCodigo && COLOR_ZONA[t.zonaCodigo]) || '#e9ecef';
  }

  anchoCu(t: TramoStrip): number {
    return COL.cu * Math.min(1, (t.pcCu ?? 0) / CU_MAX);
  }

  /** Texto oscuro o claro según el color de fondo. */
  tinta(fondo: string | null): string {
    const m = fondo?.match(/\d+/g);
    if (!m) { return 'var(--bo-ink)'; }
    const [r, g, b] = m.map(Number);
    return 0.299 * r + 0.587 * g + 0.114 * b > 150 ? '#0f172a' : '#ffffff';
  }

  descripcion(t: TramoStrip): string {
    return `${t.desde.toFixed(2)} a ${t.hasta.toFixed(2)} m: ${t.litoNombre}`
      + (t.altNombre ? `, alteración ${t.altNombre.toLowerCase()}${t.intensidadNombre ? ' ' + t.intensidadNombre.toLowerCase() : ''}` : '')
      + (t.pcCu !== null ? `, ${t.pcCu}% Cu` : '');
  }

  alTeclear(evento: KeyboardEvent, t: TramoStrip): void {
    if (evento.key === 'Enter' || evento.key === ' ') {
      evento.preventDefault();
      this.seleccionar.emit(t);
    }
  }
}
