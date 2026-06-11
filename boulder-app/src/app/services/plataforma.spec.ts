import { TestBed } from '@angular/core/testing';

import { Plataforma } from './plataforma';

describe('Plataforma', () => {
  let service: Plataforma;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Plataforma);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
