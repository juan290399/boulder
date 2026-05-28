import { Routes } from '@angular/router';
import { MainLayout } from './pages/main-layout/main-layout';
import { AuthLayout } from './pages/auth-layout/auth-layout';
import { Home } from './pages/main-layout/home/home';
import { AdminLayout } from './pages/admin-layout/admin-layout';
import { Register } from './pages/auth-layout/register/register';
import { RoleSelection } from './pages/auth-layout/role-selection/role-selection';
import { Login } from './pages/auth-layout/login/login';

export const routes: Routes = [
      { path: '', 
    component: MainLayout,
    children: [
      { path: '', component: Home },
    ],    
  },
  { path: 'login', 
    component: AuthLayout,
    children: [
      { path: '', component: Login },
      { path: 'seleccionar-rol', component: RoleSelection },
      { path: 'logout', component: Home }, 
      { path: 'forgot-password', component: Home }, 
      { path: 'reset-password', component: Home }, 
      { path: 'change-password', component: Home }, 
      { path: 'register', component: Register },
    ]
  }, 
];
