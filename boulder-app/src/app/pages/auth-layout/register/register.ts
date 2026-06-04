import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register {
  user = { nombre: '', email: '', password: '' };
  loading = false;

  constructor(private http: HttpClient, private router: Router, private cdr: ChangeDetectorRef) {}

  onRegister(): void {
    this.loading = true;
    this.http.post('http://localhost:8080/api/auth/register', this.user)
      .subscribe({
        next: (res) => {
          this.loading = false;
          alert('¡Registro exitoso! Ahora puedes iniciar sesión.');
          this.router.navigate(['/auth/login']);
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.loading = false;
          console.error(err);
          this.cdr.detectChanges();
        }
      });
  }
}