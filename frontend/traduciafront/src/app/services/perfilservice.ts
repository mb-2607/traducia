import type { Observable } from 'rxjs';
import type { GuardarPerfil, PerfilUsuario } from '../models/perfilmodel';

/**
 * Contrato de acceso al perfil de la cuenta autenticada.
 * La implementación HTTP se registrará cuando estén definidos los endpoints.
 * Cada método emite una respuesta y completa, o propaga el error de la operación.
 * El servidor identifica la cuenta a partir de la sesión y valida los archivos.
 */
export abstract class PerfilService {
  /** Obtiene los datos actuales, incluida la URL de la imagen guardada. */
  abstract obtener(): Observable<PerfilUsuario>;

  /** Guarda los cambios y devuelve el perfil definitivo confirmado por el servidor. */
  abstract guardar(solicitud: GuardarPerfil): Observable<PerfilUsuario>;
}
