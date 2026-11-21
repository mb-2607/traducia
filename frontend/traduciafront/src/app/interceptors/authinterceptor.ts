import type { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/authservice';

/**
 * Adjunta automáticamente el token JWT de la sesión a las peticiones HTTP
 * enviadas al backend.
 *
 * Lee el token actual desde AuthService y, si existe y la URL de la
 * solicitud comienza por `http://localhost:8080`, clona la petición con la
 * cabecera `Authorization: Bearer <token>`. En caso contrario, deja pasar la
 * petición sin cambios.
 *
 * @param req Solicitud HTTP saliente.
 * @param next Siguiente manejador en la cadena de interceptores de `HttpClient`.
 * @returns Observable con los eventos de la respuesta HTTP.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.getJwt();

  if (token && req.url.startsWith('http://localhost:8080')) {
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
