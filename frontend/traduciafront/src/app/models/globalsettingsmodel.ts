/** URL base del backend de TraducIA. */
export const API_URL_BASE = 'https://gpcueb.org/traduciaback';

/** Límites de carga locales, alineados con el máximo de 10 MiB del backend. */
export const MAX_ARCHIVO_BYTES = 10 * 1024 * 1024;

/** Formatos documentales admitidos por la integración del backend. */
export const EXTENSIONES_DOCUMENTO = [
  'txt', 'pdf', 'doc', 'docx', 'png', 'jpg', 'jpeg', 'tif', 'tiff', 'bmp', 'webp',
] as const;
