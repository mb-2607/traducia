import {Injectable} from '@angular/core';
import {Idioma} from '../models/idiomamodel';
import {Observable, of} from 'rxjs';

const IDIOMAS: Idioma[] = [
  { id: 'es', nombre: 'Español', voz: 'es-CO' },
  { id: 'en', nombre: 'Inglés', voz: 'en-US' },
  { id: 'fr', nombre: 'Francés', voz: 'fr-FR' },
  { id: 'pt', nombre: 'Portugués', voz: 'pt-BR' },
  { id: 'de', nombre: 'Alemán', voz: 'de-DE' },
  { id: 'it', nombre: 'Italiano', voz: 'it-IT' },
];

@Injectable({
  providedIn: 'root'
})
export class IdiomaService {

  getListaIdioma(): Observable<Idioma[]> {
    return of(IDIOMAS);
  }

}
