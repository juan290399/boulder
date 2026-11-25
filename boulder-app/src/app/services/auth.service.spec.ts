import { TestBed } from '@angular/core/testing';

import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AuthService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('lee nombres con tildes del token sin romper los caracteres', () => {
    const json = JSON.stringify({ sub: 'jperez', nombre: 'José Peña', exp: 4102444800 });
    const bytes = new TextEncoder().encode(json);
    const base64url = btoa(String.fromCharCode(...bytes))
      .replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
    localStorage.setItem('token', `x.${base64url}.y`);

    expect(service.getNombreFromToken()).toBe('José Peña');
    expect(service.getInicialesFromToken()).toBe('JP');
    localStorage.removeItem('token');
  });
});
