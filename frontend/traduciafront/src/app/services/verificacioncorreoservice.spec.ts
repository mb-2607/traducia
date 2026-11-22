import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import type { SolicitudTraduccionResponseDTO } from '../models/traduccionmodel';
import { AuthService } from './authservice';
import { TraduccionService } from './traduccionservice';

const URL_TRADUCTOR = 'http://localhost:8080/traductor';

/** Respuesta de ejemplo del backend para una traducción. */
const respuesta: SolicitudTraduccionResponseDTO = {
  id: 15,
  fechaCreacion: '2026-05-10T09:30:00',
  textoOriginal: 'Buenos días',
  origen: 'es',
  destino: 'en',
  listaTraduccion: [
    {
      estado: 'EXITOSA',
      textoTraducido: 'Good morning',
      tipoProveedor: 'TRADICIONAL',
      proveedorTraduccion: 'DEEPL',
    },
  ],
};

/**
 * Pruebas unitarias de {@link TraduccionService}.
 *
 * Validan las peticiones para traducir, consultar el historial paginado y obtener el detalle de una
 * traducción, incluida la cabecera de autorización. {@link AuthService} se simula y
 * `HttpTestingController` reemplaza al backend.
 */
describe('TraduccionService', () => {
  let servicio: TraduccionService;
  let httpMock: HttpTestingController;
  let jwt: string | null;

  beforeEach(() => {
    jwt = 'jwt-laura';

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: { getJwt: () => jwt } },
      ],
    });

    servicio = TestBed.inject(TraduccionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  /**
   * Verifica el cuerpo de la petición de traducción, la cabecera Bearer y la respuesta recibida.
   */
  it('envía el texto a traducir con el token de la sesión', () => {
    let recibido: SolicitudTraduccionResponseDTO | undefined;

    servicio
      .traducir({ texto: 'Buenos días', origen: 'es', destino: 'en' })
      .subscribe((r) => (recibido = r));

    const peticion = httpMock.expectOne(`${URL_TRADUCTOR}/traducir`);
    expect(peticion.request.method).toBe('POST');
    expect(peticion.request.body).toEqual({ texto: 'Buenos días', origen: 'es', destino: 'en' });
    expect(peticion.request.headers.get('Authorization')).toBe('Bearer jwt-laura');
    peticion.flush(respuesta);

    expect(recibido).toEqual(respuesta);
  });

  /** Verifica que sin sesión no se envíe la cabecera de autorización. */
  it('no agrega la cabecera Authorization si no hay sesión', () => {
    jwt = null;

    servicio.traducir({ texto: 'Hola', origen: 'es', destino: 'fr' }).subscribe();

    const peticion = httpMock.expectOne(`${URL_TRADUCTOR}/traducir`);
    expect(peticion.request.headers.has('Authorization')).toBe(false);
    peticion.flush(respuesta);
  });

  /** Verifica los parámetros por defecto del historial. */
  it('pide la primera página del historial con 10 registros por defecto', () => {
    servicio.obtenerHistorial().subscribe();

    const peticion = httpMock.expectOne((req) => req.url === `${URL_TRADUCTOR}/historial`);
    expect(peticion.request.params.get('page')).toBe('0');
    expect(peticion.request.params.get('size')).toBe('10');
    peticion.flush({ content: [], totalElements: 0, totalPages: 0 });
  });

  /** Verifica que la página y el tamaño indicados viajen como parámetros. */
  it('pide la página y el tamaño indicados', () => {
    servicio.obtenerHistorial(2, 5).subscribe();

    const peticion = httpMock.expectOne((req) => req.url === `${URL_TRADUCTOR}/historial`);
    expect(peticion.request.urlWithParams).toBe(`${URL_TRADUCTOR}/historial?page=2&size=5`);
    peticion.flush({ content: [], totalElements: 0, totalPages: 0 });
  });

  /** Verifica la petición del detalle por identificador. */
  it('consulta el detalle de una traducción por su id', () => {
    let detalle: SolicitudTraduccionResponseDTO | undefined;

    servicio.obtenerPorId(15).subscribe((r) => (detalle = r));

    const peticion = httpMock.expectOne(`${URL_TRADUCTOR}/15`);
    expect(peticion.request.method).toBe('GET');
    peticion.flush(respuesta);

    expect(detalle?.listaTraduccion[0].textoTraducido).toBe('Good morning');
  });
});
