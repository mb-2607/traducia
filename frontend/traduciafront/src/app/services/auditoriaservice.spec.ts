import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import type { AuditoriaLogDTO, RegistroAuditoria } from '../models/auditoriamodel';
import type { UserResponseDTO } from '../models/usermodel';
import { AuditoriaService } from './auditoriaservice';

const URL_AUDITORIA = 'http://localhost:8080/auditoria/obtener';
const URL_USUARIOS = 'http://localhost:8080/user/listar';

/** Usuarios registrados que se usan para identificar al actor de cada evento. */
const usuarios: UserResponseDTO[] = [
  { id: 4, nombre: 'Laura', correo: 'laura@correo.com', rol: 'USER', planId: 'ORIA' },
  { id: 1, nombre: 'admin', correo: 'admin@traducia.com', rol: 'ADMIN', planId: 'INFINITIA' },
];

/** Crea un registro de auditoría del backend con valores por defecto. */
function log(cambios: Partial<AuditoriaLogDTO> = {}): AuditoriaLogDTO {
  return {
    id: 101,
    usuario: 'laura',
    metodo: 'POST',
    ruta: '/traductor/traducir',
    status: 200,
    ip: '190.25.1.10',
    duracionMs: 35,
    fecha: '2026-05-10T09:30:00',
    ...cambios,
  };
}

/**
 * Pruebas unitarias de {@link AuditoriaService}.
 *
 * Validan la consulta de los eventos de auditoría y su adaptación al modelo de la vista:
 * normalización de fechas al horario de Colombia, interpretación del estado HTTP, identificación
 * del actor con la lista de usuarios y manejo de errores. Se usa `HttpTestingController` para
 * simular el backend.
 */
describe('AuditoriaService', () => {
  let servicio: AuditoriaService;
  let httpMock: HttpTestingController;

  /** Ejecuta `listar`, responde las dos peticiones y devuelve los registros adaptados. */
  function listarCon(
    registros: AuditoriaLogDTO[],
    listaUsuarios: UserResponseDTO[] = usuarios,
  ): readonly RegistroAuditoria[] {
    let resultado: readonly RegistroAuditoria[] = [];

    servicio.listar().subscribe((lista) => (resultado = lista));
    httpMock.expectOne((req) => req.url === URL_AUDITORIA).flush({ content: registros });
    httpMock.expectOne(URL_USUARIOS).flush(listaUsuarios);

    return resultado;
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    servicio = TestBed.inject(AuditoriaService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  /** Verifica los parámetros de paginación y orden de la consulta de auditoría. */
  it('pide los 200 eventos más recientes', () => {
    servicio.listar().subscribe();

    const peticion = httpMock.expectOne((req) => req.url === URL_AUDITORIA);
    expect(peticion.request.params.get('page')).toBe('0');
    expect(peticion.request.params.get('size')).toBe('200');
    expect(peticion.request.params.get('sort')).toBe('fecha,desc');
    peticion.flush({ content: [] });
    httpMock.expectOne(URL_USUARIOS).flush([]);
  });

  /**
   * Verifica la adaptación completa de un registro, incluido el actor encontrado en la lista de
   * usuarios.
   */
  it('adapta cada registro al formato de la tabla', () => {
    const [registro] = listarCon([log()]);

    expect(registro).toEqual({
      id: '101',
      fecha: '2026-05-10T09:30:00-05:00',
      accion: 'POST /traductor/traducir',
      resultado: 'SUCCESS',
      detalle: 'HTTP 200 · Duración: 35 ms · IP: 190.25.1.10',
      actor: {
        id: 4,
        nombre: 'Laura',
        correo: 'laura@correo.com',
        tipo: 'USER',
        planActual: 'ORIA',
      },
    });
  });

  /** Verifica que el offset de Colombia solo se agregue a las fechas que no traen zona horaria. */
  it('respeta la zona horaria cuando la fecha ya la incluye', () => {
    const registros = listarCon([
      log({ id: 1, fecha: '2026-05-10T14:30:00Z' }),
      log({ id: 2, fecha: '2026-05-10T09:30:00+02:00' }),
      log({ id: 3, fecha: '' }),
    ]);

    expect(registros.map((r) => r.fecha)).toEqual([
      '2026-05-10T14:30:00Z',
      '2026-05-10T09:30:00+02:00',
      '',
    ]);
  });

  /** Verifica la correspondencia entre el código HTTP y el resultado que se muestra. */
  it.each([
    [201, 'SUCCESS'],
    [302, 'SUCCESS'],
    [401, 'FAILURE'],
    [500, 'FAILURE'],
    [102, 'PENDING'],
  ])('interpreta el estado HTTP %i como %s', (status, esperado) => {
    const [registro] = listarCon([log({ status })]);

    expect(registro.resultado).toBe(esperado);
  });

  /** Verifica que el usuario SYSTEM se presente como el sistema. */
  it('identifica las acciones del sistema', () => {
    const [registro] = listarCon([log({ usuario: 'SYSTEM' })]);

    expect(registro.actor).toEqual({
      id: null,
      nombre: 'Sistema',
      correo: null,
      tipo: 'SYSTEM',
      planActual: null,
    });
  });

  /** Verifica que `anonymousUser` se marque como actor desconocido. */
  it('marca como desconocidas las peticiones anónimas', () => {
    const [registro] = listarCon([log({ usuario: 'anonymousUser' })]);

    expect(registro.actor.tipo).toBe('UNKNOWN');
    expect(registro.actor.nombre).toBeNull();
  });

  /**
   * Verifica que un usuario que no está en la lista conserve su nombre, recortado, como actor
   * desconocido.
   */
  it('conserva el nombre de un usuario que ya no aparece en la lista', () => {
    const [registro] = listarCon([log({ usuario: ' carlos ' })]);

    expect(registro.actor).toMatchObject({ nombre: 'carlos', tipo: 'UNKNOWN', correo: null });
  });

  /** Verifica el texto del detalle cuando la IP viene vacía. */
  it('indica cuando la IP no está disponible', () => {
    const [registro] = listarCon([log({ ip: '  ' })]);

    expect(registro.detalle).toContain('IP: No disponible');
  });

  /** Verifica que un error al consultar los usuarios no impida mostrar los eventos. */
  it('muestra los eventos aunque falle la consulta de usuarios', () => {
    let resultado: readonly RegistroAuditoria[] = [];

    servicio.listar().subscribe((lista) => (resultado = lista));
    httpMock.expectOne((req) => req.url === URL_AUDITORIA).flush({ content: [log()] });
    httpMock.expectOne(URL_USUARIOS).flush(null, { status: 403, statusText: 'Forbidden' });

    expect(resultado).toHaveLength(1);
    expect(resultado[0].actor).toMatchObject({ nombre: 'laura', tipo: 'UNKNOWN' });
  });

  /** Verifica que el error de la auditoría se propague y que se cancele la consulta de usuarios. */
  it('propaga el error si no se pueden consultar los eventos', () => {
    let estado: number | undefined;

    servicio.listar().subscribe({ error: (error) => (estado = error.status) });
    httpMock
      .expectOne((req) => req.url === URL_AUDITORIA)
      .flush(null, { status: 500, statusText: 'Server Error' });

    expect(estado).toBe(500);
    expect(httpMock.expectOne(URL_USUARIOS).cancelled).toBe(true);
  });
});
