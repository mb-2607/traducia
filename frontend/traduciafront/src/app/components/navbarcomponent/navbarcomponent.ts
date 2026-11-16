
/**
 * Componente reutilizable encargado de representar la barra de navegación
 * principal de TraducIA.
 *
 * Muestra el logotipo de la aplicación y proporciona accesos directos
 * a las secciones de gestión de planes y perfil del usuario.
 *
 * Permite personalizar la imagen del logotipo y las rutas de navegación
 * mediante propiedades de entrada, facilitando su reutilización
 * en diferentes vistas de la aplicación.
 *
 * Utiliza la estrategia OnPush de detección de cambios para optimizar
 * el rendimiento y evitar comprobaciones innecesarias.
 */
import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

@Component({
  selector: 'app-navbarcomponent',
  standalone: false,
  templateUrl: './navbarcomponent.html',
  styleUrls: ['./navbarcomponent.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Navbarcomponent {

  /** Ruta de la imagen utilizada como logotipo de la aplicación. */
  @Input() logoSrc = 'assets/logoempresa.png';

  /** Ruta de navegación hacia la sección de planes. */
  @Input() planRoute = '/planes';

  /** Ruta de navegación hacia la sección de perfil del usuario. */
  @Input() profileRoute = '/perfil';
}
