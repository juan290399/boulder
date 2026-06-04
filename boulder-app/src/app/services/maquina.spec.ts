import { TestBed } from '@angular/core/testing';

import { Maquina } from './maquina';

describe('Maquina', () => {
  let service: Maquina;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Maquina);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
