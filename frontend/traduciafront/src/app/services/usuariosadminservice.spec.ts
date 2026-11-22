import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import type {
  ResultadoAdministracion,
  UsuarioAdministracion,
  UsuarioAdministracionResponseDTO,
} from '../models/usuarioadminmodel';
import { UsuariosAdminService } from './usuariosadminservice';

const URL_USUARIOS = 'http://localhost:8080/user';

/** Usuarios tal como los devuelve el backend. */
const usuariosBackend: UsuarioAdministracionResponseDTO[] = [
  { id: 7, nombre: 'laura', correo: 'laura@correo.com', rol: 'USER', planId: 'ORIA' },
  { id: 9, nombre: 'andres', correo: 'andres@correo.com', rol: 'ADMIN', planId: 'INFINITIA' },
];

/**
 * Pruebas unitarias de {@link UsuariosAdminService}.
 *
 * Validan el listado de usuarios y su adaptación al modelo de la tabla, y las operaciones
 * administrativas: actualizar conservando el rol y el plan, eliminar y rechazar las acciones que el
 * backend aún no soporta. Se usa `HttpTestingController` para simular el backend.
 */
describe('UsuariosAdminService', () => {
  let servicio: UsuariosAdminService;
  let httpMock: HttpTestingController;

  /** Ejecuta `listar` y responde con los usuarios indicados para llenar la caché del servicio. */
  function cargarLista(usuarios = usuariosBackend): void {
    servicio.listar().subscribe();
    httpMock.expectOne(`${URL_USUARIOS}/listar`).flush(usuarios);
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    servicio = TestBed.inject(UsuariosAdminService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  /** Consulta de `/user/listar`. */
  describe('listar', () => {
    /** Verifica la adaptación de un usuario del backend al modelo de la tabla. */
    it('adapta los usuarios del backend al modelo de la tabla', () => {
      let usuarios: readonly UsuarioAdministracion[] = [];

      servicio.listar().subscribe((lista) => (usuarios = lista));
      httpMock.expectOne(`${URL_USUARIOS}/listar`).flush(usuariosBackend);

      expect(usuarios[0]).toEqual({
        id: 7,
        username: 'laura',
        email: 'laura@correo.com',
        role: 'USER',
        planType: 'ORIA',
        enabled: true,
        accountNonLocked: true,
      });
    });

    /** Verifica el identificador de respaldo, según la posición, cuando falta el `id`. */
    it('asigna un id según la posición cuando el backend no lo envía', () => {
      let usuarios: readonly UsuarioAdministracion[] = [];

      servicio.listar().subscribe((lista) => (usuarios = lista));
      httpMock.expectOne(`${URL_USUARIOS}/listar`).flush([
        { nombre: 'sofia', correo: 'sofia@correo.com', rol: 'USER', planId: 'FREE' },
        { nombre: 'mateo', correo: 'mateo@correo.com', rol: 'USER', planId: 'FREE' },
      ]);

      expect(usuarios.map((u) => u.id)).toEqual([1, 2]);
    });
  });

  /** Operaciones administrativas. */
  describe('ejecutar', () => {
    /**
     * Verifica que la actualización use el rol y el plan del usuario en caché y devuelva el
     * resultado adaptado.
     */
    it('actualiza nombre y correo conservando el rol y el plan del usuario listado', () => {
      cargarLista();
      let resultado: ResultadoAdministracion | undefined;

      servicio
        .ejecutar({
          accion: 'actualizar',
          usuarioId: 7,
          cambios: { username: 'laura.m', email: 'lauram@correo.com' },
        })
        .subscribe((r) => (resultado = r));

      const peticion = httpMock.expectOne(`${URL_USUARIOS}/actualizar/7`);
      expect(peticion.request.method).toBe('PUT');
      expect(peticion.request.body).toEqual({
        nombre: 'laura.m',
        correo: 'lauram@correo.com',
        rol: 'USER',
        planId: 'ORIA',
      });
      peticion.flush({ ...usuariosBackend[0], nombre: 'laura.m', correo: 'lauram@correo.com' });

      expect(resultado).toEqual({
        tipo: 'actualizado',
        usuario: expect.objectContaining({ id: 7, username: 'laura.m' }),
      });
    });

    /**
     * Verifica que, si el usuario no está en caché, primero se consulte por id para obtener su rol
     * y su plan.
     */
    it('consulta el usuario en el backend si no está en la lista cargada', () => {
      servicio
        .ejecutar({
          accion: 'actualizar',
          usuarioId: 12,
          cambios: { username: 'mateo', email: 'mateo@correo.com' },
        })
        .subscribe();

      httpMock.expectOne(`${URL_USUARIOS}/12`).flush({
        id: 12,
        nombre: 'mateo',
        correo: 'viejo@correo.com',
        rol: 'ADMIN',
        planId: 'ORIA',
      });

      const peticion = httpMock.expectOne(`${URL_USUARIOS}/actualizar/12`);
      expect(peticion.request.body).toMatchObject({ rol: 'ADMIN', planId: 'ORIA' });
      peticion.flush({
        id: 12,
        nombre: 'mateo',
        correo: 'mateo@correo.com',
        rol: 'ADMIN',
        planId: 'ORIA',
      });
    });

    /** Verifica que se envíe `FREE` cuando el usuario no tiene plan. */
    it('envía el plan gratuito si el usuario no tiene plan registrado', () => {
      servicio
        .ejecutar({
          accion: 'actualizar',
          usuarioId: 3,
          cambios: { username: 'sofia', email: 'sofia@correo.com' },
        })
        .subscribe();

      httpMock
        .expectOne(`${URL_USUARIOS}/3`)
        .flush({ id: 3, nombre: 'sofia', correo: 'sofia@correo.com', rol: 'USER' });

      const peticion = httpMock.expectOne(`${URL_USUARIOS}/actualizar/3`);
      expect(peticion.request.body.planId).toBe('FREE');
      peticion.flush({
        id: 3,
        nombre: 'sofia',
        correo: 'sofia@correo.com',
        rol: 'USER',
        planId: 'FREE',
      });
    });

    /** Verifica la petición de eliminación y que el usuario salga de la caché. */
    it('elimina un usuario y lo saca de la caché', () => {
      cargarLista();
      let resultado: ResultadoAdministracion | undefined;

      servicio.ejecutar({ accion: 'eliminar', usuarioId: 7 }).subscribe((r) => (resultado = r));

      const peticion = httpMock.expectOne(`${URL_USUARIOS}/eliminar/7`);
      expect(peticion.request.method).toBe('DELETE');
      expect(peticion.request.responseType).toBe('text');
      peticion.flush('Usuario eliminado');

      expect(resultado).toEqual({ tipo: 'eliminado', usuarioId: 7 });

      // Como ya no está en caché, una edición posterior debe consultarlo de nuevo.
      servicio
        .ejecutar({
          accion: 'actualizar',
          usuarioId: 7,
          cambios: { username: 'x', email: 'x@correo.com' },
        })
        .subscribe({ error: () => undefined });
      httpMock
        .expectOne(`${URL_USUARIOS}/7`)
        .flush('No existe', { status: 404, statusText: 'Not Found' });
    });

    /**
     * Verifica que las acciones sin endpoint en el backend terminen en error sin hacer peticiones.
     */
    it.each(['bloquear', 'desbloquear', 'habilitar', 'deshabilitar'] as const)(
      'rechaza la acción %s porque el backend todavía no la soporta',
      (accion) => {
        let mensaje = '';

        servicio
          .ejecutar({ accion, usuarioId: 7 })
          .subscribe({ error: (error: Error) => (mensaje = error.message) });

        expect(mensaje).toBe('La operación solicitada aún no está disponible en el backend.');
      },
    );
  });
});
