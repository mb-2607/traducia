import { Injectable, inject } from '@angular/core';
import type { Rol } from '../models/usermodel';
import { jwtDentroDeVigencia } from '../utils/jwt-utils';
import { AuthService } from './authservice';

/**
 * Centraliza la evaluación local de la sesión para la navegación.
 *
 * Reutiliza el usuario y el JWT que mantiene AuthService.
 * Una sesión local incompleta, vencida o con un rol desconocido
 * se descarta para permitir una nueva autenticación.
 *
 * No realiza peticiones ni sustituye la autorización
 * que debe aplicar el backend en sus endpoints.
 */
@Injectable({
  providedIn: 'root',
})
export class AccesoService {
  private readonly auth = inject(AuthService);

  /**
   * Obtiene el rol de una sesión local cuyo token está
   * dentro de su periodo de vigencia.
   *
   * Los roles aceptados corresponden al modelo del proyecto:
   * USER y ADMIN.
   *
   * @returns El rol disponible o null cuando se requiere autenticar de nuevo.
   */
  obtenerRolVigente(): Rol | null {
    const usuario = this.auth.getUser();
    const token = this.auth.getJwt();

    if (
      typeof usuario === 'object' &&
      usuario !== null &&
      (usuario.rol === 'USER' || usuario.rol === 'ADMIN') &&
      typeof token === 'string' &&
      jwtDentroDeVigencia(token)
    ) {
      return usuario.rol;
    }

    if (usuario !== null || token !== null) {
      this.auth.cerrarSesion();
    }

    return null;
  }
}
