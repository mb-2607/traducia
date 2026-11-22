import { TestBed } from '@angular/core/testing';
import { Router, UrlTree, provideRouter } from '@angular/router';
import type { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import type { Rol } from '../models/usermodel';
import { AccesoService } from '../services/accesoservice';
import { autenticacionGuard } from './autenticacionguard-guard';

/**
 * Pruebas unitarias de {@link autenticacionGuard}.
 *
 * Validan que las rutas protegidas solo se abran con una sesión vigente. {@link AccesoService} se
 * simula para controlar el rol vigente.
 */
describe('autenticacionGuard', () => {
  let rolVigente: Rol | null;

  /** Ejecuta el guard dentro del contexto de inyección de TestBed. */
  const ejecutarGuard = () =>
    TestBed.runInInjectionContext(() =>
      autenticacionGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );

  beforeEach(() => {
    rolVigente = null;

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AccesoService, useValue: { obtenerRolVigente: () => rolVigente } },
      ],
    });
  });

  /** Verifica que cualquier rol reconocido pueda continuar. */
  it.each<Rol>(['USER', 'ADMIN'])('permite la navegación con una sesión de %s', (rol) => {
    rolVigente = rol;

    expect(ejecutarGuard()).toBe(true);
  });

  /** Verifica que sin una sesión vigente se devuelva un `UrlTree` hacia el login. */
  it('redirige al login cuando la sesión no es válida', () => {
    const resultado = ejecutarGuard();

    expect(resultado).toBeInstanceOf(UrlTree);
    expect(TestBed.inject(Router).serializeUrl(resultado as UrlTree)).toBe('/login');
  });
});
