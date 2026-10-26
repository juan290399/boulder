import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { forkJoin } from 'rxjs';

import { CatalogoGeologia } from '../../../services/logueo.service';
import { DiccionarioService, EstructuraCatalogo } from '../../../services/diccionario.service';

/** Nombre legible de cada catálogo y el bloque del menú donde va. */
const NOMBRES: Record<string, [string, string]> = {
  lit_diccionario: ['Litologías', 'Litología'],
  grupo_litologico: ['Grupos litológicos', 'Litología'],
  lit_textura: ['Texturas', 'Litología'],
  tamanio_grano: ['Tamaño de grano', 'Litología'],
  forma_clasto: ['Forma de clasto', 'Litología'],
  tipo_material: ['Tipo de material', 'Litología'],
  color: ['Colores', 'Litología'],
  intensidad_color: ['Intensidad de color', 'Litología'],
  alt_diccionario: ['Alteraciones', 'Alteración'],
  alt_mineral: ['Minerales de alteración', 'Alteración'],
  alt_intensidad: ['Intensidad de alteración', 'Alteración'],
  alt_estilo: ['Estilos de alteración', 'Alteración'],
  min_diccionario: ['Minerales de mena', 'Mineralización'],
  min_distribucion: ['Distribución', 'Mineralización'],
  min_zona: ['Zonas', 'Mineralización'],
  est_diccionario: ['Estructuras', 'Estructural'],
  est_tipo_venilla: ['Tipos de venilla', 'Estructural'],
  est_espesor: ['Espesor', 'Estructural'],
  est_forma: ['Forma', 'Estructural'],
  est_relleno: ['Relleno', 'Estructural'],
  est_rugosidad: ['Rugosidad', 'Estructural'],
};

interface Formulario {
  id?: string;
  codigo: string;
  nombre: string;
  grupoId: string | null;
  color: string;
  conColor: boolean;
}

const VACIO = (): Formulario => ({ codigo: '', nombre: '', grupoId: null, color: '#a0a0a0', conColor: false });

/**
 * Diccionarios geológicos (P15, RF04): los códigos que el quicklog y el logueo
 * ofrecen en sus combos. Solo para ADMIN.
 *
 * El formulario se arma con la estructura que publica el backend: el largo del
 * código y si el catálogo lleva grupo, color o estado. No se repite ese
 * conocimiento aquí, así un catálogo nuevo en la base aparece solo.
 */
@Component({
  selector: 'app-admin-diccionarios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './diccionarios.html',
  styleUrl: './diccionarios.scss',
})
export class Diccionarios implements OnInit {

  private readonly servicio = inject(DiccionarioService);

  readonly todos = signal<CatalogoGeologia[]>([]);
  readonly estructura = signal<Record<string, EstructuraCatalogo>>({});
  readonly tipo = signal('lit_diccionario');
  readonly busqueda = signal('');
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly aviso = signal<string | null>(null);
  readonly form = signal<Formulario>(VACIO());

  /** Catálogos agrupados para el selector, en el orden de NOMBRES. */
  readonly grupos = computed(() => {
    const tipos = Object.keys(this.estructura());
    const orden = Object.keys(NOMBRES);
    const porGrupo = new Map<string, string[]>();
    [...tipos].sort((a, b) => (orden.indexOf(a) + 1 || 99) - (orden.indexOf(b) + 1 || 99))
      .forEach(t => {
        const g = NOMBRES[t]?.[1] ?? 'Otros';
        porGrupo.set(g, [...(porGrupo.get(g) ?? []), t]);
      });
    return [...porGrupo.entries()].map(([titulo, items]) => ({ titulo, items }));
  });

  readonly forma = computed(() => this.estructura()[this.tipo()] ?? { columnas: [], largoCodigo: 5 });
  readonly tieneGrupo = computed(() => this.forma().columnas.includes('grupo_id'));
  readonly tieneColor = computed(() => this.forma().columnas.includes('color_r'));
  readonly tieneEstado = computed(() => this.forma().columnas.includes('esta_activo'));

