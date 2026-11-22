/**
 * Estados de traducción definidos por el backend.
 *
 * Cada proveedor devuelve su propio estado: una solicitud
 * puede contener resultados exitosos y fallidos.
 */
export type EstadoTraduccion = 'EXITOSA' | 'FALLIDA';

/** Proveedores reconocidos por el backend. */
export type ProveedorTraduccion = 'DEEPL' | 'GEMMA' | 'RIVA' | 'NEMOTRON';

/** Clasificación del proveedor según el backend. */
export type TipoProveedor = 'TRADICIONAL' | 'IA';

/**
 * Datos de entrada para solicitar una traducción.
 *
 * Coincide con TraduccionRequestDTO del backend.
 * Los idiomas se identifican mediante sus códigos.
 *
 * La validación del texto y del límite de caracteres
 * corresponde al formulario y al backend.
 */
export interface SolicitudTraduccion {
  texto: string;
  origen: string;
  destino: string;
}

/**
 * Resultado individual devuelto por un proveedor.
 *
 * Coincide con TraduccionResponseDTO del backend.
 */
export interface TraduccionResponseDTO {
  readonly estado: EstadoTraduccion;

  /**
   * Texto generado por el proveedor.
   *
   * Puede ser null cuando la traducción falla.
   */
  readonly textoTraducido: string | null;
  readonly tipoProveedor: TipoProveedor;
  readonly proveedorTraduccion: ProveedorTraduccion;
}

/**
 * Respuesta completa de una solicitud de traducción.
 *
 * Coincide con SolicitudTraduccionResponseDTO del backend.
 * Agrupa los resultados de los proveedores incluidos
 * en el plan del usuario.
 */
export interface SolicitudTraduccionResponseDTO {
  readonly id: number;

  /**
   * Fecha serializada por el backend.
   *
   * Se conserva como string al recibir JSON.
   * El backend utiliza LocalDateTime, que no incluye zona horaria.
   */
  readonly fechaCreacion: string;
  readonly textoOriginal: string;
  readonly origen: string;
  readonly destino: string;
  readonly listaTraduccion: readonly TraduccionResponseDTO[];
}

/**
 * Respuesta paginada devuelta por los endpoints de Spring Data.
 */
export interface PageResponse<T> {
  readonly content: readonly T[];
  readonly totalElements: number;
  readonly totalPages: number;
  readonly size: number;
  readonly number: number;
  readonly first: boolean;
  readonly last: boolean;
  readonly empty: boolean;
}

/**
 * Representación visual utilizada actualmente por el traductor.
 *
 * El servicio de integración podrá construir este modelo
 * a partir de los resultados recibidos del backend.
 */
export interface ResultadoTraduccion {
  proveedor: string;
  texto: string;
}

/**
 * Representación visual compartida por historial y biblioteca.
 *
 * Los campos titulo y tipo pertenecen al modelo de presentación;
 * no forman parte de la respuesta actual de traducción del backend.
 */
export interface TraduccionGuardada {
  id: string;
  titulo: string;
  tipo: 'texto' | 'archivo';
}
