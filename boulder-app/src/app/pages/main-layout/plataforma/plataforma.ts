import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-plataforma',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './plataforma.html',
  styleUrl: './plataforma.scss'
})
export class Plataforma implements OnInit {

  private baseUrl = 'http://localhost:8080/plataforma'; 

  plataformas: any[] = [];

  form: any = {
    id: null,
    codigo: '',
    nombre: '',
    ubicacion: '',
    capacidad: '',
    estado: 'ACTIVA'
  };

  busquedaCodigo: string = '';
  busquedaId: string = '';
  loading = false;

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.listarPlataformas();
  }

  listarPlataformas(): void {
    if (this.loading) return;
    this.loading = true;

    console.log('DEBUG: Listando plataformas desde:', this.baseUrl);

    this.http.get<any[]>(this.baseUrl)
      .subscribe({
        next: (data) => {
          this.plataformas = Array.isArray(data) ? [...data] : [data];
          console.log('INFO: Plataformas cargadas', this.plataformas.length);
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('ERROR: listarPlataformas', err);
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
  }

  crearPlataforma(): void {
    console.log('DEBUG: Creando plataforma', this.form);

    this.http.post(this.baseUrl, this.form)
      .subscribe({
        next: (res: any) => {
          console.log('INFO: Plataforma creada', res);
          this.listarPlataformas();
          this.limpiarFormulario();
        },
        error: (err) => {
          console.error('ERROR: crearPlataforma', err);
        }
      });
  }

  actualizarPlataforma(): void {
    if (!this.form.id) {
      console.warn('WARN: No hay ID para actualizar');
      return;
    }

    console.log('DEBUG: Actualizando plataforma con ID:', this.form.id);

    this.http.put(`${this.baseUrl}/${this.form.id}`, this.form)
      .subscribe({
        next: (res: any) => {
          console.log('INFO: Plataforma actualizada', res);
          this.listarPlataformas();
          this.limpiarFormulario();
        },
        error: (err) => {
          console.error('ERROR: actualizarPlataforma', err);
        }
      });
  }

  buscarPorCodigo(): void {
    if (!this.busquedaCodigo.trim()) return;
    if (this.loading) return;
    this.loading = true;

    this.http.get<any>(`${this.baseUrl}/codigo/${this.busquedaCodigo.trim()}`)
      .subscribe({
        next: (res) => {
          this.plataformas = res ? [res] : [];
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('ERROR: buscarPorCodigo', err);
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
  }

  buscarPorId(): void {
    if (!this.busquedaId.trim()) return;
    if (this.loading) return;
    this.loading = true;

    this.http.get<any>(`${this.baseUrl}/${this.busquedaId.trim()}`)
      .subscribe({
        next: (res) => {
          this.plataformas = res ? [res] : [];
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('ERROR: buscarPorId', err);
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
  }

  editar(plataforma: any): void {
    console.log('DEBUG: Cargando plataforma al formulario', plataforma);
    this.form = { ...plataforma };
    this.cdr.detectChanges();
  }

  limpiarFormulario(): void {
    console.log('DEBUG: Limpiando formulario');
    this.form = {
      id: null,
      codigo: '',
      nombre: '',
      ubicacion: '',
      capacidad: '',
      estado: 'ACTIVA'
    };
    this.cdr.detectChanges(); 
  }
}