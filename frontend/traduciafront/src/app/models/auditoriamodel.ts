import type { Rol } from './usermodel';
import type { PlanAdministracion } from './usuarioadminmodel';

/**
 * Identifica una cuenta de usuario, un administrador
 * o un proceso automático del sistema.
 */
export type TipoActorAuditoria = Rol | 'SYSTEM';

/** Resultado comunicado por el backend para una acción registrada. */
export type ResultadoAuditoria = 'SUCCESS' | 'FAILURE' | 'PENDING';

/**
 * Información del actor asociado al evento.
 *
 * Los valores nulos representan datos no disponibles o que no
 * corresponden a un proceso del sistema.
 *
 * No incluye contraseñas, hashes ni tokens.
 */
export interface ActorAuditoria {
  readonly id: number | null;
  readonly nombre: string | null;
  readonly correo: string | null;
  readonly tipo: TipoActorAuditoria;

  /**
   * Plan actual de la cuenta al consultar la auditoría.
   * No representa necesariamente el plan que tenía al ocurrir el evento.
   */
  readonly planActual: PlanAdministracion | null;
}

/**
 * Contrato de lectura que consume la pantalla de auditoría.
 *
 * La integración adapta los campos del backend a esta estructura.
 * Cada evento debe tener un identificador único y estable.
 */
export interface RegistroAuditoria {
  readonly id: string;

  /** Instante ISO 8601 con zona horaria u offset explícito. */
  readonly fecha: string;
  readonly actor: ActorAuditoria;

  /** Nombre legible de la acción, preparado por la integración. */
  readonly accion: string;
  readonly resultado: ResultadoAuditoria;

  /**
   * Explicación apta para el administrador.
   * No debe contener credenciales ni información secreta.
   */
  readonly detalle: string | null;
}
