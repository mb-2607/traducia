import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { map } from 'rxjs';
import type { Observable } from 'rxjs';
import { API_URL_BASE } from './authservice';

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
 * Servicio HTTP de recuperación de acceso contra `/auth`.
 *
 * Los endpoints responden sin revelar si un correo tiene cuenta y el servidor
 * valida la vigencia y el uso único del token de recuperación. Cada método
 * emite una vez al completarse o propaga el error recibido.
 *
 * Está disponible globalmente (`providedIn: 'root'`).
 */
@Injectable({
  providedIn: 'root',
})
export class RecuperacionService {
  /** Cliente HTTP utilizado para enviar las peticiones al backend. */
  private readonly http = inject(HttpClient);

  /** URL base del controlador de recuperación de contraseña. */
  private readonly urlBase = `${API_URL_BASE}/auth`;

  /** Solicita al backend el envío del enlace de recuperación al correo indicado. */
  solicitarEnlace(correo: string): Observable<void> {
    const solicitud: ForgotPasswordRequestDTO = { correo };

    return this.http
      .post(`${this.urlBase}/forgot-password`, solicitud, { responseType: 'text' })
      .pipe(map(() => undefined));
  }

  /** Envía el token y la nueva contraseña al backend para completar el cambio. */
  restablecer(token: string, password: string): Observable<void> {
    const solicitud: ResetPasswordRequestDTO = {
      token,
      nuevaPassword: password,
    };

    return this.http
      .post(`${this.urlBase}/reset-password`, solicitud, { responseType: 'text' })
      .pipe(map(() => undefined));
  }
}
