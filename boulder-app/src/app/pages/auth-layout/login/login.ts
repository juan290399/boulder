import { ChangeDetectorRef, Component, EventEmitter, Output, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
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
  private readonly cdr = inject(ChangeDetectorRef);

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
      next: (res) => {
        this.loading = false;
        this.authService.saveToken(res.token);

        // RoleSelection vuelve a leer los roles del token; el evento es solo
        // para quien embeba este componente como modal.
        this.loginExitoso.emit(this.authService.getRolesFromToken());

        this.router.navigate(['/auth/seleccionar-rol']);
      },
      error: (e: HttpErrorResponse) => {
        this.loading = false;
        this.loginForm.enable();
        // Con 429 el backend avisa que la cuenta quedó bloqueada por intentos
        // fallidos; el mensaje genérico haría creer que basta con reintentar.
        this.errorMessage = e.status === 429 && e.error?.mensaje
          ? e.error.mensaje
          : 'Credenciales incorrectas o usuario inactivo';
        this.cdr.detectChanges();
      },
    });
  }

  cerrarModal(): void {
    this.cerrar.emit();
  }
}