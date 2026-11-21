/**
 * Texto extraído del documento y límites calculados por el backend.
 */
export interface DocumentoExtraidoResponse {
  readonly texto: string;
  readonly caracteres: number;
  readonly limiteCaracteres: number;
  readonly excedeLimite: boolean;
}