  readonly gruposLitologicos = computed(() => this.todos().filter(c => c.tipo === 'grupo_litologico'));

  readonly filas = computed(() => {
    const q = this.busqueda().trim().toLowerCase();
    return this.todos()
      .filter(c => c.tipo === this.tipo())
      .filter(c => !q || c.codigo.toLowerCase().includes(q) || c.nombre.toLowerCase().includes(q));
  });

  nombre(tipo: string): string {
    return NOMBRES[tipo]?.[0] ?? tipo;
  }

  nombreGrupo(id?: string | null): string {
    return this.gruposLitologicos().find(g => g.id === id)?.nombre ?? '—';
  }

  rgb(c: CatalogoGeologia): string | null {
    return c.colorR == null ? null : `rgb(${c.colorR}, ${c.colorG}, ${c.colorB})`;
  }

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    forkJoin({ lista: this.servicio.listar(), estructura: this.servicio.estructura() }).subscribe({
      next: ({ lista, estructura }) => {
        this.todos.set(lista);
        this.estructura.set(estructura);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar los diccionarios.');
        this.cargando.set(false);
      },
    });
  }

  elegirTipo(t: string): void {
    this.tipo.set(t);
    this.nuevo();
  }

  nuevo(): void {
    this.form.set(VACIO());
    this.error.set(null);
  }

  editar(c: CatalogoGeologia): void {
    this.error.set(null);
    this.form.set({
      id: c.id,
      codigo: c.codigo,
      nombre: c.nombre,
      grupoId: c.grupoId ?? null,
      conColor: c.colorR != null,
      color: c.colorR != null ? this.hex(c.colorR, c.colorG!, c.colorB!) : '#a0a0a0',
    });
  }

  actualizarForm(cambio: Partial<Formulario>): void {
    this.form.update(f => ({ ...f, ...cambio }));
  }

  guardar(): void {
    const f = this.form();
    const datos: Partial<CatalogoGeologia> = { codigo: f.codigo, nombre: f.nombre };
    if (this.tieneGrupo()) {
      datos.grupoId = f.grupoId;
    }
    if (this.tieneColor() && f.conColor) {
      const [r, g, b] = this.canales(f.color);
      Object.assign(datos, { colorR: r, colorG: g, colorB: b });
    }
    const t = this.tipo();
    const peticion = f.id ? this.servicio.actualizar(t, f.id, datos) : this.servicio.crear(t, datos);
    peticion.subscribe({
      next: () => {
        this.aviso.set(f.id ? `Código ${f.codigo.toUpperCase()} actualizado.` : `Código ${f.codigo.toUpperCase()} agregado.`);
        this.nuevo();
        this.cargar();
      },
      error: (e: HttpErrorResponse) => this.error.set(this.motivo(e)),
    });
  }

  alternarEstado(c: CatalogoGeologia): void {
    const activar = !c.estaActivo;
    this.servicio.cambiarEstado(this.tipo(), c.id, activar).subscribe({
      next: () => {
        this.aviso.set(`Código ${c.codigo} ${activar ? 'activado' : 'desactivado'}.`);
        this.cargar();
      },
      error: (e: HttpErrorResponse) => this.error.set(this.motivo(e)),
    });
  }

  private motivo(e: HttpErrorResponse): string {
    if (e.status === 409) {
      return 'Ese código ya existe en este catálogo.';
    }
    if (e.status === 403) {
      return 'Solo el administrador puede modificar los diccionarios.';
    }
    return e.error?.message ?? 'No se pudo guardar.';
  }

  private hex(r: number, g: number, b: number): string {
    return '#' + [r, g, b].map(v => v.toString(16).padStart(2, '0')).join('');
  }

  private canales(hex: string): [number, number, number] {
    const n = parseInt(hex.slice(1), 16);
    return [(n >> 16) & 255, (n >> 8) & 255, n & 255];
  }
}
