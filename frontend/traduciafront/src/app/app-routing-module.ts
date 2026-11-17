import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Suscripcioncomponent } from './components/suscripcioncomponent/suscripcioncomponent';
import { Userpagecomponent } from './components/userpagecomponent/userpagecomponent';
import { Nuevatraduccioncomponent } from './components/nuevatraduccioncomponent/nuevatraduccioncomponent';
import { Historialtraduccionescomponent } from './components/historialtraduccionescomponent/historialtraduccionescomponent';
import { Bibliotecatraduccionescomponent } from './components/bibliotecatraduccionescomponent/bibliotecatraduccionescomponent';
import { Loggincomponent } from './components/loggincomponent/loggincomponent';

/**
 * Configuración de las rutas principales de la aplicación.
 *
 * Define las páginas accesibles mediante la URL y las rutas hijas
 * correspondientes al dashboard.
 */
const routes: Routes = [
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full',
  },
  {
    path: 'login',
    component: Loggincomponent,
  },
  {
    path: 'planes',
    component: Suscripcioncomponent,
  },
  {
    path: 'dashboard',
    component: Userpagecomponent,
    children: [
      {
        path: '',
        redirectTo: 'new',
        pathMatch: 'full',
      },
      {
        path: 'new',
        component: Nuevatraduccioncomponent,
      },
      {
        path: 'historial',
        component: Historialtraduccionescomponent,
      },
      {
        path: 'biblioteca/archivos',
        component: Bibliotecatraduccionescomponent,
        data: { tipo: 'archivo' },
      },
      {
        path: 'biblioteca/texto',
        component: Bibliotecatraduccionescomponent,
        data: { tipo: 'texto' },
      },
      {
        path: 'biblioteca',
        pathMatch: 'full',
        component: Bibliotecatraduccionescomponent,
      },
    ],
  },
];

/**
 * Módulo de enrutamiento principal de la aplicación.
 *
 * Define las rutas disponibles y establece qué componentes deben
 * mostrarse según la URL solicitada. También configura las rutas
 * anidadas del dashboard y exporta RouterModule para permitir
 * la navegación dentro de la aplicación.
 */
@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})

// skipcq: JS-0327
export class AppRoutingModule {}
