import { DOCUMENT } from '@angular/common';
import { TestBed } from '@angular/core/testing';
import { StorageService } from './storageservice';

/**
 * Pruebas unitarias de {@link StorageService}.
 *
 * Validan la lectura y escritura de valores JSON en `localStorage`, la tolerancia a datos dañados y
 * el comportamiento cuando no existe una ventana.
 */
describe('StorageService', () => {
  let servicio: StorageService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
    servicio = TestBed.inject(StorageService);
  });

  afterEach(() => {
    localStorage.clear();
  });

  /** Verifica que `set` guarde el valor serializado como JSON. */
  it('guarda los valores como JSON', () => {
    servicio.set('usuario', { nombre: 'laura', plan: 'oria' });

    expect(localStorage.getItem('usuario')).toBe('{"nombre":"laura","plan":"oria"}');
  });

  /** Verifica que `get` devuelva los valores ya deserializados. */
  it('devuelve el valor ya convertido desde JSON', () => {
    localStorage.setItem('tema', 'false');
    localStorage.setItem('usuario', '{"nombre":"laura"}');

    expect(servicio.get<boolean>('tema')).toBe(false);
    expect(servicio.get<{ nombre: string }>('usuario')).toEqual({ nombre: 'laura' });
  });

  /** Verifica que una clave inexistente devuelva `null`. */
  it('devuelve null cuando la clave no existe', () => {
    expect(servicio.get('no-existe')).toBeNull();
  });

  /** Verifica que un JSON dañado devuelva `null` en lugar de lanzar un error. */
  it('devuelve null en lugar de fallar si el contenido está dañado', () => {
    localStorage.setItem('usuario', '{nombre: laura');

    expect(servicio.get('usuario')).toBeNull();
  });

  /** Verifica que guardar dos veces la misma clave conserve el último valor. */
  it('sobrescribe un valor existente', () => {
    servicio.set('token', 'primero');
    servicio.set('token', 'segundo');

    expect(servicio.get('token')).toBe('segundo');
  });

  /** Verifica que `remove` no afecte a las demás claves. */
  it('elimina solo la clave indicada', () => {
    servicio.set('token', 'abc');
    servicio.set('tema', true);

    servicio.remove('token');

    expect(servicio.get('token')).toBeNull();
    expect(servicio.get('tema')).toBe(true);
  });

  /** Verifica que `clear` borre todas las claves, incluidas las de otras partes del sitio. */
  it('limpia todo el almacenamiento', () => {
    servicio.set('token', 'abc');
    localStorage.setItem('otra-app', 'dato');

    servicio.clear();

    expect(localStorage.length).toBe(0);
  });

  /** Entorno sin `window`, como ocurre en el renderizado del lado del servidor. */
  describe('sin ventana disponible', () => {
    beforeEach(() => {
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        providers: [{ provide: DOCUMENT, useValue: { defaultView: null } }],
      });
      servicio = TestBed.inject(StorageService);
    });

    /** Verifica que sin ventana ninguna operación falle y las lecturas devuelvan `null`. */
    it('no lanza errores y se comporta como un almacenamiento vacío', () => {
      expect(() => servicio.set('token', 'abc')).not.toThrow();
      expect(() => servicio.remove('token')).not.toThrow();
      expect(() => servicio.clear()).not.toThrow();
      expect(servicio.get('token')).toBeNull();
    });
  });
});
