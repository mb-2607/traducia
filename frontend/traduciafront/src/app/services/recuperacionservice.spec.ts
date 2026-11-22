import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { RecuperacionService } from './recuperacionservice';

const URL_AUTH = 'http://localhost:8080/auth';

/**
 * Pruebas unitarias de {@link RecuperacionService}.
 *
 * Validan las peticiones para solicitar el enlace de recuperación y para restablecer la contraseña.
 * Se usa `HttpTestingController` para simular el backend.
 */
describe('RecuperacionService', () => {
  let servicio: RecuperacionService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    servicio = TestBed.inject(RecuperacionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  /**
   * Verifica la petición a `forgot-password` con respuesta de texto y que se emita una vez al
   * completarse.
   */
  it('solicita el enlace de recuperación para el correo indicado', () => {
    const completado = vi.fn();

    servicio.solicitarEnlace('ana@correo.com').subscribe(completado);

    const peticion = httpMock.expectOne(`${URL_AUTH}/forgot-password`);
    expect(peticion.request.method).toBe('POST');
    expect(peticion.request.body).toEqual({ correo: 'ana@correo.com' });
    expect(peticion.request.responseType).toBe('text');
    peticion.flush('Si el correo existe, se envió un enlace.');

    expect(completado).toHaveBeenCalledWith(undefined);
  });

  /** Verifica el cuerpo de la petición a `reset-password`. */
  it('envía el token junto con la nueva contraseña', () => {
    const completado = vi.fn();

    servicio.restablecer('token-123', 'NuevaClave2026').subscribe(completado);

    const peticion = httpMock.expectOne(`${URL_AUTH}/reset-password`);
    expect(peticion.request.body).toEqual({ token: 'token-123', nuevaPassword: 'NuevaClave2026' });
    peticion.flush('Contraseña actualizada');

    expect(completado).toHaveBeenCalledTimes(1);
  });

  /** Verifica que el error del backend llegue al suscriptor. */
  it('propaga el error cuando el token ya no sirve', () => {
    let estado: number | undefined;

    servicio.restablecer('token-vencido', 'NuevaClave2026').subscribe({
      error: (error) => (estado = error.status),
    });
    httpMock
      .expectOne(`${URL_AUTH}/reset-password`)
      .flush('Token inválido', { status: 400, statusText: 'Bad Request' });

    expect(estado).toBe(400);
  });
});
