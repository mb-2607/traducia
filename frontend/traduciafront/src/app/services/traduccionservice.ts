import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import type { Observable } from 'rxjs';
import type {
  PageResponse,
  SolicitudTraduccion,
  SolicitudTraduccionResponseDTO,
} from '../models/traduccionmodel';
import {API_URL_BASE} from '../models/globalsettingsmodel';
import { AuthService } from './authservice';

/**
 * Servicio HTTP encargado de ejecutar traducciones y consultar el historial
 * de solicitudes del usuario autenticado.
 */
@Injectable({
  providedIn: 'root',
})
export class TraduccionService {
  /** Cliente HTTP para comunicarse con el backend. */
  private readonly cliente = inject(HttpClient);

  /** Servicio de autenticación del que se obtiene el JWT activo. */
  private readonly authService = inject(AuthService);

  /** URL base del controlador de traducciones en el backend. */
  private readonly URL_BASE = `${API_URL_BASE}/traductor`;

  /**
   * Construye las cabeceras JSON incluyendo el token Bearer si hay sesión activa.
   */
  private crearHeaders(): HttpHeaders {
    let headers = new HttpHeaders({
      'Content-Type': 'application/json',
      accept: 'application/json',
    });

    const token = this.authService.getJwt();
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }

    return headers;
  }

  /**
   * Solicita una traducción para los idiomas indicados.
   *
   * @param solicitud Texto original y códigos de los idiomas.
   * @returns Respuesta con los resultados de los proveedores.
   */
  traducir(solicitud: SolicitudTraduccion): Observable<SolicitudTraduccionResponseDTO> {
    return this.cliente.post<SolicitudTraduccionResponseDTO>(
      `${this.URL_BASE}/traducir`,
      solicitud,
      { headers: this.crearHeaders() },
    );
  }

  /**
   * Obtiene el historial paginado de traducciones del usuario autenticado.
   *
   * @param page Número de página (inicia en 0).
   * @param size Cantidad de registros por página.
   * @returns Página con las solicitudes registradas.
   */
  obtenerHistorial(
    page = 0,
    size = 10,
  ): Observable<PageResponse<SolicitudTraduccionResponseDTO>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.cliente.get<PageResponse<SolicitudTraduccionResponseDTO>>(
      `${this.URL_BASE}/historial`,
      { headers: this.crearHeaders(), params },
    );
  }

  /**
   * Obtiene el detalle completo de una solicitud de traducción por su identificador.
   *
   * @param id Identificador de la solicitud.
   * @returns Detalle de la solicitud y sus resultados por proveedor.
   */
  obtenerPorId(id: number): Observable<SolicitudTraduccionResponseDTO> {
    return this.cliente.get<SolicitudTraduccionResponseDTO>(`${this.URL_BASE}/${id}`, {
      headers: this.crearHeaders(),
    });
  }
}
