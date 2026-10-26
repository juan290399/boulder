import { Routes } from '@angular/router';
import { adminGuard, authGuard, logoutGuard } from './guards/auth';
import { MainLayout } from './pages/main-layout/main-layout';
import { AuthLayout } from './pages/auth-layout/auth-layout';
import { AdminLayout } from './pages/admin-layout/admin-layout';
import { Register } from './pages/auth-layout/register/register';
import { RoleSelection } from './pages/auth-layout/role-selection/role-selection';
import { Login } from './pages/auth-layout/login/login';
import { Usuarios } from './pages/admin-layout/usuarios/usuarios';
import { Roles } from './pages/admin-layout/roles/roles';
import { Empleados } from './pages/admin-layout/empleados/empleados';
import { Empresa } from './pages/admin-layout/empresa/empresa';
import { Proyecto } from './pages/admin-layout/proyecto/proyecto';
import { Plataforma } from './pages/main-layout/plataforma/plataforma';
import { Maquina } from './pages/main-layout/maquina/maquina';
import { Sondaje } from './pages/main-layout/sondaje/sondaje';
import { AvanceOperativoPage } from './pages/main-layout/avance-operativo/avance-operativo';
import { Desvio } from './pages/main-layout/desvio/desvio';
import { Quicklog } from './pages/main-layout/quicklog/quicklog';
import { Importacion } from './pages/main-layout/importacion/importacion';
import { Fotos } from './pages/main-layout/fotos/fotos';

export const routes: Routes = [
  {
    path: 'auth',
    component: AuthLayout,
    children: [
      { path: 'login', component: Login },
      { path: 'register', component: Register },
      { path: 'seleccionar-rol', component: RoleSelection },
      // canActivate, no redirectTo: la salida tiene que borrar la sesión.
      { path: 'logout', canActivate: [logoutGuard], children: [] },
      { path: '', redirectTo: 'login', pathMatch: 'full' },
    ],
  },
  {
    // Panel de administración. Contiene solo lo exclusivo del rol ADMIN:
    // cuentas, roles y los datos maestros del esquema `base`. Los datos de
    // operación se consultan en el layout normal, no aquí, para no acabar con
    // dos pantallas de la misma tabla.
    path: 'admin',
    component: AdminLayout,
    canActivate: [adminGuard],
    children: [
      { path: '', redirectTo: 'usuarios', pathMatch: 'full' },
      { path: 'usuarios', component: Usuarios },
      { path: 'roles', component: Roles },
      { path: 'empleados', component: Empleados },
      { path: 'empresas', component: Empresa },
      { path: 'proyectos', component: Proyecto },
    ],
  },
  {
    path: '',
    component: MainLayout,
    canActivate: [authGuard],
    children: [
      { path: 'avances', component: AvanceOperativoPage },
      { path: 'sondajes', component: Sondaje },
      { path: 'desvios', component: Desvio },
      { path: 'quicklog', component: Quicklog },
      { path: 'fotos', component: Fotos },
      { path: 'importar', component: Importacion },
      { path: 'plataformas', component: Plataforma },
      { path: 'maquinas', component: Maquina },
      // Empresas y proyectos se administran en /admin. El destino por defecto
      // es el frente de avance y no el listado de sondajes: la pregunta con la
      // que se entra a trabajar no es "qué sondajes hay" —son 72— sino "de qué
      // estoy atrasado hoy", y eso solo lo responde el frente.
      { path: '', redirectTo: 'avances', pathMatch: 'full' },
    ],
  },

  { path: '**', redirectTo: 'auth/login' },
];
