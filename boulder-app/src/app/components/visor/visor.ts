import {
  ChangeDetectionStrategy, Component, ElementRef, HostListener, OnDestroy, OnInit, input, output, viewChild,
} from '@angular/core';

/**
 * Visor de una foto ampliada (Fotos de testigo y Quicklog). Es un diálogo
 * modal: se anuncia con su etiqueta, lleva el foco al botón de cerrar, lo
 * mantiene dentro mientras está abierto, se cierra con Esc o con el fondo y
 * devuelve el foco a la miniatura que lo abrió.
 *
 * Uso:
 *   @if (ampliada(); as a) {
 *     <app-visor etiqueta="Foto de las cajas 1–3" (cerrar)="ampliada.set(null)">
 *       <div info>...datos de la foto...</div>
 *       <div acciones>...botones extra...</div>
 *       <img ...>
 *     </app-visor>
 *   }
 */
@Component({
  selector: 'app-visor',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="visor" (click)="cerrar.emit()">
      <div #caja class="visor__caja" role="dialog" aria-modal="true" [attr.aria-label]="etiqueta()"
           (click)="$event.stopPropagation()">
        <div class="visor__barra">
          <div class="visor__info"><ng-content select="[info]" /></div>
          <div class="visor__acciones">
            <ng-content select="[acciones]" />
            <button #botonCerrar type="button" class="btn btn-sm btn-outline-secondary" aria-label="Cerrar visor"
                    (click)="cerrar.emit()">
              <i class="bi bi-x-lg" aria-hidden="true"></i>
            </button>
          </div>
        </div>
        <ng-content />
      </div>
    </div>
  `,
  styles: `
    .visor {
      position: fixed;
      inset: 0;
      z-index: 1050;
      display: grid;
      place-items: center;
      padding: 1.5rem;
      background: rgb(from var(--bo-navy-900) r g b / 0.8);
    }
    .visor__caja {
      display: flex;
      flex-direction: column;
      max-width: min(1400px, 96vw);
      max-height: 94vh;
      overflow: hidden;
      background: var(--bo-surface);
      border-radius: var(--bo-r-sm);
      box-shadow: var(--bo-shadow-lg);
    }
    .visor__barra {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 1rem;
      padding: 0.75rem 1rem;
      border-bottom: 1px solid var(--bo-border);
      font-size: 0.9rem;
    }
    .visor__acciones { display: flex; gap: 0.5rem; flex-shrink: 0; }
    :host ::ng-deep .visor__img {
      display: block;
      max-width: 100%;
      max-height: calc(94vh - 70px);
      margin: auto;
      object-fit: contain;
    }
  `,
})
export class Visor implements OnInit, OnDestroy {
  /** Lo que anuncia el lector de pantalla al abrir, p. ej. «Foto de las cajas 1–3». */
  readonly etiqueta = input('Foto ampliada');
  readonly cerrar = output<void>();

  private readonly caja = viewChild.required<ElementRef<HTMLElement>>('caja');
  private readonly botonCerrar = viewChild.required<ElementRef<HTMLButtonElement>>('botonCerrar');
  private origen: HTMLElement | null = null;

  ngOnInit(): void {
    this.origen = document.activeElement as HTMLElement | null;
    setTimeout(() => this.botonCerrar().nativeElement.focus());
  }

  ngOnDestroy(): void {
    const volver = this.origen;
    setTimeout(() => volver?.isConnected && volver.focus());
  }

  @HostListener('document:keydown', ['$event'])
  teclado(evento: KeyboardEvent): void {
    if (evento.key === 'Escape') {
      evento.preventDefault();
      this.cerrar.emit();
    } else if (evento.key === 'Tab') {
      // El foco gira dentro del visor mientras está abierto.
      const lista = [...this.caja().nativeElement.querySelectorAll<HTMLElement>('button:not([disabled]), a[href]')];
      const primero = lista[0];
      const ultimo = lista[lista.length - 1];
      if (evento.shiftKey && document.activeElement === primero) {
        evento.preventDefault();
        ultimo.focus();
      } else if (!evento.shiftKey && document.activeElement === ultimo) {
        evento.preventDefault();
        primero.focus();
      }
    }
  }
}
