import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './home.html',
  styleUrl: './home.scss'
})
export class Home {

  private baseUrl = 'http://localhost:8080/api/operacional/maquinas';

  maquinas: any[] = [];

  form: any = {
    id: null,
    codigo: '',
    nombre: '',
    tipo: '',
    marca: '',
    estado: 'DISPONIBLE'
  };

  busquedaCodigo: string = '';
  busquedaId: string = '';

  loading = false;

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) {}

  listarMaquinas(): void {
      if (this.loading) return;
      this.loading = true;

      console.log('DEBUG: Listando máquinas');

      this.http.get<any[]>(this.baseUrl)
        .subscribe({
          next: (data) => {
            this.maquinas = [...data];
            console.log('INFO: Máquinas cargadas', data.length);
            this.loading = false;
            
            this.cdr.detectChanges(); // 🔥 3. Fuerza a Angular a pintar los cambios AQUÍ
          },
          error: (err) => {
            console.error('ERROR: listarMaquinas', err);
            this.loading = false;
            this.cdr.detectChanges(); // También en caso de error para liberar el 'loading'
          }
        });
    }

  crearMaquina(): void {
    console.log('DEBUG: Creando máquina', this.form);

    this.http.post(this.baseUrl, this.form)
      .subscribe({
        next: (res: any) => {
          console.log('INFO: Máquina creada', res);
          this.listarMaquinas();
          this.limpiarFormulario();
          this.cdr.detectChanges(); // 🔥 Fuerza el renderizado del formulario limpio
        },
        error: (err) => {
          console.error('ERROR: crearMaquina', err);
        }
      });
  }

  actualizarMaquina(): void {
    if (!this.form.id) {
      console.warn('WARN: No hay ID para actualizar');
      return;
    }

    console.log('DEBUG: Actualizando máquina', this.form.id);

    this.http.put(`${this.baseUrl}/${this.form.id}`, this.form)
      .subscribe({
        next: (res: any) => {
          console.log('INFO: Máquina actualizada', res);
          this.listarMaquinas();
          this.limpiarFormulario();
          this.cdr.detectChanges(); // 🔥 Fuerza el renderizado del formulario limpio
        },
        error: (err) => {
          console.error('ERROR: actualizarMaquina', err);
        }
      });
  }

  buscarPorCodigo(): void {
    if (this.loading) return;
    this.loading = true;

    console.log('DEBUG: Buscando por código', this.busquedaCodigo);

    this.http.get<any>(`${this.baseUrl}/codigo/${this.busquedaCodigo}`)
      .subscribe({
        next: (res) => {
          this.maquinas = [res];
          console.log('INFO: Resultado por código:', res);
          this.loading = false;
          this.cdr.detectChanges(); // 🔥 Fuerza a Angular a pintar la máquina encontrada
        },
        error: (err) => {
          console.error('ERROR: buscarPorCodigo', err);
          this.loading = false;
          this.cdr.detectChanges(); // Quita el estado 'loading' visualmente
        }
      });
  }

  buscarPorId(): void {
    if (this.loading) return;
    this.loading = true;

    console.log('DEBUG: Buscando por ID', this.busquedaId);

    this.http.get<any>(`${this.baseUrl}/${this.busquedaId}`)
      .subscribe({
        next: (res) => {
          this.maquinas = [res];
          console.log('INFO: Resultado por ID:', res);
          this.loading = false;
          this.cdr.detectChanges(); // 🔥 Fuerza a Angular a pintar la máquina encontrada
        },
        error: (err) => {
          console.error('ERROR: buscarPorId', err);
          this.loading = false;
          this.cdr.detectChanges(); // Quita el estado 'loading' visualmente
        }
      });
  }

  editar(maquina: any): void {
    console.log('DEBUG: Editando máquina', maquina);
    this.form = { ...maquina };
    this.cdr.detectChanges(); // 🔥 Sube los datos al formulario de inmediato al hacer clic en "Editar"
  }

  limpiarFormulario(): void {
    console.log('DEBUG: limpiando formulario');
    this.form = {
      id: null,
      codigo: '',
      nombre: '',
      tipo: '',
      marca: '',
      estado: 'DISPONIBLE'
    };
    this.cdr.detectChanges(); // 🔥 Asegura el reset visual de los inputs
  }
}