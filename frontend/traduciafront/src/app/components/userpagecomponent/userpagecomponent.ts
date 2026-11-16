import { Component } from '@angular/core';

/**
 * Componente encargado de representar la página principal del usuario.
 *
 * Proporciona la estructura base de la interfaz de usuario, integrando
 * el componente de navegación lateral y un área de contenido dinámico
 * mediante Angular Router.
 *
 * Las vistas correspondientes a las diferentes funcionalidades del
 * usuario se renderizan dentro del router-outlet, mientras que el
 * componente lateral permanece disponible como elemento de navegación.
 */
@Component({
  selector: 'app-userpagecomponent',
  standalone: false,
  styleUrl: './userpagecomponent.css',
  templateUrl: './userpagecomponent.html',
})
export class Userpagecomponent {

}
