import { Component, signal } from '@angular/core';

/**
 * Componente raíz de la aplicación TraducIA.
 *
 * Actúa como punto de entrada visual de la aplicación y proporciona
 * la estructura principal sobre la que se renderizan los demás componentes.
 */
@Component({
  selector: 'app-root',
  standalone: false,
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  protected readonly title = signal('traduciafront');
}
