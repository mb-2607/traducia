/** Transcripción devuelta por `POST /traductor/transcribir`. */
export interface TranscripcionResponse {
  readonly texto: string;
  readonly caracteres: number;
}
