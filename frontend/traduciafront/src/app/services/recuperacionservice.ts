import type { Observable } from 'rxjs';

/**
 * Contrato de recuperación de acceso.
 * Los endpoints deben responder sin revelar si un correo tiene cuenta.
 * El servidor valida la vigencia y el uso único del token de recuperación.
 * Cada método emite una respuesta y completa, o propaga el error recibido.
 */
export abstract class RecuperacionService {
  /** Solicita el envío del enlace sin confirmar la existencia de la cuenta. */
  abstract solicitarEnlace(correo: string): Observable<void>;

  /** Establece la contraseña después de que el servidor valide el token. */
  abstract restablecer(token: string, password: string): Observable<void>;
}
