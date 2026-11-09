import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Paginaprincipalcomponent } from './components/paginaprincipalcomponent/paginaprincipalcomponent';
import { Suscripcioncomponent } from './components/suscripcioncomponent/suscripcioncomponent';

const routes: Routes = [
  { path: '', pathMatch: 'full', component: Paginaprincipalcomponent },
  { path: 'planes', component: Suscripcioncomponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
