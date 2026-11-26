import { Marca } from '../marca/marca';
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

/** Una entrada del menú lateral. */
interface ItemMenu {
  etiqueta: string;
  ruta: string;
  icono: string;
}

/** Un bloque del menú, con su encabezado. */
interface GrupoMenu {
  titulo: string;
  items: ItemMenu[];
}

/**
 * Barra lateral del área de operaciones.
 *
 * El menú vive acá como dato y no repartido por la plantilla. Con ocho
 * entradas repartidas en cuatro bloques, tenerlo declarado en un sitio evita
 * que el orden visible y el orden pensado se separen.
 *
 * Empresas y Proyectos no aparecen: son datos maestros del esquema `base` y
 * se administran en /admin.
 */
@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, Marca],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss',
})
export class Navbar {

  readonly grupos: GrupoMenu[] = [
    {
      titulo: 'Operación',
      items: [
        { etiqueta: 'Avances operativos', ruta: '/avances',   icono: 'bi-clipboard-data' },
        { etiqueta: 'Sondajes',           ruta: '/sondajes',  icono: 'bi-record-circle' },
        { etiqueta: 'Desvíos',            ruta: '/desvios',   icono: 'bi-compass' },
      ],
    },
    {
      titulo: 'Geología',
      items: [
        { etiqueta: 'Quicklog', ruta: '/quicklog', icono: 'bi-journal-richtext' },
        { etiqueta: 'Fotos de testigo', ruta: '/fotos', icono: 'bi-camera' },
      ],
    },
    {
      // «Cargar datos» (importación de plantillas Excel) queda fuera del menú
      // en esta etapa: la evaluación trabaja con una base de demostración. La
      // pantalla sigue en pages/main-layout/importacion para volver a usarla.
      titulo: 'Reportes',
      items: [
        { etiqueta: 'Reporte diario', ruta: '/reportes', icono: 'bi-file-earmark-bar-graph' },
        { etiqueta: 'Reporte de periodo', ruta: '/reportes/periodo', icono: 'bi-calendar-range' },
      ],
    },
    {
      titulo: 'Recursos',
      items: [
        { etiqueta: 'Plataformas', ruta: '/plataformas', icono: 'bi-geo-alt' },
        { etiqueta: 'Máquinas',    ruta: '/maquinas',    icono: 'bi-tools' },
      ],
    },
  ];
}
