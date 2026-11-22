import { TestBed } from '@angular/core/testing';
import type { Mock } from 'vitest';
import type { Rol, User } from '../models/usermodel';
import { AccesoService } from './accesoservice';
import { AuthService } from './authservice';

/** Crea un JWT de prueba que vence dentro de los segundos indicados (negativo si ya venció). */
function tokenQueVenceEn(segundos: number): string {
  const contenido = btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + segundos }))
    .replaceAll('+', '-')
    .replaceAll('/', '_')
    .replace(/=+$/, '');

  return `eyJhbGciOiJIUzI1NiJ9.${contenido}.firma`;
}

/** Crea un usuario de sesión con el rol indicado, aunque no sea un rol válido. */
function usuarioCon(rol: string): User {
  return { nombre: 'laura', correo: 'laura@correo.com', plan: 'gratuito', rol: rol as Rol };
}

/**
 * Pruebas unitarias de {@link AccesoService}.
 *
 * Validan cuándo una sesión local se considera vigente (usuario con un rol reconocido y JWT sin
 * vencer) y que las sesiones vencidas, incompletas o con roles desconocidos se descarten. {@link
 * AuthService} se simula.
 */
describe('AccesoService', () => {
  let servicio: AccesoService;
  let auth: { getUser: Mock; getJwt: Mock; cerrarSesion: Mock };

  beforeEach(() => {
    auth = {
      getUser: vi.fn().mockReturnValue(null),
      getJwt: vi.fn().mockReturnValue(null),
      cerrarSesion: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [{ provide: AuthService, useValue: auth }],
    });

    servicio = TestBed.inject(AccesoService);
  });

  /** Verifica que con un token vigente se devuelva el rol del usuario sin cerrar la sesión. */
  it.each(['USER', 'ADMIN'])('devuelve el rol %s cuando la sesión está vigente', (rol) => {
    auth.getUser.mockReturnValue(usuarioCon(rol));
    auth.getJwt.mockReturnValue(tokenQueVenceEn(3600));

    expect(servicio.obtenerRolVigente()).toBe(rol);
    expect(auth.cerrarSesion).not.toHaveBeenCalled();
  });

  /** Verifica que un token vencido cierre la sesión y devuelva `null`. */
  it('cierra la sesión cuando el token ya venció', () => {
    auth.getUser.mockReturnValue(usuarioCon('USER'));
    auth.getJwt.mockReturnValue(tokenQueVenceEn(-10));

    expect(servicio.obtenerRolVigente()).toBeNull();
    expect(auth.cerrarSesion).toHaveBeenCalledTimes(1);
  });

  /** Verifica que un rol desconocido invalide la sesión. */
  it('descarta una sesión con un rol que la aplicación no conoce', () => {
    auth.getUser.mockReturnValue(usuarioCon('SUPERADMIN'));
    auth.getJwt.mockReturnValue(tokenQueVenceEn(3600));

    expect(servicio.obtenerRolVigente()).toBeNull();
    expect(auth.cerrarSesion).toHaveBeenCalled();
  });

  /** Verifica que un usuario sin token se trate como una sesión incompleta y se cierre. */
  it('descarta una sesión incompleta', () => {
    auth.getUser.mockReturnValue(usuarioCon('USER'));

    expect(servicio.obtenerRolVigente()).toBeNull();
    expect(auth.cerrarSesion).toHaveBeenCalled();
  });

  /** Verifica que sin datos de sesión no se llame a `cerrarSesion`. */
  it('no intenta cerrar sesión si no había ninguna', () => {
    expect(servicio.obtenerRolVigente()).toBeNull();
    expect(auth.cerrarSesion).not.toHaveBeenCalled();
  });
});
