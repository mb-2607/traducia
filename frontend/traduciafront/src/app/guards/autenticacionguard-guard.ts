import { inject } from '@angular/core';
import { Router } from '@angular/router';
import type { CanActivateFn } from '@angular/router';
import { AccesoService } from '../services/accesoservice';

/**
 * Exige una sesión local con un rol reconocido
 * y un JWT dentro de su periodo de vigencia.
 *
 * Redirige al inicio de sesión cuando se necesita
 * autenticar de nuevo.
 *
 * Su firma también permite utilizarlo en canActivateChild
 * para comprobar la navegación entre rutas hijas.
 */
export const autenticacionGuard: CanActivateFn = () => {
  const acceso = inject(AccesoService);
  const router = inject(Router);
  return acceso.obtenerRolVigente() !== null ? true : router.createUrlTree(['/login']);
};
