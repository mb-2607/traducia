import { inject } from '@angular/core';
import { Router } from '@angular/router';
import type { CanActivateFn } from '@angular/router';
import { AccesoService } from '../services/accesoservice';

/**
 * Permite abrir el login cuando no existe una sesión local vigente.
 *
 * Un administrador autenticado vuelve a su panel.
 * Un usuario autenticado vuelve a su dashboard.
 *
 * No se aplica a las pantallas de recuperación de contraseña,
 * porque también pueden utilizarse desde una sesión iniciada.
 */
export const visitanteGuard: CanActivateFn = () => {
  const acceso = inject(AccesoService);
  const router = inject(Router);
  const rol = acceso.obtenerRolVigente();

  return rol === null
    ? true
    : router.createUrlTree([rol === 'ADMIN' ? '/admin' : '/dashboard/new']);
};
