import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { catchError, forkJoin, map, of } from 'rxjs';
import type { Observable } from 'rxjs';
import type {
  ActorAuditoria,
  AuditoriaLogDTO,
  PaginaAuditoriaDTO,
  RegistroAuditoria,
  ResultadoAuditoria,
} from '../models/auditoriamodel';
import type { UserResponseDTO } from '../models/usermodel';
import {API_URL_BASE} from '../models/globalsettingsmodel';

/** Expresión regular para detectar si una fecha ISO ya incluye zona horaria u offset. */
const TIENE_ZONA_HORARIA = /(?:Z|[+-]\d{2}:\d{2})$/i;

/**
 * Normaliza la fecha `LocalDateTime` del backend agregando el offset de Colombia
 * cuando la cadena no incluye zona horaria explícita.
 *
 * @param fecha Cadena de fecha recibida desde la API.
 * @returns Fecha ISO con offset explícito o cadena vacía si viene nula.
 */
function normalizarFecha(fecha: string): string {
  const limpia = fecha?.trim() ?? '';
  if (limpia === '') {
    return '';
  }

  return TIENE_ZONA_HORARIA.test(limpia) ? limpia : `${limpia}-05:00`;
}

/**
 * Traduce el código de estado HTTP del registro al resultado esperado por la vista.
 *
 * @param status Código HTTP devuelto por el backend en la petición auditada.
 * @returns Estado semántico de auditoría.
 */
function mapearResultado(status: number): ResultadoAuditoria {
  if (status >= 200 && status < 400) {
    return 'SUCCESS';
  }
  if (status >= 400) {
    return 'FAILURE';
  }
  return 'PENDING';
}

/**
 * Construye la información del actor cruzando el nombre registrado en auditoría
 * con el catálogo de usuarios disponible.
 *
 * @param nombreUsuario Nombre de usuario registrado en el log de auditoría.
 * @param indiceUsuarios Mapa de usuarios indexado por nombre en minúsculas.
 * @returns Información del actor lista para mostrarse en la tabla.
 */
function adaptarActor(
  nombreUsuario: string,
  indiceUsuarios: ReadonlyMap<string, UserResponseDTO>,
): ActorAuditoria {
  const limpio = nombreUsuario?.trim() ?? '';
  const clave = limpio.toLocaleLowerCase('es');

  if (clave === '' || clave === 'anonymoususer' || clave === 'anonimo') {
    return {
      id: null,
      nombre: null,
      correo: null,
      tipo: 'UNKNOWN',
      planActual: null,
    };
  }

  if (clave === 'system' || clave === 'sistema') {
    return {
      id: null,
      nombre: 'Sistema',
      correo: null,
      tipo: 'SYSTEM',
      planActual: null,
    };
  }

  const usuario = indiceUsuarios.get(clave);
  if (usuario !== undefined) {
    return {
      id: usuario.id ?? null,
      nombre: usuario.nombre,
      correo: usuario.correo,
      tipo: usuario.rol,
      planActual: usuario.planId ?? null,
    };
  }

  return {
    id: null,
    nombre: limpio,
    correo: null,
    tipo: 'UNKNOWN',
    planActual: null,
  };
}

/**
 * Adapta un registro `AuditoriaLogDTO` del backend al modelo de presentación.
 *
 * @param dto Registro devuelto por `GET /auditoria/obtener`.
 * @param indiceUsuarios Mapa de usuarios para enriquecer los datos del actor.
 * @returns Evento normalizado para la tabla de auditoría.
 */
function adaptarRegistro(
  dto: AuditoriaLogDTO,
  indiceUsuarios: ReadonlyMap<string, UserResponseDTO>,
): RegistroAuditoria {
  const ip = dto.ip?.trim() || 'No disponible';

  return {
    id: String(dto.id),
    fecha: normalizarFecha(dto.fecha),
    actor: adaptarActor(dto.usuario, indiceUsuarios),
    accion: `${dto.metodo} ${dto.ruta}`.trim(),
    resultado: mapearResultado(dto.status),
    detalle: `HTTP ${dto.status} · Duración: ${dto.duracionMs} ms · IP: ${ip}`,
  };
}

/**
 * Servicio HTTP de consulta de auditoría conectado a `/auditoria/obtener`.
 *
 * Cada consulta emite una lista completa, finaliza y propaga los errores que
 * impidan obtener los registros. El backend comprueba los permisos y devuelve
 * los eventos registrados; la pantalla no modifica ni elimina eventos.
 *
 * Está disponible globalmente (`providedIn: 'root'`).
 */
@Injectable({
  providedIn: 'root',
})
export class AuditoriaService {
  /** Cliente HTTP utilizado para consultar los registros de auditoría. */
  private readonly http = inject(HttpClient);

  /** URL del endpoint paginado de auditoría en el backend. */
  private readonly urlAuditoria = `${API_URL_BASE}/auditoria/obtener`;

  /** URL del endpoint de usuarios utilizado para enriquecer los datos del actor. */
  private readonly urlUsuarios = `${API_URL_BASE}/user/listar`;

  /**
   * Obtiene los eventos que puede consultar el administrador autenticado.
   *
   * @returns Lista de eventos disponibles para su consulta.
   */
  listar(): Observable<readonly RegistroAuditoria[]> {
    const params = new HttpParams()
      .set('page', '0')
      .set('size', '200')
      .set('sort', 'fecha,desc');

    return forkJoin({
      pagina: this.http.get<PaginaAuditoriaDTO>(this.urlAuditoria, { params }),
      usuarios: this.http
        .get<UserResponseDTO[]>(this.urlUsuarios)
        .pipe(catchError(() => of([] as UserResponseDTO[]))),
    }).pipe(
      map(({ pagina, usuarios }) => {
        const indiceUsuarios = new Map<string, UserResponseDTO>();
        for (const usuario of usuarios) {
          indiceUsuarios.set(usuario.nombre.trim().toLocaleLowerCase('es'), usuario);
        }

        return (pagina.content ?? []).map((registro) => adaptarRegistro(registro, indiceUsuarios));
      }),
    );
  }
}
