import { TestBed } from '@angular/core/testing';
import { Router, UrlTree, provideRouter } from '@angular/router';
import type { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import type { Rol } from '../models/usermodel';
import { AccesoService } from '../services/accesoservice';
import { administradorGuard } from './administradorguard-guard';

/**
 * Pruebas unitarias de {@link administradorGuard}.
 *
 * Validan que solo un administrador pueda entrar al área de administración y a dónde se redirige en
 * los demás casos. {@link AccesoService} se simula para controlar el rol vigente.
 */
describe('administradorGuard', () => {
  let rolVigente: Rol | null;

  /** Ejecuta el guard dentro del contexto de inyección de TestBed. */
  const ejecutarGuard = () =>
    TestBed.runInInjectionContext(() =>
      administradorGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
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

  /** Verifica que un administrador pueda continuar. */
  it('deja entrar a un administrador', () => {
    rolVigente = 'ADMIN';

    expect(ejecutarGuard()).toBe(true);
  });

  /** Verifica que un usuario sin rol ADMIN sea enviado a su dashboard sin cerrar la sesión. */
  it('manda al dashboard a un usuario sin permisos de administración', () => {
    rolVigente = 'USER';

    expect(rutaDe(ejecutarGuard())).toBe('/dashboard/new');
  });

  /** Verifica que sin una sesión vigente se redirija al login. */
  it('manda al login cuando no hay sesión', () => {
    expect(rutaDe(ejecutarGuard())).toBe('/login');
  });
});
