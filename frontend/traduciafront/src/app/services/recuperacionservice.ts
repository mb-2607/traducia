import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { map } from 'rxjs';
import type { Observable } from 'rxjs';

/** Cuerpo enviado al backend para solicitar el enlace de recuperación. */
interface ForgotPasswordRequestDTO {
  readonly correo: string;
}

/** Cuerpo enviado al backend para restablecer la contraseña con un token. */
interface ResetPasswordRequestDTO {
  readonly token: string;
  readonly nuevaPassword: string;
}

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

/**
 * Implementación HTTP de {@link RecuperacionService} contra `/auth`.
 */
@Injectable({
  providedIn: 'root',
})
export class RecuperacionHttpService extends RecuperacionService {
  /** Cliente HTTP utilizado para enviar las peticiones al backend. */
  private readonly http = inject(HttpClient);

  /** URL base del controlador de recuperación de contraseña. */
  private readonly urlBase = 'http://localhost:8080/auth';

  /** Solicita al backend el envío del enlace de recuperación al correo indicado. */
  override solicitarEnlace(correo: string): Observable<void> {
    const solicitud: ForgotPasswordRequestDTO = { correo };

    return this.http
      .post(`${this.urlBase}/forgot-password`, solicitud, { responseType: 'text' })
      .pipe(map(() => undefined));
  }

  /** Envía el token y la nueva contraseña al backend para completar el cambio. */
  override restablecer(token: string, password: string): Observable<void> {
    const solicitud: ResetPasswordRequestDTO = {
      token,
      nuevaPassword: password,
    };

    return this.http
      .post(`${this.urlBase}/reset-password`, solicitud, { responseType: 'text' })
      .pipe(map(() => undefined));
  }
}
