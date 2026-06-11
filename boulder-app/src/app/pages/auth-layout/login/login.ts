import { Component, EventEmitter, Output } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule], 
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {
  @Output() loginExitoso = new EventEmitter<string[]>();
  @Output() cerrar = new EventEmitter<void>();

  loginForm: FormGroup;
  loading = false;
  errorMessage: string = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    
    this.loginForm = this.fb.group({
      usuario: ['', [Validators.required, Validators.minLength(4)]],
      contrasenia: ['', [Validators.required, Validators.minLength(4)]],
    });
  }

  onLogin(event: Event): void {
    event.preventDefault();

    if (this.loginForm.invalid) {
      this.errorMessage = 'Por favor complete correctamente los campos.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.loginForm.disable(); 

    this.authService.login(this.loginForm.value).subscribe({
      next: (res: any) => {
        this.loading = false;
        this.authService.saveToken(res.token);
        
        const roles = this.authService.getRolesFromToken();
        console.log('Login Exitoso. Roles detectados:', roles);
        this.loginExitoso.emit(roles);

        this.router.navigate(['/auth/seleccionar-rol']).then((navigated) => {
          if (navigated) {
            console.log('¡Navegación exitosa a la pantalla de selección de roles!');
          } else {
            console.error('El Router no completó la navegación.');
          }
        });
      },
      error: (err: any) => {
        this.loading = false;
        this.loginForm.enable(); 
        console.error(err);
        this.errorMessage = err.error?.mensaje || 'Credenciales incorrectas o usuario inactivo';
        alert(this.errorMessage);
      },
    });
  }

  cerrarModal(): void {
    this.cerrar.emit();
  }
}