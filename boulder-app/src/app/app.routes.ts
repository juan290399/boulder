import { Routes } from '@angular/router';
import { adminGuard, authGuard, logoutGuard } from './guards/auth';
import { MainLayout } from './pages/main-layout/main-layout';
import { AuthLayout } from './pages/auth-layout/auth-layout';
import { AdminLayout } from './pages/admin-layout/admin-layout';
import { Login } from './pages/auth-layout/login/login';

// Las páginas se cargan al entrar en ellas; el bundle inicial lleva solo los
// layouts y el inicio de sesión.
export const routes: Routes = [
  {
    path: 'auth',
    component: AuthLayout,
    children: [
      { path: 'login', component: Login },
      { path: 'register', loadComponent: () => import('./pages/auth-layout/register/register').then(m => m.Register) },
      { path: 'seleccionar-rol', loadComponent: () => import('./pages/auth-layout/role-selection/role-selection').then(m => m.RoleSelection) },
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
      { path: 'usuarios', loadComponent: () => import('./pages/admin-layout/usuarios/usuarios').then(m => m.Usuarios) },
      { path: 'roles', loadComponent: () => import('./pages/admin-layout/roles/roles').then(m => m.Roles) },
      { path: 'empleados', loadComponent: () => import('./pages/admin-layout/empleados/empleados').then(m => m.Empleados) },
      { path: 'empresas', loadComponent: () => import('./pages/admin-layout/empresa/empresa').then(m => m.Empresa) },
      { path: 'proyectos', loadComponent: () => import('./pages/admin-layout/proyecto/proyecto').then(m => m.Proyecto) },
      { path: 'diccionarios', loadComponent: () => import('./pages/admin-layout/diccionarios/diccionarios').then(m => m.Diccionarios) },
    ],
  },
  {
    path: '',
    component: MainLayout,
    canActivate: [authGuard],
    children: [
      { path: 'avances', loadComponent: () => import('./pages/main-layout/avance-operativo/avance-operativo').then(m => m.AvanceOperativoPage) },
      { path: 'sondajes', loadComponent: () => import('./pages/main-layout/sondaje/sondaje').then(m => m.Sondaje) },
      { path: 'desvios', loadComponent: () => import('./pages/main-layout/desvio/desvio').then(m => m.Desvio) },
      { path: 'quicklog', loadComponent: () => import('./pages/main-layout/quicklog/quicklog').then(m => m.Quicklog) },
      { path: 'fotos', loadComponent: () => import('./pages/main-layout/fotos/fotos').then(m => m.Fotos) },
      { path: 'importar', loadComponent: () => import('./pages/main-layout/importacion/importacion').then(m => m.Importacion) },
      { path: 'reportes', loadComponent: () => import('./pages/main-layout/reporte-diario/reporte-diario').then(m => m.ReporteDiarioPage) },
      { path: 'reportes/periodo', loadComponent: () => import('./pages/main-layout/reporte-periodo/reporte-periodo').then(m => m.ReportePeriodoPage) },
      { path: 'plataformas', loadComponent: () => import('./pages/main-layout/plataforma/plataforma').then(m => m.Plataforma) },
      { path: 'maquinas', loadComponent: () => import('./pages/main-layout/maquina/maquina').then(m => m.Maquina) },
      { path: 'mi-cuenta', loadComponent: () => import('./pages/main-layout/mi-cuenta/mi-cuenta').then(m => m.MiCuenta) },
      // Empresas y proyectos se administran en /admin. El destino por defecto
      // es el frente de avance y no el listado de sondajes: la pregunta con la
      // que se entra a trabajar no es "qué sondajes hay" —son 72— sino "de qué
      // estoy atrasado hoy", y eso solo lo responde el frente.
      { path: '', redirectTo: 'avances', pathMatch: 'full' },
    ],
  },

  { path: '**', redirectTo: 'auth/login' },
];
