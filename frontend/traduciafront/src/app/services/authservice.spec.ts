import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import type { AuthResponse, User, UserResponseDTO } from '../models/usermodel';
import { AuthService } from './authservice';

const URL_AUTH = 'http://localhost:8080/auth';

/** Respuesta del backend a un inicio de sesión correcto. */
const respuestaLogin: AuthResponse = {
  jwt: 'jwt-laura',
  userResponse: {
    id: 4,
    nombre: 'laura',
    correo: 'laura@correo.com',
    rol: 'USER',
    planId: 'FREE',
  },
};

/** Usuario esperado después de adaptar la respuesta del backend. */
const usuarioLaura: User = {
  nombre: 'laura',
  correo: 'laura@correo.com',
  rol: 'USER',
  plan: 'gratuito',
};

/**
 * Pruebas unitarias de {@link AuthService}.
 *
 * Validan el inicio y el cierre de sesión, el registro, la restauración de la sesión guardada y la
 * sincronización del usuario con el almacenamiento local. Se usan `HttpTestingController` para el
 * backend y el `localStorage` de jsdom.
 */
describe('AuthService', () => {
  let httpMock: HttpTestingController;

  /**
   * Obtiene el servicio. Se llama después de preparar el almacenamiento porque el constructor lo
   * lee.
   */
  function crearServicio(): AuthService {
    return TestBed.inject(AuthService);
  }

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  /** Verifica que sin datos guardados no haya usuario, token ni sesión. */
  it('empieza sin sesión cuando no hay nada guardado', () => {
    const servicio = crearServicio();

    expect(servicio.getUser()).toBeNull();
    expect(servicio.getJwt()).toBeNull();
    expect(servicio.haySesion()).toBe(false);
  });

  /** Verifica que el constructor restaure el usuario y el token guardados. */
  it('recupera la sesión guardada al recargar la aplicación', () => {
    localStorage.setItem('user', JSON.stringify(usuarioLaura));
    localStorage.setItem('token', JSON.stringify('jwt-guardado'));

    const servicio = crearServicio();

    expect(servicio.getUser()).toEqual(usuarioLaura);
    expect(servicio.getJwt()).toBe('jwt-guardado');
    expect(servicio.haySesion()).toBe(true);
  });

  /** Verifica que un usuario guardado sin token no cuente como sesión activa. */
  it('no considera activa una sesión a la que le falta el token', () => {
    localStorage.setItem('user', JSON.stringify(usuarioLaura));

    expect(crearServicio().haySesion()).toBe(false);
  });

  /** Inicio de sesión contra `/auth/login`. */
  describe('iniciarSesion', () => {
    /** Verifica el método, el cuerpo y las cabeceras de la petición de login. */
    it('envía las credenciales al endpoint de login', () => {
      crearServicio().iniciarSesion({ nombre: 'laura', password: 'Clave2026*' }).subscribe();

      const peticion = httpMock.expectOne(`${URL_AUTH}/login`);
      expect(peticion.request.method).toBe('POST');
      expect(peticion.request.body).toEqual({ nombre: 'laura', password: 'Clave2026*' });
      expect(peticion.request.headers.get('Content-Type')).toBe('application/json');
      peticion.flush(respuestaLogin);
    });

    /**
     * Verifica que la respuesta se adapte al modelo interno y se guarde en memoria y en el
     * almacenamiento.
     */
    it('guarda el usuario y el token recibidos', () => {
      const servicio = crearServicio();
      let usuario: User | undefined;

      servicio.iniciarSesion({ nombre: 'laura', password: 'Clave2026*' }).subscribe((u) => {
        usuario = u;
      });
      httpMock.expectOne(`${URL_AUTH}/login`).flush(respuestaLogin);

      expect(usuario).toEqual(usuarioLaura);
      expect(servicio.getUser()).toEqual(usuarioLaura);
      expect(servicio.getJwt()).toBe('jwt-laura');
      expect(JSON.parse(localStorage.getItem('user')!)).toEqual(usuarioLaura);
      expect(JSON.parse(localStorage.getItem('token')!)).toBe('jwt-laura');
    });

    /** Verifica que un error de login no deje datos de sesión. */
    it('no guarda nada cuando las credenciales son rechazadas', () => {
      const servicio = crearServicio();
      let fallo = false;

      servicio.iniciarSesion({ nombre: 'laura', password: 'mala' }).subscribe({
        error: () => (fallo = true),
      });
      httpMock
        .expectOne(`${URL_AUTH}/login`)
        .flush('Credenciales inválidas', { status: 401, statusText: 'Unauthorized' });

      expect(fallo).toBe(true);
      expect(servicio.haySesion()).toBe(false);
      expect(localStorage.getItem('token')).toBeNull();
    });
  });

  /** Registro de cuentas contra `/auth/register`. */
  describe('registrarUsuario', () => {
    /** Verifica que el registro envíe solo usuario, correo y contraseña, y que no inicie sesión. */
    it('envía solo los datos que espera el backend y no inicia sesión', () => {
      const servicio = crearServicio();
      const datosFormulario = {
        nombre: 'camilo',
        correo: 'camilo@correo.com',
        password: 'Clave2026*',
        ciudad: 'Medellín',
      };
      let registrado: User | undefined;

      servicio.registrarUsuario(datosFormulario).subscribe((u) => (registrado = u));

      const peticion = httpMock.expectOne(`${URL_AUTH}/register`);
      expect(peticion.request.body).toEqual({
        nombre: 'camilo',
        correo: 'camilo@correo.com',
        password: 'Clave2026*',
      });
      peticion.flush({
        nombre: 'camilo',
        correo: 'camilo@correo.com',
        rol: 'USER',
        planId: 'FREE',
      });

      expect(registrado?.plan).toBe('gratuito');
      expect(servicio.haySesion()).toBe(false);
    });
  });

  /** Verifica que `actualizarUsuario` adapte el plan, emita el nuevo usuario y lo guarde. */
  it('actualiza el usuario en memoria y en el almacenamiento', () => {
    const servicio = crearServicio();
    const emitidos: (User | null)[] = [];
    servicio.getUserAsObservable().subscribe((u) => emitidos.push(u));

    const dto: UserResponseDTO = {
      nombre: 'laura',
      correo: 'laura@correo.com',
      rol: 'USER',
      planId: 'INFINITIA',
    };
    const actualizado = servicio.actualizarUsuario(dto);

    expect(actualizado.plan).toBe('infinitia');
    expect(emitidos).toEqual([null, actualizado]);
    expect(JSON.parse(localStorage.getItem('user')!).plan).toBe('infinitia');
  });

  /**
   * Verifica que cerrar sesión borre el usuario y el token sin tocar otras preferencias guardadas.
   */
  it('cierra la sesión y borra los datos guardados', () => {
    localStorage.setItem('user', JSON.stringify(usuarioLaura));
    localStorage.setItem('token', JSON.stringify('jwt-guardado'));
    localStorage.setItem('tema', 'true');
    const servicio = crearServicio();

    servicio.cerrarSesion();

    expect(servicio.getUser()).toBeNull();
    expect(servicio.getJwt()).toBeNull();
    expect(localStorage.getItem('user')).toBeNull();
    expect(localStorage.getItem('token')).toBeNull();
    expect(localStorage.getItem('tema')).toBe('true');
  });
});
