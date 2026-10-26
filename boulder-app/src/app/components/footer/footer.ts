import { Component } from '@angular/core';

/**
 * Pie de la zona operativa.
 *
 * Reemplaza al marcador «footer works!» que dejó el generador de Angular y
 * que seguía a la vista en todas las pantallas.
 */
@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [],
  templateUrl: './footer.html',
  styleUrl: './footer.scss',
})
export class Footer {
  readonly anio = new Date().getFullYear();
}
