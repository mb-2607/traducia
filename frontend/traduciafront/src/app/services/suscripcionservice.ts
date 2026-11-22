import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { map, tap } from 'rxjs';
import type { Observable } from 'rxjs';
import { PLANES } from '../models/planmodel';
import type {
  ChangePlanRequestDTO,
  PlanId,
  PlanResponseDTO,
  Planmodel,
} from '../models/planmodel';
import type { PlanApi, User, UserResponseDTO } from '../models/usermodel';
import { AuthService } from './authservice';

/** Mapa de planes del backend al identificador interno del frontend. */
const PLAN_API_A_ID: Readonly<Record<PlanApi, PlanId>> = {
  FREE: 'gratuito',
  ORIA: 'oria',
  INFINITIA: 'infinitia',
};

/** Mapa de identificadores internos del frontend al enum esperado por el backend. */
const PLAN_ID_A_API: Readonly<Record<PlanId, PlanApi>> = {
  gratuito: 'FREE',
  oria: 'ORIA',
  infinitia: 'INFINITIA',
};

/** Convierte el nombre de plan devuelto por el backend a un `PlanId` válido. */
function adaptarPlanId(plan: string): PlanId | null {
  if (Object.prototype.hasOwnProperty.call(PLAN_API_A_ID, plan)) {
    return PLAN_API_A_ID[plan as PlanApi];
  }
  return null;
}

/** Combina los límites devueltos por el backend con los metadatos visuales locales. */
function adaptarDetallePlan(dto: PlanResponseDTO): Planmodel | null {
  const id = adaptarPlanId(dto.plan);
  if (id === null) {
    return null;
  }

  const base = PLANES[id];
  return {
    ...base,
    caracteresPorSolicitud: dto.numeroCaracterPorSolicitud,
    ias: dto.proveedores.length > 0 ? dto.proveedores.length : base.ias,
  };
}

/**
 * Servicio de suscripciones y planes conectado a `/plan`.
 *
 * Mantiene el plan activo sincronizado con la sesión autenticada en {@link AuthService}
 * y expone las operaciones de consulta y cambio de plan contra el backend.
 */
@Injectable({ providedIn: 'root' })
export class SuscripcionService {
  /** Cliente HTTP para comunicarse con el controlador de planes. */
  private readonly http = inject(HttpClient);

  /** Servicio de autenticación que conserva la sesión del usuario. */
  private readonly authService = inject(AuthService);

  /** URL base del controlador de planes en el backend. */
  private readonly urlBase = 'http://localhost:8080/plan';

  /** Estado interno del plan activo del usuario autenticado. */
  private readonly estadoPlan = signal<PlanId>(this.authService.getUser()?.plan ?? 'gratuito');

  /** Plan actualmente activo, expuesto como señal de solo lectura. */
  readonly planActivo = this.estadoPlan.asReadonly();

  /** Sincroniza el plan activo con los cambios de sesión del usuario. */
  constructor() {
    this.authService.getUserAsObservable().subscribe((usuario) => {
      this.estadoPlan.set(usuario?.plan ?? 'gratuito');
    });
  }

  /**
   * Consulta el catálogo de planes disponibles y aplica los límites del backend.
   *
   * @returns Lista de planes listos para mostrarse en la interfaz.
   */
  listarPlanes(): Observable<Planmodel[]> {
    return this.http.get<PlanResponseDTO[]>(this.urlBase).pipe(
      map((respuesta) =>
        respuesta
          .map((item) => adaptarDetallePlan(item))
          .filter((plan): plan is Planmodel => plan !== null),
      ),
    );
  }

  /**
   * Consulta el plan vigente del usuario autenticado y actualiza la señal local.
   *
   * @returns Detalle del plan activo devuelto por el backend.
   */
  obtenerPlanActual(): Observable<PlanResponseDTO> {
    return this.http.get<PlanResponseDTO>(`${this.urlBase}/actual`).pipe(
      tap((respuesta) => {
        const plan = adaptarPlanId(respuesta.plan);
        if (plan !== null) {
          this.estadoPlan.set(plan);
        }
      }),
    );
  }

  /**
   * Solicita al backend el cambio de plan del usuario autenticado y sincroniza la sesión.
   *
   * @param plan Identificador del nuevo plan elegido.
   * @returns Usuario actualizado con el nuevo plan activo.
   */
  cambiarPlan(plan: PlanId): Observable<User> {
    const solicitud: ChangePlanRequestDTO = {
      plan: PLAN_ID_A_API[plan],
    };

    return this.http.put<UserResponseDTO>(`${this.urlBase}/cambiar`, solicitud).pipe(
      map((respuesta) => this.authService.actualizarUsuario(respuesta)),
      tap((usuario) => {
        this.estadoPlan.set(usuario.plan);
      }),
    );
  }
}
