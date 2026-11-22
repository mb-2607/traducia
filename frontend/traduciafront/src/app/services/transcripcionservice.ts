import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import type { Observable } from 'rxjs';
import type { TranscripcionResponse } from '../models/transcripcionmodel';
import {API_URL_BASE} from '../models/globalsettingsmodel';

import { AuthService } from './authservice';

/** Envía audio al backend; la clave de Groq permanece en el servidor. */
@Injectable({ providedIn: 'root' })
export class TranscripcionService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly urlTranscribir = `${API_URL_BASE.replace(/\/+$/, '')}/traductor/transcribir`;

  /** Omite idioma cuando se desea detección automática. */
  transcribir(archivo: File, idioma?: string): Observable<TranscripcionResponse> {
    const cuerpo = new FormData();
    cuerpo.append('archivo', archivo, archivo.name);

    let params = new HttpParams();
    const codigo = idioma?.trim();
    if (codigo) {
      params = params.set('idioma', codigo);
    }

    const token = this.auth.getJwt();
    const headers = token
      ? new HttpHeaders({ Authorization: `Bearer ${token}` })
      : new HttpHeaders();

    return this.http.post<TranscripcionResponse>(this.urlTranscribir, cuerpo, {
      headers,
      params,
    });
  }
}
