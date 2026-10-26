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
  imports: [CommonModule, RouterLink, RouterLinkActive],
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
      // La carga vive aparte y no dentro de Operación: no es una pantalla de
      // consulta más, es por donde entra el dato. Mientras no haya formularios
      // de captura, es la única puerta que tiene la plataforma.
      titulo: 'Datos',
      items: [
        { etiqueta: 'Cargar datos', ruta: '/importar', icono: 'bi-upload' },
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
