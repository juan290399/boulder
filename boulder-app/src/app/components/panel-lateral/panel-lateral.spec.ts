import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PanelLateral } from './panel-lateral';

describe('PanelLateral', () => {
  let fixture: ComponentFixture<PanelLateral>;
  let cerrado: number;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [PanelLateral] }).compileComponents();
    fixture = TestBed.createComponent(PanelLateral);
    cerrado = 0;
    fixture.componentInstance.cerrar.subscribe(() => cerrado++);
    fixture.componentRef.setInput('titulo', 'Editar empresa');
  });

  it('cerrado no se muestra ni se puede alcanzar con el teclado', () => {
    fixture.detectChanges();
    const panel: HTMLElement = fixture.nativeElement.querySelector('.panel-lateral');
    expect(panel.classList.contains('show')).toBe(false);
    expect(panel.hasAttribute('inert')).toBe(true);
  });

  it('abierto es un diálogo con título y se cierra con Esc', () => {
    fixture.componentRef.setInput('abierto', true);
    fixture.detectChanges();
    const panel: HTMLElement = fixture.nativeElement.querySelector('.panel-lateral');
    expect(panel.classList.contains('show')).toBe(true);
    expect(panel.getAttribute('role')).toBe('dialog');
    const titulo = fixture.nativeElement.querySelector('#' + panel.getAttribute('aria-labelledby'));
    expect(titulo.textContent).toContain('Editar empresa');

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    expect(cerrado).toBe(1);
  });

  it('el fondo también lo cierra', () => {
    fixture.componentRef.setInput('abierto', true);
    fixture.detectChanges();
    fixture.nativeElement.querySelector('.offcanvas-backdrop').click();
    expect(cerrado).toBe(1);
  });
});
