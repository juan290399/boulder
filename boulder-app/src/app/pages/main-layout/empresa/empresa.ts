import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-empresa',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './empresa.html',
  styleUrl: './empresa.scss'
})
export class Empresa implements OnInit {

  private baseUrl = 'http://localhost:8080/empresas'; 

  empresas: any[] = [];

  form: any = {
    id: null,
    nit: '',
    nombre: '',
    telefono: '',
    correo: '',
    estado: 'ACTIVA'
  };

  busquedaNit: string = '';
  busquedaId: string = '';
  loading = false;

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.listarEmpresas();
  }

  listarEmpresas(): void {
    if (this.loading) return;
    this.loading = true;

    console.log('DEBUG: Listando empresas desde:', this.baseUrl);

    this.http.get<any[]>(this.baseUrl)
      .subscribe({
        next: (data) => {
          this.empresas = Array.isArray(data) ? [...data] : [data];
          console.log('INFO: Empresas cargadas', this.empresas.length);
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('ERROR: listarEmpresas', err);
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
  }

  crearEmpresa(): void {
    console.log('DEBUG: Creando empresa', this.form);

    this.http.post(this.baseUrl, this.form)
      .subscribe({
        next: (res: any) => {
          console.log('INFO: Empresa creada', res);
          this.listarEmpresas();
          this.limpiarFormulario();
        },
        error: (err) => {
          console.error('ERROR: crearEmpresa', err);
        }
      });
  }

  actualizarEmpresa(): void {
    if (!this.form.id) {
      console.warn('WARN: No hay ID para actualizar');
      return;
    }

    console.log('DEBUG: Actualizando empresa con ID:', this.form.id);

    this.http.put(`${this.baseUrl}/${this.form.id}`, this.form)
      .subscribe({
        next: (res: any) => {
          console.log('INFO: Empresa actualizada', res);
          this.listarEmpresas();
          this.limpiarFormulario();
        },
        error: (err) => {
          console.error('ERROR: actualizarEmpresa', err);
        }
      });
  }

  buscarPorNit(): void {
    if (!this.busquedaNit.trim()) return;
    if (this.loading) return;
    this.loading = true;

    console.log('DEBUG: Buscando por NIT', this.busquedaNit);

    this.http.get<any>(`${this.baseUrl}/nit/${this.busquedaNit.trim()}`)
      .subscribe({
        next: (res) => {
          this.empresas = res ? [res] : [];
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('ERROR: buscarPorNit', err);
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
  }

  buscarPorId(): void {
    if (!this.busquedaId.trim()) return;
    if (this.loading) return;
    this.loading = true;

    console.log('DEBUG: Buscando por ID', this.busquedaId);

    this.http.get<any>(`${this.baseUrl}/${this.busquedaId.trim()}`)
      .subscribe({
        next: (res) => {
          this.empresas = res ? [res] : [];
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

  editar(empresa: any): void {
    console.log('DEBUG: Cargando empresa al formulario', empresa);
    this.form = { ...empresa };
    this.cdr.detectChanges();
  }

  limpiarFormulario(): void {
    console.log('DEBUG: Limpiando formulario');
    this.form = {
      id: null,
      nit: '',
      nombre: '',
      telefono: '',
      correo: '',
      estado: 'ACTIVA'
    };
    this.cdr.detectChanges(); 
  }
}