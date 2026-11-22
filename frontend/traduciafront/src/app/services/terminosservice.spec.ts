import { TestBed } from '@angular/core/testing';
import { TerminosService } from './terminosservice';

/** Clave con la que el servicio guarda la aceptación. */
const CLAVE = 'traduciaTerminosAceptados';

/**
 * Pruebas unitarias de {@link TerminosService}.
 *
 * Validan cómo se guarda y se lee la aceptación de los términos en el navegador: formato y versión
 * del registro, migración del formato anterior y comportamiento cuando el almacenamiento está
 * bloqueado.
 */
describe('TerminosService', () => {
  // El servicio lee el almacenamiento en el constructor, así que cada prueba
  // prepara localStorage antes de pedirlo.
  const crearServicio = () => TestBed.inject(TerminosService);

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
  });

  afterEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
    vi.useRealTimers();
  });

  /** Verifica que sin un registro previo los términos no estén aceptados. */
  it('inicia sin aceptación en un navegador nuevo', () => {
    const servicio = crearServicio();

    expect(servicio.aceptados()).toBe(false);
    expect(servicio.persistenciaDisponible()).toBe(true);
  });

  /** Verifica que aceptar guarde la versión vigente y la fecha de aceptación. */
  it('guarda la versión y la fecha al aceptar', () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-05-10T15:00:00.000Z'));
    const servicio = crearServicio();

    servicio.aceptar();

    expect(servicio.aceptados()).toBe(true);
    expect(JSON.parse(localStorage.getItem(CLAVE) ?? 'null')).toEqual({
      version: '1.0',
      aceptadoEn: '2026-05-10T15:00:00.000Z',
    });
  });

  /** Verifica que un registro válido se lea como aceptado. */
  it('recuerda una aceptación guardada anteriormente', () => {
    localStorage.setItem(
      CLAVE,
      JSON.stringify({ version: '1.0', aceptadoEn: '2026-01-15T12:00:00Z' }),
    );

    expect(crearServicio().aceptados()).toBe(true);
  });

  /** Verifica que el valor antiguo `true` se migre al formato nuevo con fecha `null`. */
  it('migra el formato antiguo sin inventar la fecha de aceptación', () => {
    localStorage.setItem(CLAVE, 'true');

    const servicio = crearServicio();

    expect(servicio.aceptados()).toBe(true);
    expect(JSON.parse(localStorage.getItem(CLAVE) ?? 'null')).toEqual({
      version: '1.0',
      aceptadoEn: null,
    });
  });

  /** Verifica que un registro de otra versión no cuente como aceptado. */
  it('pide aceptar de nuevo cuando cambia la versión de los términos', () => {
    localStorage.setItem(
      CLAVE,
      JSON.stringify({ version: '0.9', aceptadoEn: '2025-11-01T10:00:00Z' }),
    );

    expect(crearServicio().aceptados()).toBe(false);
  });

  /** Verifica que no se acepten registros con fecha inválida, sin fecha o con otro formato. */
  it.each([
    ['una fecha inválida', JSON.stringify({ version: '1.0', aceptadoEn: 'ayer' })],
    ['un registro sin fecha', JSON.stringify({ version: '1.0' })],
    ['un arreglo', JSON.stringify(['1.0'])],
    ['el texto false', 'false'],
  ])('no acepta %s como registro válido', (_caso, guardado) => {
    localStorage.setItem(CLAVE, guardado);

    expect(crearServicio().aceptados()).toBe(false);
  });

  /** Verifica que un JSON dañado se trate como no aceptado. */
  it('trata un registro dañado como no aceptado', () => {
    localStorage.setItem(CLAVE, '{version: 1.0');

    expect(crearServicio().aceptados()).toBe(false);
  });

  /**
   * Verifica que, si no se puede guardar, la aceptación siga vigente en memoria y se informe que no
   * hay persistencia.
   */
  it('mantiene la aceptación en memoria si el navegador bloquea el almacenamiento', () => {
    const servicio = crearServicio();
    vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
      throw new DOMException('Bloqueado', 'SecurityError');
    });

    servicio.aceptar();

    expect(servicio.aceptados()).toBe(true);
    expect(servicio.persistenciaDisponible()).toBe(false);
  });
});
