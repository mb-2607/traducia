import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Suscripcioncomponent } from './components/suscripcioncomponent/suscripcioncomponent';
import {Userpagecomponent} from './components/userpagecomponent/userpagecomponent';
import {Nuevatraduccioncomponent} from './components/nuevatraduccioncomponent/nuevatraduccioncomponent';

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

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
