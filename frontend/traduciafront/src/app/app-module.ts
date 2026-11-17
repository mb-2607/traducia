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
import { Historialtraduccionescomponent } from './components/historialtraduccionescomponent/historialtraduccionescomponent';
import { Bibliotecatraduccionescomponent } from './components/bibliotecatraduccionescomponent/bibliotecatraduccionescomponent';
import { Adminpagecomponent } from './components/adminpagecomponent/adminpagecomponent';
import { Loggincomponent } from './components/loggincomponent/loggincomponent';

/**
 * Módulo principal de la aplicación.
 *
 * Se encarga de declarar los componentes que forman parte de la aplicación,
 * importar los módulos necesarios para su funcionamiento, registrar los
 * proveedores globales e indicar el componente raíz desde el que se inicia
 * la aplicación.
 */
@NgModule({
  declarations: [
    App,
    AsideUserComponent,
    Userpagecomponent,
    Nuevatraduccioncomponent,
    Idiomaselectorcomponent,
    Archivoselectorcomponent,
    Navbarcomponent,
    Suscripcioncomponent,
    Historialtraduccionescomponent,
    Bibliotecatraduccionescomponent,
    Adminpagecomponent,
    Loggincomponent,
  ],
  imports: [BrowserModule, AppRoutingModule, FormsModule, ReactiveFormsModule],
  providers: [provideBrowserGlobalErrorListeners()],
  bootstrap: [App],
})
export class AppModule {}
