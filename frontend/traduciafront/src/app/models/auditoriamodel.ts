import type { Rol } from './usermodel';
import type { PlanAdministracion } from './usuarioadminmodel';

/**
 * Clasificación del actor utilizada por la interfaz.
 *
 * UNKNOWN indica que no hay información suficiente para
 * identificar el rol o la naturaleza del actor.
 */
export type TipoActorAuditoria = Rol | 'SYSTEM' | 'UNKNOWN';

/**
 * Resultados utilizados por la interfaz.
 *
 * El backend actual devuelve un código HTTP, no estos valores.
 * La integración deberá establecer la correspondencia.
 * PENDING se conserva por compatibilidad con la pantalla actual.
 */
export type ResultadoAuditoria = 'SUCCESS' | 'FAILURE' | 'PENDING';

/**
 * Registro devuelto por el backend.
 *
 * Coincide con AuditoriaLogDTO.
 * Representa una petición HTTP registrada en la auditoría.
 */
export interface AuditoriaLogDTO {
  readonly id: number;
  readonly usuario: string;
  readonly metodo: string;
  readonly ruta: string;

  /** Código HTTP de la respuesta registrada. */
  readonly status: number;
  readonly ip: string;

  /** Duración de la petición en milisegundos. */
  readonly duracionMs: number;

  /**
   * Fecha serializada desde LocalDateTime.
   *
   * No incluye zona horaria. La integración deberá acordar
   * con el backend cómo interpretar esta fecha.
   */
  readonly fecha: string;
}

/**
 * Respuesta paginada en el formato Page utilizado actualmente
 * por el controlador de auditoría.
 *
 * Los filtros y el paginador deberán trabajar con estos
 * metadatos cuando se implemente la consulta al servidor.
 */
export interface PaginaAuditoriaDTO {
  readonly content: readonly AuditoriaLogDTO[];

  /** Índice de página del servidor, comenzando en cero. */
  readonly number: number;
  readonly size: number;
  readonly totalElements: number;
  readonly totalPages: number;
  readonly first: boolean;
  readonly last: boolean;
  readonly empty: boolean;
}

/**
 * Información del actor utilizada por la pantalla.
 *
 * Los campos que no estén disponibles se representan con null.
 * El rol desconocido se representa mediante UNKNOWN.
 */
export interface ActorAuditoria {
  readonly id: number | null;
  readonly nombre: string | null;
  readonly correo: string | null;
  readonly tipo: TipoActorAuditoria;

  /**
   * Plan actual de la cuenta, cuando esté disponible.
   *
   * No representa necesariamente el plan que tenía
   * la cuenta cuando ocurrió el evento.
   */
  readonly planActual: PlanAdministracion | null;
}

/**
 * Registro preparado para su presentación en el componente.
 *
 * Es diferente de AuditoriaLogDTO: la integración deberá
 * adaptar los datos recibidos a esta estructura.
 */
export interface RegistroAuditoria {
  /** Identificador estable del evento, convertido a texto. */
  readonly id: string;

  /**
   * Fecha normalizada para su presentación.
   *
   * Debe incluir zona horaria u offset explícito.
   * La conversión requiere conocer la zona de la fecha original.
   */
  readonly fecha: string;
  readonly actor: ActorAuditoria;

  /** Descripción legible de la operación registrada. */
  readonly accion: string;
  readonly resultado: ResultadoAuditoria;

  /** Información adicional apta para mostrar al administrador. */
  readonly detalle: string | null;
}
