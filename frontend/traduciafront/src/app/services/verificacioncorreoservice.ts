import type { Observable } from 'rxjs';

/**
 * Contrato para verificar correos y solicitar nuevos enlaces.
 *
 * Cada método debe emitir una vez cuando el backend confirme
 * el éxito y propagar un error si la solicitud falla.
 *
 * Los endpoints actuales devuelven texto. La implementación HTTP
 * deberá utilizar responseType: 'text' y adaptar el resultado
 * a Observable<void>.
 */
export abstract class VerificacionCorreoService {
  /** Corresponde a GET /auth/verify-email?token=... */
  abstract verificar(token: string): Observable<void>;

  /** Corresponde a POST /auth/resend-verification con { correo }. */
  abstract reenviar(correo: string): Observable<void>;
}
