import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProyectoSondaje } from './proyecto-sondaje';

describe('ProyectoSondaje', () => {
  let component: ProyectoSondaje;
  let fixture: ComponentFixture<ProyectoSondaje>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProyectoSondaje],
    }).compileComponents();

    fixture = TestBed.createComponent(ProyectoSondaje);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
