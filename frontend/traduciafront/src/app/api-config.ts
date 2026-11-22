import { InjectionToken } from '@angular/core';

/** URL pública del backend. En producción incluye el contexto del WAR. */
export const API_BASE_URL = new InjectionToken<string>('API_BASE_URL', {
  providedIn: 'root',
  factory: () => 'http://localhost:8080',
});

/** Límites de carga locales, alineados con el máximo de 10 MiB del backend. */
export const MAX_ARCHIVO_BYTES = 10 * 1024 * 1024;

/** Formatos documentales admitidos por la integración del backend. */
export const EXTENSIONES_DOCUMENTO = [
  'txt', 'pdf', 'doc', 'docx', 'png', 'jpg', 'jpeg', 'tif', 'tiff', 'bmp', 'webp',
] as const;
