import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import type { Observable } from 'rxjs';
import { API_BASE_URL } from '../api-config';
import type { DocumentoExtraidoResponse } from '../../../../../../../../../Downloads/Traducia_Front_Documentos_Audio/Traducia_Front_Documentos_Audio/src/app/models/documentomodel';
import type { SolicitudTraduccionResponseDTO } from '../models/traduccionmodel';
import { AuthService } from './authservice';

/** Consume los endpoints documentales de TraducIA, sin credenciales externas. */
@Injectable({ providedIn: 'root' })
export class DocumentoService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly baseUrl = inject(API_BASE_URL).replace(/\/+$/, '');

  /** Obtiene el texto completo para revisarlo antes de traducir. */
  extraer(archivo: File, idiomaOcr = 'es'): Observable<DocumentoExtraidoResponse> {
    const cuerpo = new FormData();
    cuerpo.append('archivo', archivo, archivo.name);
    return this.http.post<DocumentoExtraidoResponse>(
      `${this.baseUrl}/traductor/extraer-documento`, cuerpo,
      { headers: this.crearEncabezados(), params: { idiomaOcr } },
    );
  }

  /** Alternativa de extracción y traducción directa, sin edición previa. */
  traducir(archivo: File, origen: string, destino: string): Observable<SolicitudTraduccionResponseDTO> {
    const cuerpo = new FormData();
    cuerpo.append('archivo', archivo, archivo.name);
    return this.http.post<SolicitudTraduccionResponseDTO>(
      `${this.baseUrl}/traductor/traducir-documento`, cuerpo,
      { headers: this.crearEncabezados(), params: { origen, destino } },
    );
  }

  /** Lee el JWT actual; no fuerza Content-Type para FormData. */
  private crearEncabezados(): HttpHeaders {
    const token = this.auth.getJwt();
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }
}
