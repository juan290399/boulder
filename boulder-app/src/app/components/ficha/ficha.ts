import {
  ChangeDetectionStrategy, Component, ElementRef, effect, input, output, untracked, viewChild,
} from '@angular/core';

let siguienteId = 0;

/**
 * Ficha del registro elegido en una pantalla maestro-detalle: la lista queda a
 * la izquierda y esta ficha a la derecha, siempre a la vista y sin tapar nada.
 *
 * - Cerrada muestra el contenido `[vacio]` (una invitación a elegir o crear).
 * - Abierta muestra el título, los campos (`<ng-content />`) y los botones del
 *   pie (`[pie]`), que quedan fijos aunque el formulario sea largo.
 * - Al abrirla, o al pasar a otro registro (`clave`), lleva el foco al primer
 *   campo; al cerrarla lo devuelve a la fila o botón que la abrió.
 * - Esc la cierra cuando el foco está dentro.
 * - En pantallas angostas la disposición se apila: con la ficha abierta se
 *   oculta la lista y aparece «Volver a la lista» (ver `.maestro-detalle`).
 *
 * Uso:
 *   <div class="maestro-detalle" [class.con-ficha]="abierta">
 *     <section class="panel">...lista...</section>
 *     <app-ficha [abierta]="abierta" [clave]="form.id" titulo="Editar empresa" (cerrar)="...">
 *       ...campos...
 *       <div pie>...botones...</div>
 *       <p vacio>Elige una empresa de la lista.</p>
 *     </app-ficha>
 *   </div>
 */
@Component({
  selector: 'app-ficha',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'ficha',
    '[class.ficha--vacia]': '!abierta()',
    '(keydown.escape)': 'abierta() && cerrar.emit()',
  },
  template: `
    <section #caja class="ficha__caja" [attr.aria-labelledby]="abierta() ? idTitulo : null" tabindex="-1">
      @if (abierta()) {
        <header class="ficha__head">
          <button type="button" class="btn btn-link btn-sm ficha__volver" (click)="cerrar.emit()">
            <i class="bi bi-arrow-left me-1" aria-hidden="true"></i>Volver a la lista
          </button>
          <div class="ficha__titulos">
            <h2 class="ficha__titulo" [id]="idTitulo">{{ titulo() }}</h2>
            @if (subtitulo()) {
              <p class="ficha__subtitulo mono">{{ subtitulo() }}</p>
            }
          </div>
          <button type="button" class="btn-close ficha__cerrar" aria-label="Cerrar ficha" (click)="cerrar.emit()"></button>
        </header>
        <div class="ficha__body">
          <ng-content />
        </div>
        <footer class="ficha__pie">
          <ng-content select="[pie]" />
        </footer>
      } @else {
        <div class="ficha__vacio">
          <ng-content select="[vacio]" />
        </div>
      }
    </section>
  `,
})
export class Ficha {
  readonly abierta = input(false);
  /** Identifica el registro mostrado: si cambia con la ficha abierta, vuelve el foco al primer campo. */
  readonly clave = input<unknown>(null);
  readonly titulo = input('');
  readonly subtitulo = input<string | undefined>('');
  readonly cerrar = output<void>();

  readonly idTitulo = `ficha-${++siguienteId}`;
  private readonly caja = viewChild.required<ElementRef<HTMLElement>>('caja');
  private origen: HTMLElement | null = null;

  constructor() {
    effect(() => {
      const abierta = this.abierta();
      this.clave();
      untracked(() => {
        if (abierta) {
          const activo = document.activeElement as HTMLElement | null;
          // Recordar el origen solo si el foco viene de fuera de la ficha.
          if (activo && !this.caja().nativeElement.contains(activo)) {
            this.origen = activo;
          }
          setTimeout(() => this.primerFoco());
        } else if (this.origen) {
          const volver = this.origen;
          this.origen = null;
          setTimeout(() => volver.isConnected && volver.focus());
        }
      });
    });
  }

  private primerFoco(): void {
    const caja = this.caja().nativeElement;
    const campo = [...caja.querySelectorAll<HTMLElement>(
      '.ficha__body input:not([disabled]):not([type=hidden]), .ficha__body select:not([disabled]), .ficha__body textarea:not([disabled])',
    )].find(e => e.getClientRects().length > 0);
    (campo ?? caja).focus();
  }
}
