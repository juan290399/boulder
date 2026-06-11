import { Routes } from '@angular/router';
import { authGuard } from './guards/auth';
import { MainLayout } from './pages/main-layout/main-layout';
import { AuthLayout } from './pages/auth-layout/auth-layout';
import { AdminLayout } from './pages/admin-layout/admin-layout';
import { Home } from './pages/main-layout/home/home';
import { Register } from './pages/auth-layout/register/register';
import { RoleSelection } from './pages/auth-layout/role-selection/role-selection';
import { Login } from './pages/auth-layout/login/login';
import { Plataforma } from './pages/main-layout/plataforma/plataforma';
import { Maquina } from './pages/main-layout/maquina/maquina';
import { Proyecto } from './pages/main-layout/proyecto/proyecto';
import { Empresa } from './pages/main-layout/empresa/empresa';
import { Sondaje } from './pages/main-layout/sondaje/sondaje';


export const routes: Routes = [

  { 
    path: 'auth', 
    component: AuthLayout,
    children: [
      { path: 'login', component: Login },
      { path: 'register', component: Register },
      { path: 'seleccionar-rol', component: RoleSelection },
      { path: 'logout', redirectTo: 'login', pathMatch: 'full' },
      { path: '', redirectTo: 'login', pathMatch: 'full' }
    ]
  },
  {
    path: 'admin',
    component: AdminLayout,
    canActivate: [authGuard],
    data: { roles: ['ROLE_ADMIN'] },
    children: [
      { path: '', redirectTo: 'usuarios', pathMatch: 'full' },
      
      { path: 'usuarios', component: AdminLayout }, 
      { path: 'auditoria', component: AdminLayout }
    ]
  },
  { 
    path: '', 
    component: MainLayout,
    canActivate: [authGuard],
    children: [
      { path: 'maquinas', component: Home },
      { path: 'plataformas', component: Plataforma },
      { path: 'maquina', component: Maquina },
      { path: 'empresas', component: Empresa },
      { path: 'proyectos', component: Proyecto },
      { path: 'sondajes', component: Sondaje },
      { path: '', redirectTo: 'maquinas', pathMatch: 'full' }
    ],    
  },

  { path: '**', redirectTo: 'auth/login' }
];