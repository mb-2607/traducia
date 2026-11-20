/**
 * Comprueba únicamente la estructura y las fechas públicas de un JWT.
 *
 * Exige una fecha de expiración numérica y respeta la fecha de inicio
 * de validez cuando está presente. Ambos valores se expresan
 * en segundos desde el inicio de la época Unix.
 *
 * La decodificación no comprueba la firma, el emisor, la audiencia
 * ni una posible revocación. Esas validaciones corresponden al backend.
 *
 * @param token JWT sin el prefijo Bearer.
 * @param ahora Instante de evaluación en milisegundos.
 * @returns true si el formato es interpretable y sus fechas permiten usarlo.
 */
export function jwtDentroDeVigencia(token: string, ahora = Date.now()): boolean {
  const partes = token.split('.');

  if (partes.length !== 3 || !partes.every((parte) => /^[A-Za-z0-9_-]+$/.test(parte))) {
    return false;
  }

  const contenido = partes[1];

  if (contenido === undefined || !Number.isFinite(ahora)) {
    return false;
  }

  try {
    const base = contenido.replaceAll('-', '+').replaceAll('_', '/');
    const baseConRelleno = base.padEnd(Math.ceil(base.length / 4) * 4, '=');
    const bytes = Uint8Array.from(atob(baseConRelleno), (caracter) => caracter.charCodeAt(0));
    const texto = new TextDecoder('utf-8', { fatal: true }).decode(bytes);
    const datos: unknown = JSON.parse(texto);

    if (typeof datos !== 'object' || datos === null || Array.isArray(datos) || !('exp' in datos)) {
      return false;
    }

    const exp = datos.exp;
    const segundos = ahora / 1000;

    if (typeof exp !== 'number' || !Number.isFinite(exp) || exp <= segundos) {
      return false;
    }

    if ('nbf' in datos) {
      return typeof datos.nbf === 'number' && Number.isFinite(datos.nbf) && datos.nbf <= segundos;
    }

    return true;
  } catch {
    return false;
  }
}
