import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';  
import { Router } from '@angular/router';

@Component({
  selector: 'app-role-selection',
  standalone: true,
  imports: [
    CommonModule, 
    FormsModule
  ],
  templateUrl: './role-selection.html',
  styleUrls: ['./role-selection.scss']
})
export class RoleSelection implements OnInit {
  

  roles: string[] = ['GEOLOGO', 'ADMIN']; 
  selectedRole: string | null = null;

  constructor(
    private router: Router, 
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    if (this.roles && this.roles.length === 1) {
      this.selectedRole = this.roles[0];
      this.onRoleSelected();
    }
  }

  onRoleSelected(): void {
    if (!this.selectedRole) return;

    console.log('INFO: Rol configurado para la sesión:', this.selectedRole);
    localStorage.setItem('user_role', this.selectedRole);
    
    this.router.navigate(['/']);
    this.cdr.detectChanges();
  }
}