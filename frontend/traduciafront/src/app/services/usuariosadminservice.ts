import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { map, of, switchMap, tap, throwError } from 'rxjs';
import type { Observable } from 'rxjs';
import type {
  ActualizarUsuarioRequestDTO,
  ResultadoAdministracion,
  SolicitudAdministracion,
  UsuarioAdministracion,
  UsuarioAdministracionResponseDTO,
} from '../models/usuarioadminmodel';

/**
 * Convierte la respuesta del backend al modelo de presentación de la tabla administrativa.
 *
 * @param dto Datos del usuario devueltos por `/user`.
 * @param idRespaldo Identificador de respaldo si la respuesta no incluye `id`.
 * @returns Usuario adaptado para la vista administrativa.
 */
function adaptarUsuarioAdmin(
  dto: UsuarioAdministracionResponseDTO,
  idRespaldo: number,
): UsuarioAdministracion {
  return {
    id: dto.id ?? idRespaldo,
    username: dto.nombre,
    email: dto.correo,
    role: dto.rol,
    planType: dto.planId ?? null,
    enabled: true,
    accountNonLocked: true,
  };
}

/**
 * Contrato de acceso a la administración de usuarios.
 *
 * Cada operación debe emitir su respuesta una vez y completar el observable.
 * Los errores del servidor se propagan para que la pantalla informe el fallo.
 * El servidor debe comprobar los permisos y las reglas de cada operación.
 */
export abstract class UsuariosAdminService {
  /** Obtiene los usuarios visibles para el administrador autenticado. */
  abstract listar(): Observable<readonly UsuarioAdministracion[]>;

  /**
   * Ejecuta una solicitud y devuelve el resultado confirmado por el servidor.
   * Una actualización debe devolver el usuario con sus valores definitivos.
   *
   * @param solicitud Operación autorizada y sus datos.
   */
  abstract ejecutar(
    solicitud: SolicitudAdministracion,
  ): Observable<ResultadoAdministracion>;
}

/**
 * Implementación HTTP de {@link UsuariosAdminService} conectada a `/user`.
 */
@Injectable({
  providedIn: 'root',
})
export class UsuariosAdminHttpService extends UsuariosAdminService {
  /** Cliente HTTP utilizado para comunicarse con el controlador de usuarios. */
  private readonly http = inject(HttpClient);

  /** URL base del controlador de usuarios en el backend. */
  private readonly urlBase = 'http://localhost:8080/user';

  /** Caché local de usuarios listados para conservar rol y plan al actualizar. */
  private readonly cacheUsuarios = new Map<number, UsuarioAdministracion>();

  /** Consulta el listado completo de usuarios registrados en el backend. */
  override listar(): Observable<readonly UsuarioAdministracion[]> {
    return this.http.get<UsuarioAdministracionResponseDTO[]>(`${this.urlBase}/listar`).pipe(
      map((usuarios) => usuarios.map((dto, indice) => adaptarUsuarioAdmin(dto, indice + 1))),
      tap((usuarios) => {
        this.cacheUsuarios.clear();
        for (const usuario of usuarios) {
          this.cacheUsuarios.set(usuario.id, usuario);
        }
      }),
    );
  }

  /**
   * Ejecuta las operaciones administrativas soportadas por el backend (`actualizar` y `eliminar`).
   *
   * @param solicitud Operación confirmada desde la tabla administrativa.
   * @returns Resultado confirmado por el servidor.
   */
  override ejecutar(
    solicitud: SolicitudAdministracion,
  ): Observable<ResultadoAdministracion> {
    if (solicitud.accion === 'actualizar') {
      return this.obtenerUsuarioActual(solicitud.usuarioId).pipe(
        switchMap((actual) => {
          const cuerpo: ActualizarUsuarioRequestDTO = {
            nombre: solicitud.cambios.username,
            correo: solicitud.cambios.email,
            rol: actual.role,
            planId: actual.planType ?? 'FREE',
          };

          return this.http.put<UsuarioAdministracionResponseDTO>(
            `${this.urlBase}/actualizar/${solicitud.usuarioId}`,
            cuerpo,
          );
        }),
        map((respuesta) => adaptarUsuarioAdmin(respuesta, solicitud.usuarioId)),
        tap((usuario) => {
          this.cacheUsuarios.set(usuario.id, usuario);
        }),
        map(
          (usuario): ResultadoAdministracion => ({
            tipo: 'actualizado',
            usuario,
          }),
        ),
      );
    }

    if (solicitud.accion === 'eliminar') {
      return this.http
        .delete(`${this.urlBase}/eliminar/${solicitud.usuarioId}`, {
          responseType: 'text',
        })
        .pipe(
          tap(() => {
            this.cacheUsuarios.delete(solicitud.usuarioId);
          }),
          map(
            (): ResultadoAdministracion => ({
              tipo: 'eliminado',
              usuarioId: solicitud.usuarioId,
            }),
          ),
        );
    }

    return throwError(
      () => new Error('La operación solicitada aún no está disponible en el backend.'),
    );
  }

  /** Recupera el usuario desde la caché local o lo consulta por identificador en el backend. */
  private obtenerUsuarioActual(id: number): Observable<UsuarioAdministracion> {
    const enCache = this.cacheUsuarios.get(id);
    if (enCache !== undefined) {
      return of(enCache);
    }

    return this.http
      .get<UsuarioAdministracionResponseDTO>(`${this.urlBase}/${id}`)
      .pipe(map((respuesta) => adaptarUsuarioAdmin(respuesta, id)));
  }
}
