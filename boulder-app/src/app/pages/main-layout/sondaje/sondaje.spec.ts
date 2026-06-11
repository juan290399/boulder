import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Sondaje } from './sondaje';

describe('Sondaje', () => {
  let component: Sondaje;
  let fixture: ComponentFixture<Sondaje>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Sondaje],
    }).compileComponents();

    fixture = TestBed.createComponent(Sondaje);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
