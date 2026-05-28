import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class Sondaje {
  private apiUrl =
    'http://localhost:8080/api/operacional/sondajes';

  constructor(private http: HttpClient) {}

  obtenerSondajes(proyectoId: string) {
    return this.http.get(
      `${this.apiUrl}/proyecto/${proyectoId}`
    );
  }
}
