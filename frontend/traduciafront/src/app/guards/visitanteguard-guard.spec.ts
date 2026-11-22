import { TestBed } from '@angular/core/testing';
import { Router, UrlTree, provideRouter } from '@angular/router';
import type { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import type { Rol } from '../models/usermodel';
import { AccesoService } from '../services/accesoservice';
import { visitanteGuard } from './visitanteguard-guard';

/**
 * Pruebas unitarias de {@link visitanteGuard}.
 *
 * Validan que el login solo se muestre a quien no tiene sesión y que los usuarios autenticados
 * vuelvan a su área. {@link AccesoService} se simula para controlar el rol vigente.
 */
describe('visitanteGuard', () => {
  let rolVigente: Rol | null;

  /** Ejecuta el guard dentro del contexto de inyección de TestBed. */
  const ejecutarGuard = () =>
    TestBed.runInInjectionContext(() =>
      visitanteGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );

  /** Convierte en texto el `UrlTree` devuelto por el guard. */
  const rutaDe = (resultado: unknown) => TestBed.inject(Router).serializeUrl(resultado as UrlTree);

  beforeEach(() => {
    rolVigente = null;

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AccesoService, useValue: { obtenerRolVigente: () => rolVigente } },
      ],
    });
  });

  /** Verifica que un visitante sin sesión pueda abrir el login. */
  it('muestra el login a quien no ha iniciado sesión', () => {
    expect(ejecutarGuard()).toBe(true);
  });

  /** Verifica que un administrador autenticado sea enviado a `/admin`. */
  it('devuelve al panel a un administrador que ya inició sesión', () => {
    rolVigente = 'ADMIN';

    expect(rutaDe(ejecutarGuard())).toBe('/admin');
  });

  /** Verifica que un usuario autenticado sea enviado a `/dashboard/new`. */
  it('devuelve al dashboard a un usuario que ya inició sesión', () => {
    rolVigente = 'USER';

    expect(rutaDe(ejecutarGuard())).toBe('/dashboard/new');
  });
});
