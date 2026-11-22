/** Resultado de la transcripción de un archivo de audio. */
export interface TranscripcionResponse {
  readonly texto: string;
  readonly caracteres: number;
}
