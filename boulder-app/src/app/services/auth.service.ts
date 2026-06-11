import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = 'http://localhost:8080/api/auth/login';

  constructor(private http: HttpClient) {}

  login(credentials: { usuario: string, contrasenia: string }): Observable<any> {
    return this.http.post<any>(this.apiUrl, credentials);
  }

  saveToken(token: string): void {
    localStorage.setItem('token', token);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getRolesFromToken(): string[] {
    const token = this.getToken();

    if (!token) {
      return [];
    }

    try {
      const payloadBase64 = token.split('.')[1];
      const decodedJson = atob(payloadBase64);
      const payload = JSON.parse(decodedJson);

      console.log('JWT payload:', payload);

      return payload.roles || [];
    } catch (error) {
      console.error('Error parseando JWT:', error);
      return [];
    }
  }

  getSelectedRole(): string | null {
    return localStorage.getItem('user_role');
  }

  logout(): void {
    localStorage.clear();
  }
}