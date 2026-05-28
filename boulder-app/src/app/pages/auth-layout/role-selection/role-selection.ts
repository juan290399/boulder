import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-role-selection',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './role-selection.html'
})
export class RoleSelection {

  roles = ['ADMIN', 'GEOLOGO'];
  selectedRole: string | null = null;

  onRoleSelected() {
    console.log(this.selectedRole);
  }
}