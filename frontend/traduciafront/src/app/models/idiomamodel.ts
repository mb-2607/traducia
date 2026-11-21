/**
 * Idioma devuelto por el backend.
 *
 * Coincide con IdiomaResponseDTO de la API.
 * El código identifica el idioma en las solicitudes de traducción.
 */
export interface IdiomaResponseDTO {
  readonly codigo: string;
  readonly nombre: string;
}

/**
 * Representación de un idioma utilizada por el frontend.
 *
 * El servicio de integración deberá adaptar codigo a id
 * y proporcionar la etiqueta de idioma utilizada para la voz.
 */
export interface Idioma {
  /** Código del idioma; corresponde a codigo en el backend. */
  id: string;

  /** Nombre que aparece en el selector. */
  nombre: string;

  /**
   * Etiqueta de idioma para las funciones de voz del navegador.
   *
   * Por ejemplo: es-CO o en-US.
   * Este dato no viene en la respuesta actual del backend.
   */
  voz: string;
}
