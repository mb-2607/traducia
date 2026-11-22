import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import type { Mock } from 'vitest';
import type { PerfilUsuario } from '../models/perfilmodel';
import type { User, UserResponseDTO } from '../models/usermodel';
import { AuthService } from './authservice';
import { PerfilService } from './perfilservice';

const URL_PERFIL = 'http://localhost:8080/user/me';

/** Perfil que devuelve el backend. */
const perfilServidor: UserResponseDTO = {
  id: 8,
  nombre: 'Laura Martínez',
  correo: 'laura@correo.com',
  rol: 'USER',
  planId: 'ORIA',
};

/**
 * Pruebas unitarias de {@link PerfilService}.
 *
 * Validan la consulta del perfil y el guardado de cambios conservando el correo, el rol y el plan
 * de la cuenta, además de la sincronización con la sesión. {@link AuthService} se simula y
 * `HttpTestingController` reemplaza al backend.
 */
describe('PerfilService', () => {
  let servicio: PerfilService;
  let httpMock: HttpTestingController;
  let auth: { getUser: Mock; actualizarUsuario: Mock };

  /** Construye la solicitud de guardado cambiando solo el nombre. */
  const cambios = (nombre: string) => ({
    datos: { nombre, ciudad: null, edad: null },
    foto: null,
    quitarFoto: false,
  });

  beforeEach(() => {
    auth = { getUser: vi.fn().mockReturnValue(null), actualizarUsuario: vi.fn() };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: auth },
      ],
    });

    servicio = TestBed.inject(PerfilService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  /**
   * Verifica la adaptación de la respuesta: el plan en formato interno y en `null` los campos que
   * el backend aún no envía.
   */
  it('consulta el perfil y lo adapta al modelo de la vista', () => {
    let perfil: PerfilUsuario | undefined;

    servicio.obtener().subscribe((p) => (perfil = p));
    httpMock.expectOne(URL_PERFIL).flush(perfilServidor);

    expect(perfil).toEqual({
      nombre: 'Laura Martínez',
      correo: 'laura@correo.com',
      rol: 'USER',
      plan: 'oria',
      ciudad: null,
      edad: null,
      fotoUrl: null,
    });
  });

  /** Verifica que el `PUT` tome el correo, el rol y el plan del último perfil consultado. */
  it('al guardar conserva el correo, el rol y el plan del último perfil consultado', () => {
    servicio.obtener().subscribe();
    httpMock.expectOne(URL_PERFIL).flush(perfilServidor);

    servicio.guardar(cambios('Laura M.')).subscribe();

    const peticion = httpMock.expectOne(URL_PERFIL);
    expect(peticion.request.method).toBe('PUT');
    expect(peticion.request.body).toEqual({
      nombre: 'Laura M.',
      correo: 'laura@correo.com',
      rol: 'USER',
      planId: 'ORIA',
    });
    peticion.flush({ ...perfilServidor, nombre: 'Laura M.' });
  });

  /**
   * Verifica que la respuesta del guardado se envíe a `actualizarUsuario` y se devuelva adaptada.
   */
  it('sincroniza la sesión con la respuesta del servidor', () => {
    let guardado: PerfilUsuario | undefined;
    servicio.obtener().subscribe();
    httpMock.expectOne(URL_PERFIL).flush(perfilServidor);

    servicio.guardar(cambios('Laura M.')).subscribe((p) => (guardado = p));
    httpMock.expectOne(URL_PERFIL).flush({ ...perfilServidor, nombre: 'Laura M.' });

    expect(auth.actualizarUsuario).toHaveBeenCalledWith({ ...perfilServidor, nombre: 'Laura M.' });
    expect(guardado?.nombre).toBe('Laura M.');
  });

  /**
   * Verifica que sin un perfil previo se usen los datos de la sesión, con el plan convertido al
   * formato del backend.
   */
  it('usa los datos de la sesión si todavía no se ha consultado el perfil', () => {
    const sesion: User = {
      nombre: 'andres',
      correo: 'andres@correo.com',
      rol: 'ADMIN',
      plan: 'infinitia',
    };
    auth.getUser.mockReturnValue(sesion);

    servicio.guardar(cambios('Andrés')).subscribe();

    const peticion = httpMock.expectOne(URL_PERFIL);
    expect(peticion.request.body).toEqual({
      nombre: 'Andrés',
      correo: 'andres@correo.com',
      rol: 'ADMIN',
      planId: 'INFINITIA',
    });
    peticion.flush({ ...perfilServidor, nombre: 'Andrés' });
  });

  /** Verifica que sin sesión se emita un error y no se haga ninguna petición. */
  it('falla sin llamar al backend cuando no hay sesión', () => {
    let mensaje = '';

    servicio
      .guardar(cambios('Nadie'))
      .subscribe({ error: (error: Error) => (mensaje = error.message) });

    expect(mensaje).toBe('No hay una sesión activa para actualizar el perfil.');
    httpMock.expectNone(URL_PERFIL);
  });

  /** Verifica que un error del servidor no modifique la sesión. */
  it('no actualiza la sesión si el servidor rechaza los cambios', () => {
    servicio.obtener().subscribe();
    httpMock.expectOne(URL_PERFIL).flush(perfilServidor);

    servicio.guardar(cambios('L')).subscribe({ error: () => undefined });
    httpMock
      .expectOne(URL_PERFIL)
      .flush('Nombre inválido', { status: 400, statusText: 'Bad Request' });

    expect(auth.actualizarUsuario).not.toHaveBeenCalled();
  });
});
