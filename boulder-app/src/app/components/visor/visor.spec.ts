import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Visor } from './visor';

@Component({
  imports: [Visor],
  template: `
    <button id="miniatura" type="button" (click)="abierto.set(true)">Miniatura</button>
    @if (abierto()) {
      <app-visor etiqueta="Foto de las cajas 1 a 3" (cerrar)="abierto.set(false); cerrados = cerrados + 1">
        <div info>Cajas 1–3</div>
        <img class="visor__img" src="data:," alt="Testigo de las cajas 1 a 3">
      </app-visor>
    }
  `,
})
class Anfitrion {
  readonly abierto = signal(false);
  cerrados = 0;
}

describe('Visor', () => {
  let fixture: ComponentFixture<Anfitrion>;
  let raiz: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [Anfitrion] }).compileComponents();
    fixture = TestBed.createComponent(Anfitrion);
    raiz = fixture.nativeElement;
    document.body.appendChild(raiz);
    fixture.detectChanges();
  });

  afterEach(() => raiz.remove());

  it('es un diálogo modal con etiqueta y un botón de cerrar con nombre', () => {
    fixture.componentInstance.abierto.set(true);
    fixture.detectChanges();
    const dialogo = raiz.querySelector('[role="dialog"]')!;
    expect(dialogo.getAttribute('aria-modal')).toBe('true');
    expect(dialogo.getAttribute('aria-label')).toBe('Foto de las cajas 1 a 3');
    expect(raiz.querySelector('button[aria-label="Cerrar visor"]')).not.toBeNull();
  });

  it('lleva el foco al botón de cerrar y se cierra con Esc', async () => {
    (raiz.querySelector('#miniatura') as HTMLButtonElement).focus();
    fixture.componentInstance.abierto.set(true);
    fixture.detectChanges();
    await new Promise(r => setTimeout(r));
    expect(document.activeElement?.getAttribute('aria-label')).toBe('Cerrar visor');

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    fixture.detectChanges();
    expect(fixture.componentInstance.cerrados).toBe(1);
  });

  it('al cerrarse devuelve el foco a la miniatura', async () => {
    const miniatura = raiz.querySelector('#miniatura') as HTMLButtonElement;
    miniatura.focus();
    fixture.componentInstance.abierto.set(true);
    fixture.detectChanges();
    await new Promise(r => setTimeout(r));
    fixture.componentInstance.abierto.set(false);
    fixture.detectChanges();
    await new Promise(r => setTimeout(r));
    expect(document.activeElement).toBe(miniatura);
  });
});
