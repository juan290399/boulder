import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/**
 * Ícono de Boulder: un sondaje inclinado que cruza tres estratos, con el collar
 * en superficie. Se dibuja con `currentColor`, así que toma el color del texto
 * del contenedor (blanco sobre el recuadro azul de la marca).
 */
@Component({
  selector: 'app-marca',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'marca', 'aria-hidden': 'true' },
  template: `
    <svg [attr.width]="tamanio()" [attr.height]="tamanio()" viewBox="0 0 24 24" fill="none"
         stroke="currentColor" stroke-linecap="round" stroke-linejoin="round">
      <path d="M2 8c4-1.5 7 1 10 0s6-1.5 10 0M2 13c4-1.5 7 1 10 0s6-1.5 10 0M2 18c4-1.5 7 1 10 0s6-1.5 10 0"
            stroke-width="1.6" opacity=".55" />
      <path d="M8 4l7 17" stroke-width="2.2" />
      <circle cx="8" cy="4" r="1.6" fill="currentColor" stroke="none" />
    </svg>
  `,
  styles: `:host { display: inline-flex; line-height: 0; }`,
})
export class Marca {
  /** Lado del ícono en píxeles. */
  readonly tamanio = input(26);
}
