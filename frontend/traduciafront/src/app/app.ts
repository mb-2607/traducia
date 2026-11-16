import { Component, signal } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';

/**
 * Componente raíz de la aplicación TraducIA.
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
  rutaActual: string = '';

  constructor(private router: Router) {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      this.rutaActual = event.urlAfterRedirects;
    });

    this.rutaActual = this.router.url;
  }

  esRutaPublica(): boolean {
    return this.rutaActual === '/' || this.rutaActual === '' || this.rutaActual.startsWith('/?');
  }
}
