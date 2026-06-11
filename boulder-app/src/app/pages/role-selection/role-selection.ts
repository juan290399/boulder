import { Component, OnInit, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-role-selection',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './role-selection.html',
  styleUrls: ['./role-selection.scss']
})
export class RoleSelection implements OnInit {

  roles: string[] = [];
  selectedRole: string | null = null;

  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private zone: NgZone
  ) {}

  ngOnInit(): void {

    const rolesDelToken = this.authService.getRolesFromToken();

    this.roles = rolesDelToken
      .filter(r => r.startsWith('ROLE_'))
      .map(r => r.replace('ROLE_', '').trim());

    console.log('Roles disponibles:', this.roles);

    if (this.roles.length === 1) {
      this.selectedRole = this.roles[0];

      console.log('Auto-selección:', this.selectedRole);

      setTimeout(() => {
        this.onRoleSelected(this.selectedRole);
      }, 50);
    }
  }

  onRoleSelected(valorSeleccionado?: string | null, event?: Event): void {

    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    const rolAProcesar = valorSeleccionado || this.selectedRole;

    console.log('Rol recibido:', rolAProcesar);

    if (!rolAProcesar) {
      console.warn('No se seleccionó rol');
      return;
    }

    const rolLimpio = rolAProcesar.toUpperCase().trim();

    console.log('Rol limpio:', rolLimpio);

    localStorage.setItem('user_role', rolLimpio);

    this.zone.run(() => {

      if (rolLimpio === 'ADMIN') {

        console.log('Navegando a /admin');

        this.router.navigate(['/admin']).then(result => {
          console.log('Resultado navegación admin:', result);
        });

      } else {

        console.log('Navegando a /maquinas');

        this.router.navigate(['/maquinas']).then(result => {
          console.log('Resultado navegación maquinas:', result);
        });

      }

      this.cdr.detectChanges();

    });
  }
}