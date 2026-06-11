import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-proyecto',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './proyecto.html',
  styleUrl: './proyecto.scss'
})
export class Proyecto implements OnInit {

  private baseUrl = 'http://localhost:8080//proyecto'; 

  proyectos: any[] = [];

  form: any = {
    id: null,
    codigo: '',
    nombre: '',
    fechaInicio: '',
    presupuesto: null,
    estado: 'PLANIFICACION'
  };

  busquedaCodigo: string = '';
  busquedaId: string = '';
  loading = false;

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.listarProyectos();
  }

  listarProyectos(): void {
    if (this.loading) return;
    this.loading = true;

    console.log('DEBUG: Listando proyectos desde:', this.baseUrl);

    this.http.get<any[]>(this.baseUrl)
      .subscribe({
        next: (data) => {
          this.proyectos = Array.isArray(data) ? [...data] : [data];
          console.log('INFO: Proyectos cargados', this.proyectos.length);
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('ERROR: listarProyectos', err);
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
  }

  crearProyecto(): void {
    console.log('DEBUG: Creando proyecto', this.form);

    this.http.post(this.baseUrl, this.form)
      .subscribe({
        next: (res: any) => {
          console.log('INFO: Proyecto creado', res);
          this.listarProyectos();
          this.limpiarFormulario();
        },
        error: (err) => {
          console.error('ERROR: crearProyecto', err);
        }
      });
  }

  actualizarProyecto(): void {
    if (!this.form.id) {
      console.warn('WARN: No hay ID para actualizar');
      return;
    }

    console.log('DEBUG: Actualizando proyecto con ID:', this.form.id);

    this.http.put(`${this.baseUrl}/${this.form.id}`, this.form)
      .subscribe({
        next: (res: any) => {
          console.log('INFO: Proyecto actualizado', res);
          this.listarProyectos();
          this.limpiarFormulario();
        },
        error: (err) => {
          console.error('ERROR: actualizarProyecto', err);
        }
      });
  }

  buscarPorCodigo(): void {
    if (!this.busquedaCodigo.trim()) return;
    if (this.loading) return;
    this.loading = true;

    console.log('DEBUG: Buscando proyecto por código', this.busquedaCodigo);

    this.http.get<any>(`${this.baseUrl}/codigo/${this.busquedaCodigo.trim()}`)
      .subscribe({
        next: (res) => {
          this.proyectos = res ? [res] : [];
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

    console.log('DEBUG: Buscando proyecto por ID', this.busquedaId);

    this.http.get<any>(`${this.baseUrl}/${this.busquedaId.trim()}`)
      .subscribe({
        next: (res) => {
          this.proyectos = res ? [res] : [];
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

  editar(proyecto: any): void {
    console.log('DEBUG: Cargando proyecto al formulario', proyecto);
    this.form = { ...proyecto };
    this.cdr.detectChanges();
  }

  limpiarFormulario(): void {
    console.log('DEBUG: Limpiando formulario de proyectos');
    this.form = {
      id: null,
      codigo: '',
      nombre: '',
      fechaInicio: '',
      presupuesto: null,
      estado: 'PLANIFICACION'
    };
    this.cdr.detectChanges(); 
  }
}