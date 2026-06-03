import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home.html',
  styleUrl: './home.scss'
})
export class Home {

  maquinas: any[] = [];

  constructor(private http: HttpClient) {}

  cargarMaquinas(): void {

    console.log('Botón presionado');

    this.http.get<any[]>(
      'http://localhost:8080/api/operacional/maquinas'
    )
    .subscribe({
      next: (response) => {
        console.log('DATOS RECIBIDOS:', response);
        this.maquinas = response;
      },
      error: (error) => {
        console.error('ERROR BACKEND:', error);
      }
    });
  }
}