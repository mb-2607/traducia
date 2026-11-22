import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { API_BASE_URL } from '../api-config';
import { AuthService } from './authservice';
import { TranscripcionService } from './transcripcionservice';

const URL_TRANSCRIBIR = 'http://localhost:8080/traductor/transcribir';

/** Transcripción de ejemplo que devuelve el backend. */
const transcripcion = { texto: 'Hola, ¿cómo estás?', caracteres: 18 };

/** Crea una nota de voz de prueba en formato WebM. */
function notaDeVoz(nombre = 'grabacion.webm'): File {
  return new File([new Uint8Array([26, 69, 223, 163])], nombre, { type: 'audio/webm' });
}

/**
 * Pruebas unitarias de {@link TranscripcionService}.
 *
 * Validan el envío del audio al backend para transcribirlo: el archivo viaja en un `FormData`, el
 * idioma solo se envía cuando se indica (si no, el backend lo detecta) y el token de la sesión va
 * en la cabecera de autorización. {@link AuthService} se simula y `HttpTestingController`
 * reemplaza al backend.
 */
describe('TranscripcionService', () => {
  let servicio: TranscripcionService;
  let httpMock: HttpTestingController;
  let jwt: string | null;

  /** Busca la petición de transcripción sin importar sus parámetros. */
  const peticionTranscribir = () => httpMock.expectOne((req) => req.url === URL_TRANSCRIBIR);

  beforeEach(() => {
    jwt = 'jwt-laura';

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: { getJwt: () => jwt } },
        { provide: API_BASE_URL, useValue: 'http://localhost:8080/' },
      ],
    });

    servicio = TestBed.inject(TranscripcionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  /** Verifica que el audio se envíe como `FormData` en el campo `archivo`, con su nombre original. */
  it('envía el audio dentro de un FormData', () => {
    servicio.transcribir(notaDeVoz()).subscribe();

    const peticion = peticionTranscribir();
    expect(peticion.request.method).toBe('POST');
    expect((peticion.request.body as FormData).get('archivo')).toBeInstanceOf(File);
    expect(((peticion.request.body as FormData).get('archivo') as File).name).toBe(
      'grabacion.webm',
    );
    peticion.flush(transcripcion);
  });

  /** Verifica que sin idioma no se envíe el parámetro, para que el backend lo detecte. */
  it('deja que el backend detecte el idioma si no se indica', () => {
    servicio.transcribir(notaDeVoz()).subscribe();

    const peticion = peticionTranscribir();
    expect(peticion.request.params.has('idioma')).toBe(false);
    peticion.flush(transcripcion);
  });

  /** Verifica que el idioma indicado se envíe sin espacios sobrantes. */
  it('envía el idioma indicado sin espacios', () => {
    servicio.transcribir(notaDeVoz(), ' en ').subscribe();

    const peticion = peticionTranscribir();
    expect(peticion.request.params.get('idioma')).toBe('en');
    peticion.flush(transcripcion);
  });

  /** Verifica que un idioma formado solo por espacios se trate como si no se hubiera indicado. */
  it('ignora un idioma vacío', () => {
    servicio.transcribir(notaDeVoz(), '   ').subscribe();

    const peticion = peticionTranscribir();
    expect(peticion.request.params.has('idioma')).toBe(false);
    peticion.flush(transcripcion);
  });

  /** Verifica que la URL base se use sin la barra final y que se adjunte el token de la sesión. */
  it('usa la URL base sin barra final y agrega el token', () => {
    servicio.transcribir(notaDeVoz()).subscribe();

    const peticion = peticionTranscribir();
    expect(peticion.request.url).toBe(URL_TRANSCRIBIR);
    expect(peticion.request.headers.get('Authorization')).toBe('Bearer jwt-laura');
    peticion.flush(transcripcion);
  });

  /** Verifica que sin sesión la petición salga sin cabecera de autorización. */
  it('no envía cabecera de autorización si no hay sesión', () => {
    jwt = null;

    servicio.transcribir(notaDeVoz()).subscribe();

    const peticion = peticionTranscribir();
    expect(peticion.request.headers.has('Authorization')).toBe(false);
    peticion.flush(transcripcion);
  });

  /** Verifica que la transcripción del backend llegue sin cambios al suscriptor. */
  it('entrega el texto transcrito', () => {
    let recibido: unknown;

    servicio.transcribir(notaDeVoz()).subscribe((r) => (recibido = r));
    peticionTranscribir().flush(transcripcion);

    expect(recibido).toEqual(transcripcion);
  });

  /** Verifica que el error del backend, por ejemplo un audio demasiado grande, llegue al suscriptor. */
  it('propaga el error del backend', () => {
    let estado: number | undefined;

    servicio.transcribir(notaDeVoz()).subscribe({ error: (error) => (estado = error.status) });
    peticionTranscribir().flush('El audio supera el máximo permitido', {
      status: 413,
      statusText: 'Payload Too Large',
    });

    expect(estado).toBe(413);
  });
});
