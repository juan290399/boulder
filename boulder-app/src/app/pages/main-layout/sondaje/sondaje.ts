import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-sondaje',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './sondaje.html',
  styleUrl: './sondaje.scss'
})
export class Sondaje implements OnInit {

  private baseUrl = 'http://localhost:8080/sondaje'; 

  sondajes: any[] = [];

  form: any = {
    id: null,
    codigo: '',
    profundidadMaxima: null,
    inclinacion: null,      
    plataformaId: null,    
    estado: 'EN_PROGRESO'
  };

  busquedaCodigo: string = '';
  busquedaId: string = '';
  loading = false;

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.listarSondajes();
  }

  listarSondajes(): void {
    if (this.loading) return;
    this.loading = true;

    console.log('DEBUG: Listando sondajes desde:', this.baseUrl);

    this.http.get<any[]>(this.baseUrl)
      .subscribe({
        next: (data) => {
          this.sondajes = Array.isArray(data) ? [...data] : [data];
          console.log('INFO: Sondajes cargados', this.sondajes.length);
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('ERROR: listarSondajes', err);
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
  }

  crearSondaje(): void {
    console.log('DEBUG: Creando registro de sondaje', this.form);

    this.http.post(this.baseUrl, this.form)
      .subscribe({
        next: (res: any) => {
          console.log('INFO: Sondaje creado', res);
          this.listarSondajes();
          this.limpiarFormulario();
        },
        error: (err) => {
          console.error('ERROR: crearSondaje', err);
        }
      });
  }

  actualizarSondaje(): void {
    if (!this.form.id) {
      console.warn('WARN: No hay ID para actualizar');
      return;
    }

    console.log('DEBUG: Actualizando sondaje con ID:', this.form.id);

    this.http.put(`${this.baseUrl}/${this.form.id}`, this.form)
      .subscribe({
        next: (res: any) => {
          console.log('INFO: Sondaje actualizado', res);
          this.listarSondajes();
          this.limpiarFormulario();
        },
        error: (err) => {
          console.error('ERROR: actualizarSondaje', err);
        }
      });
  }

  buscarPorCodigo(): void {
    if (!this.busquedaCodigo.trim()) return;
    if (this.loading) return;
    this.loading = true;

    console.log('DEBUG: Buscando sondaje por código', this.busquedaCodigo);

    this.http.get<any>(`${this.baseUrl}/codigo/${this.busquedaCodigo.trim()}`)
      .subscribe({
        next: (res) => {
          this.sondajes = res ? [res] : [];
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

    console.log('DEBUG: Buscando sondaje por ID', this.busquedaId);

    this.http.get<any>(`${this.baseUrl}/${this.busquedaId.trim()}`)
      .subscribe({
        next: (res) => {
          this.sondajes = res ? [res] : [];
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

  editar(sondaje: any): void {
    console.log('DEBUG: Cargando sondaje al formulario', sondaje);
    this.form = { ...sondaje };
    this.cdr.detectChanges();
  }

  limpiarFormulario(): void {
    console.log('DEBUG: Limpiando formulario de sondajes');
    this.form = {
      id: null,
      codigo: '',
      profundidadMaxima: null,
      inclinacion: null,
      plataformaId: null,
      estado: 'EN_PROGRESO'
    };
    this.cdr.detectChanges(); 
  }
}