import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { map, tap, throwError } from 'rxjs';
import type { Observable } from 'rxjs';
import type { GuardarPerfil, PerfilUsuario } from '../models/perfilmodel';
import type { PlanId } from '../models/planmodel';
import type { PlanApi, UpdateUserRequestDTO, UserResponseDTO } from '../models/usermodel';
import { API_URL_BASE, AuthService } from './authservice';

/** Convierte el identificador de plan del backend al formato interno del frontend. */
function planApiAPlanId(plan: PlanApi): PlanId {
  const planes: Readonly<Record<PlanApi, PlanId>> = {
    FREE: 'gratuito',
    ORIA: 'oria',
    INFINITIA: 'infinitia',
  };
  return planes[plan];
}

/** Convierte el identificador de plan del frontend al enum esperado por el backend. */
function planIdAPlanApi(plan: PlanId): PlanApi {
  const planes: Readonly<Record<PlanId, PlanApi>> = {
    gratuito: 'FREE',
    oria: 'ORIA',
    infinitia: 'INFINITIA',
  };
  return planes[plan];
}

/** Adapta la respuesta del backend al modelo mostrado por el componente de perfil. */
function adaptarPerfil(datos: UserResponseDTO): PerfilUsuario {
  return {
    nombre: datos.nombre,
    correo: datos.correo,
    ciudad: null,
    edad: null,
    fotoUrl: null,
    rol: datos.rol,
    plan: planApiAPlanId(datos.planId),
  };
}

/**
 * Servicio HTTP de acceso al perfil de la cuenta autenticada conectado a `/user/me`.
 *
 * Cada método emite una respuesta y completa, o propaga el error de la operación.
 * El servidor identifica la cuenta a partir de la sesión y valida los archivos.
 *
 * Está disponible globalmente (`providedIn: 'root'`).
 */
@Injectable({
  providedIn: 'root',
})
export class PerfilService {
  /** Cliente HTTP para comunicarse con el backend. */
  private readonly http = inject(HttpClient);

  /** Servicio de autenticación utilizado para sincronizar la sesión local. */
  private readonly authService = inject(AuthService);

  /** URL del endpoint de perfil del usuario autenticado. */
  private readonly urlPerfil = `${API_URL_BASE}/user/me`;

  /** Última respuesta confirmada por el servidor para conservar correo, rol y plan. */
  private ultimoPerfil: UserResponseDTO | null = null;

  /** Consulta el perfil del usuario autenticado en el backend. */
  obtener(): Observable<PerfilUsuario> {
    return this.http.get<UserResponseDTO>(this.urlPerfil).pipe(
      tap((respuesta) => {
        this.ultimoPerfil = respuesta;
      }),
      map((respuesta) => adaptarPerfil(respuesta)),
    );
  }

  /** Envía los datos actualizados del perfil preservando correo, rol y plan actuales. */
  guardar(solicitud: GuardarPerfil): Observable<PerfilUsuario> {
    const base = this.obtenerDatosBase();
    if (base === null) {
      return throwError(() => new Error('No hay una sesión activa para actualizar el perfil.'));
    }

    const cuerpo: UpdateUserRequestDTO = {
      nombre: solicitud.datos.nombre,
      correo: base.correo,
      rol: base.rol,
      planId: base.planId,
    };

    return this.http.put<UserResponseDTO>(this.urlPerfil, cuerpo).pipe(
      tap((respuesta) => {
        this.ultimoPerfil = respuesta;
        this.authService.actualizarUsuario(respuesta);
      }),
      map((respuesta) => adaptarPerfil(respuesta)),
    );
  }

  /** Obtiene los datos obligatorios de la cuenta desde el último perfil o la sesión actual. */
  private obtenerDatosBase(): Pick<UserResponseDTO, 'correo' | 'rol' | 'planId'> | null {
    if (this.ultimoPerfil !== null) {
      return this.ultimoPerfil;
    }

    const sesion = this.authService.getUser();
    if (sesion === null) {
      return null;
    }

    return {
      correo: sesion.correo,
      rol: sesion.rol,
      planId: planIdAPlanApi(sesion.plan),
    };
  }
}
