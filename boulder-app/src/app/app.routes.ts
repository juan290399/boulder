import { Routes } from '@angular/router';
import { MainLayout } from './pages/main-layout/main-layout';
import { AuthLayout } from './pages/auth-layout/auth-layout';
import { Home } from './pages/main-layout/home/home';
import { Register } from './pages/auth-layout/register/register';
import { RoleSelection } from './pages/auth-layout/role-selection/role-selection';
import { Login } from './pages/auth-layout/login/login';

export const routes: Routes = [
  // 1. MÓDULO DE AUTENTICACIÓN (Independiente y Limpio)
  { 
    path: 'auth', 
    component: AuthLayout, // 👈 Este layout NO debe tener el menú izquierdo ni el header en su HTML
    children: [
      { path: 'login', component: Login },
      { path: 'register', component: Register },
      { path: 'seleccionar-rol', component: RoleSelection },
      { path: 'logout', redirectTo: 'login', pathMatch: 'full' },
      { path: '', redirectTo: 'login', pathMatch: 'full' }
    ]
  },

  // 2. MÓDULO PRIVADO (Con el menú lateral, Sondajes, Equipos, etc.)
  { 
    path: '', 
    component: MainLayout, // 👈 Este es el que contiene el Sidebar, Navbar y el <router-outlet> para el Home
    children: [
      { path: 'maquinas', component: Home },
      { path: '', redirectTo: 'maquinas', pathMatch: 'full' } // Al entrar a '/' te lleva a las máquinas
    ],    
  },

  // 3. Redirección por defecto si escriben cualquier otra ruta
  { path: '**', redirectTo: 'auth/login' }
];