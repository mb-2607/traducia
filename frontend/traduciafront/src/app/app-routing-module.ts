import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Suscripcioncomponent } from './components/suscripcioncomponent/suscripcioncomponent';
import {Userpagecomponent} from './components/userpagecomponent/userpagecomponent';
import {Nuevatraduccioncomponent} from './components/nuevatraduccioncomponent/nuevatraduccioncomponent';

/**
 * Configuración de las rutas principales de la aplicación.
 *
 * Define las páginas accesibles mediante la URL y las rutas hijas
 * correspondientes al dashboard.
 */
const routes: Routes = [
  //{TODO: Esto es lo de caballo
    //path: '',
    //pathMatch: 'full',
    //component: Landingpagecomponent
  //},
  {
    path: 'planes',
    component: Suscripcioncomponent
  },
  {
    path: 'dashboard',
    component: Userpagecomponent,
    children: [
      {
        path: '',
        redirectTo: 'new',
        pathMatch: 'full'
      },
      {
        path: 'new',
        component: Nuevatraduccioncomponent
      }
    ]
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
export class AppRoutingModule {}
