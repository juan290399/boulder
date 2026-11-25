import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Ficha } from './ficha';

@Component({
  imports: [Ficha],
  template: `
    <app-ficha [abierta]="abierta()" titulo="Editar empresa" subtitulo="MIN" (cerrar)="cerrados = cerrados + 1">
      <input id="campo" />
      <div pie><button type="button">Guardar</button></div>
      <p vacio>Elige una empresa.</p>
    </app-ficha>
  `,
})
class Anfitrion {
  readonly abierta = signal(false);
  cerrados = 0;
}

describe('Ficha', () => {
  let fixture: ComponentFixture<Anfitrion>;
  let raiz: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [Anfitrion] }).compileComponents();
    fixture = TestBed.createComponent(Anfitrion);
    raiz = fixture.nativeElement;
    fixture.detectChanges();
  });

  it('cerrada muestra la invitación y no los campos', () => {
    expect(raiz.textContent).toContain('Elige una empresa.');
    expect(raiz.querySelector('#campo')).toBeNull();
    expect(raiz.querySelector('app-ficha')!.classList.contains('ficha--vacia')).toBe(true);
  });

  it('abierta muestra título, campos y pie, con la región nombrada por el título', () => {
    fixture.componentInstance.abierta.set(true);
    fixture.detectChanges();
    const caja = raiz.querySelector('.ficha__caja')!;
    const titulo = raiz.querySelector('#' + caja.getAttribute('aria-labelledby'))!;
    expect(titulo.textContent).toContain('Editar empresa');
    expect(raiz.querySelector('#campo')).not.toBeNull();
    expect(raiz.querySelector('.ficha__pie')!.textContent).toContain('Guardar');
    expect(raiz.textContent).not.toContain('Elige una empresa.');
  });

  it('Esc dentro de la ficha y el botón cerrar avisan al padre', () => {
    fixture.componentInstance.abierta.set(true);
    fixture.detectChanges();
    raiz.querySelector('#campo')!.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
    (raiz.querySelector('.ficha__cerrar') as HTMLButtonElement).click();
    expect(fixture.componentInstance.cerrados).toBe(2);
  });
});
