import type { PlanApi } from './usermodel';

/** Identificadores internos de los planes en el frontend. */
export type PlanId = 'gratuito' | 'oria' | 'infinitia';

/** Representación visual y funcional de un plan de suscripción. */
export interface Planmodel {
  readonly id: PlanId;
  readonly nombre: string;
  readonly descripcion: string;
  readonly precio: number;
  readonly caracteresPorSolicitud: number;
  readonly archivosPorDia: number | null;
  readonly ias: number;
  readonly icono: string;
}

/** Detalle de plan devuelto por `GET /plan` y `GET /plan/actual`. */
export interface PlanResponseDTO {
  readonly plan: PlanApi;
  readonly numeroCaracterPorSolicitud: number;
  readonly proveedores: readonly string[];
}

/** Solicitud enviada a `PUT /plan/cambiar`. */
export interface ChangePlanRequestDTO {
  readonly plan: PlanApi;
}

/** Catálogo base de planes con sus datos visuales y precios locales. */
export const PLANES: Readonly<Record<PlanId, Planmodel>> = {
  gratuito: {
    id: 'gratuito',
    nombre: 'Free Pass',
    descripcion: 'Para empezar con traducciones de uso diario.',
    precio: 0,
    caracteresPorSolicitud: 500,
    archivosPorDia: 2,
    ias: 1,
    icono: 'fa-ticket',
  },
  oria: {
    id: 'oria',
    nombre: 'orIA',
    descripcion: 'Para textos más largos y más archivos cada día.',
    precio: 19900,
    caracteresPorSolicitud: 2000,
    archivosPorDia: 10,
    ias: 2,
    icono: 'fa-crown',
  },
  infinitia: {
    id: 'infinitia',
    nombre: 'InfinitIA',
    descripcion: 'Para traducir con las tres IAs con mayor cantidad de caracteres por traducción.',
    precio: 39900,
    caracteresPorSolicitud: 5000,
    archivosPorDia: null,
    ias: 3,
    icono: 'fa-satellite',
  },
};
