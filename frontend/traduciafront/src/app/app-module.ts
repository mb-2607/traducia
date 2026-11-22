import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { Navbarcomponent } from './components/navbarcomponent/navbarcomponent';
import { Suscripcioncomponent } from './components/suscripcioncomponent/suscripcioncomponent';
import { AsideUserComponent } from './components/asideusercomponent/asideusercomponent';
import { Userpagecomponent } from './components/userpagecomponent/userpagecomponent';
import { Nuevatraduccioncomponent } from './components/nuevatraduccioncomponent/nuevatraduccioncomponent';
import { Idiomaselectorcomponent } from './components/idiomaselectorcomponent/idiomaselectorcomponent';
import { Archivoselectorcomponent } from './components/archivoselectorcomponent/archivoselectorcomponent';
import { Historialtraduccionescomponent } from './components/historialtraduccionescomponent/historialtraduccionescomponent';
import { Bibliotecatraduccionescomponent } from './components/bibliotecatraduccionescomponent/bibliotecatraduccionescomponent';
import { Adminpagecomponent } from './components/adminpagecomponent/adminpagecomponent';
import { Loggincomponent } from './components/loggincomponent/loggincomponent';
import { Listausuarioscomponent } from './components/listausuarioscomponent/listausuarioscomponent';
import { Auditoriacomponent } from './components/auditoriacomponent/auditoriacomponent';
import { Perfilcomponent } from './components/perfilcomponent/perfilcomponent';
import { Recuperarcontrasenacomponent } from './components/recuperarcontrasenacomponent/recuperarcontrasenacomponent';
import { Landingpagecomponent } from './components/landingpagecomponent/landingpagecomponent';
import { Noencontradocomponent } from './components/noencontradocomponent/noencontradocomponent';
import { Terminocomponent } from './components/terminocomponent/terminocomponent';
import { Verificarcorreocomponent } from './components/verificarcorreocomponent/verificarcorreocomponent';
import { authInterceptor } from './interceptors/authinterceptor';
import { AuditoriaHttpService, AuditoriaService } from './services/auditoriaservice';
import { PerfilHttpService, PerfilService } from './services/perfilservice';
import { RecuperacionHttpService, RecuperacionService } from './services/recuperacionservice';
import { UsuariosAdminHttpService, UsuariosAdminService } from './services/usuariosadminservice';

/**
 * Módulo principal de la aplicación.
 *
 * Declara los componentes de las páginas públicas, el área de usuario
 * y el panel de administración.
 *
 * Importa las herramientas de navegación y formularios, registra
 * el cliente HTTP utilizado por los servicios y establece
 * el componente raíz de la aplicación.
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
    Listausuarioscomponent,
    Auditoriacomponent,
    Perfilcomponent,
    Recuperarcontrasenacomponent,
    Landingpagecomponent,
    Noencontradocomponent,
    Terminocomponent,
    Verificarcorreocomponent,
  ],
  imports: [BrowserModule, AppRoutingModule, FormsModule, ReactiveFormsModule],
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(withInterceptors([authInterceptor])),
    { provide: RecuperacionService, useExisting: RecuperacionHttpService },
    { provide: PerfilService, useExisting: PerfilHttpService },
    { provide: UsuariosAdminService, useExisting: UsuariosAdminHttpService },
    { provide: AuditoriaService, useExisting: AuditoriaHttpService },
  ],
  bootstrap: [App],
})
// skipcq: JS-0327
export class AppModule {}
