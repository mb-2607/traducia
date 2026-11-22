import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import type { Observable } from 'rxjs';
import { API_BASE_URL } from '../api-config';
import type {
  SolicitudTraduccion,
  SolicitudTraduccionResponseDTO,
} from '../models/traduccionmodel';
import { AuthService } from './authservice';
import { TraduccionService } from './traduccionservice';

/** Implementa el contrato existente sin cambiar sus consumidores. */
@Injectable({
  providedIn: 'root',
})
export class TraduccionHttpService extends TraduccionService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly baseUrl = inject(API_BASE_URL).replace(/\/+$/, '');

  /** Traduce el texto revisado con los proveedores del plan autenticado. */
  override traducir(solicitud: SolicitudTraduccion): Observable<SolicitudTraduccionResponseDTO> {
    const token = this.auth.getJwt();
    const headers = token
      ? new HttpHeaders({ Authorization: `Bearer ${token}` })
      : new HttpHeaders();
    return this.http.post<SolicitudTraduccionResponseDTO>(
      `${this.baseUrl}/traductor/traducir`,
      solicitud,
      { headers },
    );
  }
}
