import {Injectable} from '@angular/core';
import {Idioma} from '../models/idiomamodel';
import {Observable, of} from 'rxjs';

/**
 * Catálogo de idiomas soportados por la aplicación.
 *
 * Cada idioma incluye su identificador (`id`), el nombre que se muestra
 * al usuario (`nombre`) y el código de voz (`voz`), usado para la
 * síntesis de voz con la variante regional correspondiente (por ejemplo, `es-CO`).
 * Esto es así mientras se implementa en el Back.
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
 * que lo consumen no tengan que cambiar.
 *
 * Está disponible globalmente (`providedIn: 'root'`).
 */
@Injectable({
  providedIn: 'root'
})
export class IdiomaService {

  /**
   * Obtiene la lista de idiomas disponibles.
   *
   * @returns Observable que emite una sola vez el arreglo de idiomas y se completa.
   */
  getListaIdioma(): Observable<Idioma[]> {
    return of(IDIOMAS);
  }

}
