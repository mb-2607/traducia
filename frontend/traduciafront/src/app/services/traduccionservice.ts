import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import type { Observable } from 'rxjs';
import type {
  SolicitudTraduccion,
  SolicitudTraduccionResponseDTO,
} from '../models/traduccionmodel';

/**
 * Servicio de traducción que comunica las solicitudes del frontend con el
 * backend de TraducIA.
 *
 * Envía el texto y los idiomas de origen y destino hacia el endpoint
 * protegido de traducción y devuelve los resultados por proveedor.
 */
@Injectable({ providedIn: 'root' })
export class TraduccionService {
  /** Cliente HTTP utilizado para realizar las peticiones al backend. */
  private readonly http = inject(HttpClient);

  /** URL del endpoint de traducción en el backend. */
  private readonly url = 'http://localhost:8080/traductor/traducir';

  /**
   * Solicita una traducción para los idiomas indicados.
   *
   * @param solicitud Texto original y códigos de los idiomas.
   * @returns Respuesta con los resultados de los proveedores.
   */
  traducir(solicitud: SolicitudTraduccion): Observable<SolicitudTraduccionResponseDTO> {
    return this.http.post<SolicitudTraduccionResponseDTO>(this.url, solicitud);
  }
}
