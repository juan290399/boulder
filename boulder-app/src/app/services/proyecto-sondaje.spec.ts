import { TestBed } from '@angular/core/testing';

import { ProyectoSondaje } from './proyecto-sondaje';

describe('ProyectoSondaje', () => {
  let service: ProyectoSondaje;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ProyectoSondaje);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
