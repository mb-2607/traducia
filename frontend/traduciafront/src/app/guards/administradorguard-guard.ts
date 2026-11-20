import { inject } from '@angular/core';
import { Router } from '@angular/router';
import type { CanActivateFn } from '@angular/router';
import { AccesoService } from '../services/accesoservice';

/**
 * Restringe las páginas administrativas al rol ADMIN.
 *
 * Una visita sin sesión vuelve al inicio de sesión.
 * Un usuario autenticado sin permisos administrativos vuelve
 * a su dashboard sin cerrar una sesión que sigue siendo válida.
 *
 * Puede utilizarse en canActivate y canActivateChild.
 */
export const administradorGuard: CanActivateFn = () => {
  const acceso = inject(AccesoService);
  const router = inject(Router);
  const rol = acceso.obtenerRolVigente();

  if (rol === null) {
    return router.createUrlTree(['/login']);
  }

  return rol === 'ADMIN' ? true : router.createUrlTree(['/dashboard/new']);
};
