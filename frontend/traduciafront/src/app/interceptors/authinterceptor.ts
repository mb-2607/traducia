import type { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { API_URL_BASE, AuthService } from '../services/authservice';

/**
 * Adjunta automáticamente el token JWT de la sesión a las peticiones HTTP
 *  * enviadas al backend de TraducIA, excepto en rutas de autenticación..
 *
  * Lee el token actual desde AuthService y, si existe, la URL no pertenece a
   * `/auth/` y comienza por {@link API_URL_BASE}, clona la petición con la
 * cabecera `Authorization: Bearer <token>`. En caso contrario, deja pasar la
 * petición sin cambios.
 *
 * @param req Solicitud HTTP saliente.
 * @param next Siguiente manejador en la cadena de interceptores de `HttpClient`.
 * @returns Observable con los eventos de la respuesta HTTP.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.url.includes('/auth/')) {
    return next(req);
  }
  const auth = inject(AuthService);
  const token = auth.getJwt();

  if (token && req.url.startsWith(API_URL_BASE)) {
    return next(
      req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`,
        },
      }),
    );
  }

  return next(req);
};
