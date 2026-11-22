import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { map } from 'rxjs';
import type { Observable } from 'rxjs';
import type { Idioma, IdiomaResponseDTO } from '../models/idiomamodel';
import { API_URL_BASE } from './authservice';

/** Etiquetas de voz por defecto según el código base del idioma. */
const VOCES_POR_CODIGO: Readonly<Record<string, string>> = {
  es: 'es-CO',
  en: 'en-US',
  fr: 'fr-FR',
  pt: 'pt-BR',
  de: 'de-DE',
  it: 'it-IT',
};

/**
 * Servicio de idiomas que obtiene el catálogo disponible desde el backend
 * y lo adapta al modelo utilizado por los componentes del frontend.
 *
 * Está disponible globalmente (`providedIn: 'root'`).
 */
@Injectable({ providedIn: 'root' })
export class IdiomaService {
  /** Cliente HTTP utilizado para consultar los idiomas del backend. */
  private readonly http = inject(HttpClient);

  /** URL del endpoint que lista los idiomas disponibles. */
  private readonly url = `${API_URL_BASE}/idiomas`;

  /**
   * Obtiene la lista de idiomas disponibles desde el backend y asigna una
   * etiqueta de voz para cada uno.
   *
   * @returns Observable que emite el arreglo de {@link Idioma} adaptado.
   */
  getListaIdioma(): Observable<Idioma[]> {
    return this.http.get<IdiomaResponseDTO[]>(this.url).pipe(
      map((idiomas) =>
        idiomas.map((idioma) => {
          const codigoNormalizado = idioma.codigo.trim().toLowerCase();
          const voz =
            VOCES_POR_CODIGO[codigoNormalizado] ??
            (codigoNormalizado.length > 0 ? codigoNormalizado : 'es-CO');

          return {
            id: idioma.codigo,
            nombre: idioma.nombre,
            voz,
          };
        }),
      ),
    );
  }
}
