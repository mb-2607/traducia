import type { Observable } from 'rxjs';
import type {
  SolicitudTraduccion,
  SolicitudTraduccionResponseDTO,
} from '../models/traduccionmodel';

/**
 * Contrato del servicio de traducción.
 *
 * Define la comunicación que necesita el frontend.
 * La implementación HTTP se proporcionará durante
 * la integración con el backend.
 */
export abstract class TraduccionService {
  /**
   * Solicita una traducción para los idiomas indicados.
   *
   * @param solicitud Texto original y códigos de los idiomas.
   * @returns Respuesta con los resultados de los proveedores.
   */
  abstract traducir(solicitud: SolicitudTraduccion): Observable<SolicitudTraduccionResponseDTO>;
}
