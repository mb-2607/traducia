import { Injectable } from '@angular/core';
import { of } from 'rxjs';
import type { Observable } from 'rxjs';
import type { Idioma } from '../models/idiomamodel';

/**
 * Catálogo de idiomas soportados por la aplicación.
 *
 * Cada idioma incluye su identificador (`id`), el nombre que se muestra
 * al usuario (`nombre`) y el código de voz (`voz`), usado para la
 * síntesis de voz con la variante regional correspondiente (por ejemplo, `es-CO`).
 * El catálogo se define localmente mientras se integra con el backend.
 */
const IDIOMAS: Idioma[] = [
  { id: 'es', nombre: 'Español', voz: 'es-CO' },
  { id: 'en', nombre: 'Inglés', voz: 'en-US' },
  { id: 'fr', nombre: 'Francés', voz: 'fr-FR' },
  { id: 'pt', nombre: 'Portugués', voz: 'pt-BR' },
  { id: 'de', nombre: 'Alemán', voz: 'de-DE' },
  { id: 'it', nombre: 'Italiano', voz: 'it-IT' },
];

/**
 * Servicio de idiomas.
 *
 * Provee la lista de idiomas disponibles para traducir. Los datos son
 * locales (constante `IDIOMAS`), pero se exponen como `Observable` para
 * que, si más adelante se obtienen desde el backend, los componentes
 * que lo consumen mantengan la misma forma de acceder al catálogo.
 *
 * Está disponible globalmente (`providedIn: 'root'`).
 */
@Injectable({ providedIn: 'root' })
export class IdiomaService {
  /** Catálogo de idiomas que entrega el servicio. */
  private readonly idiomas = IDIOMAS;

  /**
   * Obtiene la lista de idiomas disponibles.
   *
   * @returns Observable que emite una sola vez el arreglo de idiomas y se completa.
   */
  getListaIdioma(): Observable<Idioma[]> {
    return of(this.idiomas);
  }
}
