import { TestBed } from '@angular/core/testing';

import { Sondaje } from './sondaje';

describe('Sondaje', () => {
  let service: Sondaje;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Sondaje);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
