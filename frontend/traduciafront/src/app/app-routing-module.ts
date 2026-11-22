import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import type { Routes } from '@angular/router';
import { Suscripcioncomponent } from './components/suscripcioncomponent/suscripcioncomponent';
import { Userpagecomponent } from './components/userpagecomponent/userpagecomponent';
import { Nuevatraduccioncomponent } from './components/nuevatraduccioncomponent/nuevatraduccioncomponent';
import { Historialtraduccionescomponent } from './components/historialtraduccionescomponent/historialtraduccionescomponent';
import { Bibliotecatraduccionescomponent } from './components/bibliotecatraduccionescomponent/bibliotecatraduccionescomponent';
import { Loggincomponent } from './components/loggincomponent/loggincomponent';
import { Listausuarioscomponent } from './components/listausuarioscomponent/listausuarioscomponent';
import { Adminpagecomponent } from './components/adminpagecomponent/adminpagecomponent';
import { Auditoriacomponent } from './components/auditoriacomponent/auditoriacomponent';
import { Perfilcomponent } from './components/perfilcomponent/perfilcomponent';
import { Recuperarcontrasenacomponent } from './components/recuperarcontrasenacomponent/recuperarcontrasenacomponent';
import { Landingpagecomponent } from './components/landingpagecomponent/landingpagecomponent';
import { Noencontradocomponent } from './components/noencontradocomponent/noencontradocomponent';
import { administradorGuard } from './guards/administradorguard-guard';
import { autenticacionGuard } from './guards/autenticacionguard-guard';
import { visitanteGuard } from './guards/visitanteguard-guard';
import { Verificarcorreocomponent } from './components/verificarcorreocomponent/verificarcorreocomponent';

/**
 * Configuración de navegación de TraducIA.
 *
 * El acceso inicial dirige al login. La landing y las pantallas
 * de recuperación de contraseña permanecen disponibles públicamente.
 *
 * El perfil, los planes y el dashboard requieren autenticación.
 * Las páginas administrativas requieren el rol ADMIN.
 *
 * Los guards de rutas hijas comprueban también la navegación
 * entre las distintas secciones de cada área protegida.
 *
 * La última ruta presenta la página no encontrada cuando
 * ninguna dirección anterior coincide.
 */
const routes: Routes = [
  {
    path: '',
    redirectTo: 'landing',
    pathMatch: 'full',
  },
  {
    path: 'landing',
    component: Landingpagecomponent,
    title: 'TraducIA',
  },
  {
    path: 'login',
    component: Loggincomponent,
    canActivate: [visitanteGuard],
    title: 'Iniciar sesión | TraducIA',
  },
  {
    path: 'recuperar-contrasena',
    component: Recuperarcontrasenacomponent,
    data: { modo: 'solicitar' },
    title: 'Recuperar acceso | TraducIA',
  },
  {
    path: 'restablecer-contrasena',
    component: Recuperarcontrasenacomponent,
    data: { modo: 'restablecer' },
    title: 'Nueva contraseña | TraducIA',
  },
  {
    path: 'perfil',
    component: Perfilcomponent,
    canActivate: [autenticacionGuard],
    title: 'Perfil | TraducIA',
  },
  {
    path: 'planes',
    component: Suscripcioncomponent,
    canActivate: [autenticacionGuard],
    title: 'Planes | TraducIA',
  },
  {
    path: 'verificar-correo',
    component: Verificarcorreocomponent,
    title: 'Verificar correo | TraducIA',
  },
  {
    path: 'dashboard',
    component: Userpagecomponent,
   // canActivate: [autenticacionGuard],
   // canActivateChild: [autenticacionGuard],
    children: [
      {
        path: '',
        redirectTo: 'new',
        pathMatch: 'full',
      },
      {
        path: 'new',
        component: Nuevatraduccioncomponent,
        title: 'Nueva traducción | TraducIA',
      },
      {
        path: 'historial',
        component: Historialtraduccionescomponent,
        title: 'Historial | TraducIA',
      },
      {
        path: 'biblioteca/archivos',
        component: Bibliotecatraduccionescomponent,
        data: { tipo: 'archivo' },
        title: 'Archivos | TraducIA',
      },
      {
        path: 'biblioteca/texto',
        component: Bibliotecatraduccionescomponent,
        data: { tipo: 'texto' },
        title: 'Textos | TraducIA',
      },
      {
        path: 'biblioteca',
        pathMatch: 'full',
        component: Bibliotecatraduccionescomponent,
        title: 'Biblioteca | TraducIA',
      },
    ],
  },
  {
    path: 'admin',
    canActivate: [administradorGuard],
    canActivateChild: [administradorGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        component: Adminpagecomponent,
        title: 'Administración | TraducIA',
      },
      {
        path: 'usuarios',
        component: Listausuarioscomponent,
        title: 'Usuarios | TraducIA',
      },
      {
        path: 'auditoria',
        component: Auditoriacomponent,
        title: 'Auditoría | TraducIA',
      },
    ],
  },
  {
    path: '**',
    component: Noencontradocomponent,
    title: 'Página no encontrada | TraducIA',
  },
];

/**
 * Módulo de enrutamiento principal.
 *
 * Registra las páginas públicas y protegidas de la aplicación.
 * Exporta RouterModule para utilizar los enlaces de navegación
 * y presentar las vistas mediante router-outlet.
 */
@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
// skipcq: JS-0327
export class AppRoutingModule {}
