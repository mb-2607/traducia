import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { map } from 'rxjs';
import type { Observable } from 'rxjs';
import {API_URL_BASE} from '../models/globalsettingsmodel';

/**
 * Servicio para verificar correos electrónicos y solicitar nuevos enlaces de
 * verificación contra el backend.
 *
 * Cada método emite una vez cuando el backend confirma el éxito y propaga un
 * error si la solicitud falla.
 *
 * Los endpoints actuales devuelven texto plano; las peticiones utilizan
 * `responseType: 'text'` y adaptan el resultado a `Observable<void>`.
 */
@Injectable({ providedIn: 'root' })
export class VerificacionCorreoService {
  /** Cliente HTTP utilizado para comunicarse con el backend. */
  private readonly http = inject(HttpClient);

  /** URL base de los endpoints de autenticación y verificación de correo. */
  private readonly urlBase = `${API_URL_BASE}/auth`;

  /**
   * Verifica la cuenta del usuario a partir del token recibido por enlace.
   *
   * Corresponde a `GET /auth/verify-email?token=...`.
   *
   * @param token Código único de verificación del enlace.
   * @returns Observable que emite una vez al completarse la verificación.
   */
  verificar(token: string): Observable<void> {
    const params = new HttpParams().set('token', token);

    return this.http
      .get(`${this.urlBase}/verify-email`, { params, responseType: 'text' })
      .pipe(map(() => undefined));
  }

  /**
   * Solicita un nuevo enlace de verificación para el correo indicado.
   *
   * Corresponde a `POST /auth/resend-verification` con `{ correo }`.
   *
   * @param correo Dirección de correo electrónico asociada a la cuenta.
   * @returns Observable que emite una vez al confirmarse el reenvío.
   */
  reenviar(correo: string): Observable<void> {
    return this.http
      .post(`${this.urlBase}/resend-verification`, { correo }, { responseType: 'text' })
      .pipe(map(() => undefined));
  }
}
