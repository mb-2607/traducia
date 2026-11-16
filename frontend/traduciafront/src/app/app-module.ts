import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { Navbarcomponent } from './components/navbarcomponent/navbarcomponent';
import { Suscripcioncomponent } from './components/suscripcioncomponent/suscripcioncomponent';
import { AsideUserComponent } from './components/asideusercomponent/asideusercomponent';
import { Userpagecomponent } from './components/userpagecomponent/userpagecomponent';
import { Nuevatraduccioncomponent } from './components/nuevatraduccioncomponent/nuevatraduccioncomponent';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Idiomaselectorcomponent } from './components/idiomaselectorcomponent/idiomaselectorcomponent';
import { Archivoselectorcomponent } from './components/archivoselectorcomponent/archivoselectorcomponent';
import { Microfonocomponent } from './components/microfonocomponent/microfonocomponent';

@NgModule({
  declarations: [
    App,
    AsideUserComponent,
    Userpagecomponent,
    Nuevatraduccioncomponent,
    Idiomaselectorcomponent,
    Archivoselectorcomponent,
    Microfonocomponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    Navbarcomponent,
    Suscripcioncomponent,
    FormsModule,
    ReactiveFormsModule,
  ],
  providers: [provideBrowserGlobalErrorListeners()],
  bootstrap: [App],
})
export class AppModule {}
