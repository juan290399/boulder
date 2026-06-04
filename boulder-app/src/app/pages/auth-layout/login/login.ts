import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  credentials = { email: '', password: '' };
  loading = false;

  constructor(
    private http: HttpClient, 
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  onLogin(): void {
    this.loading = true;
    
    this.http.post<any>('http://localhost:8080/api/auth/login', this.credentials)
      .subscribe({
        next: (res) => {
          this.loading = false;
          console.log('Login exitoso', res);

          localStorage.setItem('token', res.token); 
          
          this.router.navigate(['/auth/role-selection']);
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.loading = false;
          console.error('Error en login', err);
          alert('Credenciales incorrectas');
          this.cdr.detectChanges();
        }
      });
  }
}
