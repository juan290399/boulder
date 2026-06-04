import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class MaquinaService {

  private baseUrl = 'http://localhost:8080/api/maquinas';

  constructor(private http: HttpClient) {}

  listarMaquinas(): Observable<any[]> {
    return this.http.get<any[]>(this.baseUrl);
  }

  obtenerPorId(id: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${id}`);
  }

  obtenerPorCodigo(codigo: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/codigo/${codigo}`);
  }

  crearMaquina(data: any): Observable<any> {
    return this.http.post(this.baseUrl, data);
  }

  actualizarMaquina(id: string, data: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}`, data);
  }
}