import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Maquina } from './maquina';

describe('Maquina', () => {
  let component: Maquina;
  let fixture: ComponentFixture<Maquina>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Maquina],
    }).compileComponents();

    fixture = TestBed.createComponent(Maquina);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
