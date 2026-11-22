import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import type { Observable } from 'rxjs';
import { API_BASE_URL } from '../api-config';
import type { TranscripcionResponse } from '../../../../../../../../../Downloads/Traducia_Front_Documentos_Audio/Traducia_Front_Documentos_Audio/src/app/models/transcripcionmodel';
import { AuthService } from './authservice';

/** Envía audio al backend; la clave de Groq permanece en el servidor. */
@Injectable({ providedIn: 'root' })
export class TranscripcionService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly baseUrl = inject(API_BASE_URL).replace(/\/+$/, '');

  /** Omite idioma cuando se desea detección automática. */
  transcribir(archivo: File, idioma?: string): Observable<TranscripcionResponse> {
    const cuerpo = new FormData();
    cuerpo.append('archivo', archivo, archivo.name);
    const params: Record<string, string> = {};
    const codigo = idioma?.trim();
    if (codigo) {
      params['idioma'] = codigo;
    }
    const token = this.auth.getJwt();
    const headers = token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
    return this.http.post<TranscripcionResponse>(
      `${this.baseUrl}/traductor/transcribir`, cuerpo, { headers, params },
    );
  }
}
