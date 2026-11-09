export type PlanId = 'gratuito' | 'oria' | 'infinitia';

export interface Plan {
  readonly id: PlanId;
  readonly nombre: string;
  readonly descripcion: string;
  readonly precio: number;
  readonly palabras: number | null;
  readonly archivosPorDia: number | null;
  readonly ias: number;
  readonly icono: string;
}

export const PLANES: Readonly<Record<PlanId, Plan>> = {
  gratuito: {
    id: 'gratuito',
    nombre: 'Free Pass',
    descripcion: 'Para empezar con traducciones de uso diario.',
    precio: 0,
    palabras: 250,
    archivosPorDia: 2,
    ias: 1,
    icono: 'fa-ticket',
  },
  oria: {
    id: 'oria',
    nombre: 'orIA',
    descripcion: 'Para textos más largos y más archivos cada día.',
    precio: 19900,
    palabras: 1500,
    archivosPorDia: 10,
    ias: 2,
    icono: 'fa-crown',
  },
  infinitia: {
    id: 'infinitia',
    nombre: 'InfinitIA',
    descripcion: 'Para traducir con las tres IAs y sin límites de uso.',
    precio: 39900,
    palabras: null,
    archivosPorDia: null,
    ias: 3,
    icono: 'fa-satellite',
  },
};
