import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Plataforma } from './plataforma';

describe('Plataforma', () => {
  let component: Plataforma;
  let fixture: ComponentFixture<Plataforma>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Plataforma],
    }).compileComponents();

    fixture = TestBed.createComponent(Plataforma);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
