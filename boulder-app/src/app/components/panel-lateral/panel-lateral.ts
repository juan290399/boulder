import {
  ChangeDetectionStrategy, Component, ElementRef, HostListener, effect, input, output, viewChild,
} from '@angular/core';

let siguienteId = 0;

/**
 * Panel que entra desde la derecha para crear o editar un registro sin perder
 * de vista la lista. Usa las clases `offcanvas` de Bootstrap, pero el estado lo
 * maneja Angular (no el JavaScript de Bootstrap), así la plantilla decide cuándo
 * está abierto.
 *
 * Accesibilidad: es un diálogo modal (`role="dialog"`, `aria-modal`), lleva el
 * foco al primer campo al abrir, lo mantiene dentro mientras está abierto, se
 * cierra con Esc o con el fondo y devuelve el foco al botón que lo abrió.
 *
 * Uso:
 *   <app-panel-lateral [abierto]="..." titulo="Editar empresa" (cerrar)="...">
 *     ...campos...
 *     <div pie>...botones...</div>
 *   </app-panel-lateral>
 */
@Component({
  selector: 'app-panel-lateral',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (abierto()) {
      <div class="offcanvas-backdrop fade show" (click)="cerrar.emit()"></div>
    }
    <div #panel class="offcanvas offcanvas-end panel-lateral" [class.show]="abierto()"
         role="dialog" aria-modal="true" [attr.aria-labelledby]="idTitulo" [attr.aria-hidden]="!abierto()"
         [attr.inert]="abierto() ? null : ''" tabindex="-1">
      <div class="offcanvas-header">
        <div>
          <h2 class="offcanvas-title panel-lateral__titulo" [id]="idTitulo">{{ titulo() }}</h2>
          @if (subtitulo()) {
            <p class="panel-lateral__subtitulo mono">{{ subtitulo() }}</p>
          }
        </div>
        <button type="button" class="btn-close" aria-label="Cerrar panel" (click)="cerrar.emit()"></button>
      </div>
      <div class="offcanvas-body">
        <ng-content />
      </div>
      <div class="panel-lateral__pie">
        <ng-content select="[pie]" />
      </div>
    </div>
  `,
  styles: `
    .panel-lateral {
      --bs-offcanvas-width: min(480px, 100vw);
      --bs-offcanvas-bg: var(--bo-surface);
      --bs-offcanvas-border-color: var(--bo-border);
      box-shadow: var(--bo-shadow-lg);
    }
    .panel-lateral__titulo {
      margin: 0;
      font-size: 1.1rem;
      font-weight: 700;
      color: var(--bo-ink);
    }
    .panel-lateral__subtitulo {
      margin: 0.15rem 0 0;
      font-size: 0.8rem;
      color: var(--bo-ink-2);
    }
    .offcanvas-header { border-bottom: 1px solid var(--bo-border); }
    .panel-lateral__pie:not(:empty) {
      padding: 0.875rem 1rem;
      border-top: 1px solid var(--bo-border);
      background: var(--bo-surface-2);
    }
    @media (prefers-reduced-motion: reduce) {
      .panel-lateral { transition: none; }
    }
  `,
})
export class PanelLateral {
  readonly abierto = input(false);
  readonly titulo = input('');
  readonly subtitulo = input('');
  readonly cerrar = output<void>();

  readonly idTitulo = `panel-lateral-${++siguienteId}`;
  private readonly panel = viewChild.required<ElementRef<HTMLElement>>('panel');
  private origen: HTMLElement | null = null;

  constructor() {
    effect(() => {
      if (this.abierto()) {
        // Recordar quién abrió el panel para devolverle el foco al cerrar.
        this.origen = document.activeElement as HTMLElement | null;
        setTimeout(() => this.primerFoco());
      } else if (this.origen) {
        const volver = this.origen;
        this.origen = null;
        setTimeout(() => volver.isConnected && volver.focus());
      }
    });
  }

  @HostListener('document:keydown', ['$event'])
  teclado(evento: KeyboardEvent): void {
    if (!this.abierto()) {
      return;
    }
    if (evento.key === 'Escape') {
      evento.preventDefault();
      this.cerrar.emit();
    } else if (evento.key === 'Tab') {
      this.atraparFoco(evento);
    }
  }

  private enfocables(): HTMLElement[] {
    return [...this.panel().nativeElement.querySelectorAll<HTMLElement>(
      'input:not([disabled]):not([type=hidden]), select:not([disabled]), textarea:not([disabled]), button:not([disabled]), a[href]',
    )].filter(e => e.getClientRects().length > 0);
  }

  private primerFoco(): void {
    const campos = this.enfocables().filter(e => !e.classList.contains('btn-close'));
    (campos[0] ?? this.panel().nativeElement).focus();
  }

  /** Mientras está abierto, Tab y Shift+Tab giran dentro del panel. */
  private atraparFoco(evento: KeyboardEvent): void {
    const lista = this.enfocables();
    if (!lista.length) {
      return;
    }
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
