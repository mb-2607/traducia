import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import type { Idioma } from '../models/idiomamodel';
import { IdiomaService } from './idiomaservice';

const URL_IDIOMAS = 'http://localhost:8080/idiomas';

/**
 * Pruebas unitarias de {@link IdiomaService}.
 *
 * Validan la consulta del catálogo de idiomas y la etiqueta de voz que se asigna a cada uno. Se usa
 * `HttpTestingController` para simular el backend.
 */
describe('IdiomaService', () => {
  let servicio: IdiomaService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    servicio = TestBed.inject(IdiomaService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  /** Verifica que cada idioma se adapte con su código, su nombre y su etiqueta de voz. */
  it('consulta los idiomas y asigna la voz de cada uno', () => {
    let idiomas: Idioma[] = [];

    servicio.getListaIdioma().subscribe((lista) => (idiomas = lista));

    const peticion = httpMock.expectOne(URL_IDIOMAS);
    expect(peticion.request.method).toBe('GET');
    peticion.flush([
      { codigo: 'es', nombre: 'Español' },
      { codigo: 'en', nombre: 'Inglés' },
      { codigo: 'pt', nombre: 'Portugués' },
    ]);

    expect(idiomas).toEqual([
      { id: 'es', nombre: 'Español', voz: 'es-CO' },
      { id: 'en', nombre: 'Inglés', voz: 'en-US' },
      { id: 'pt', nombre: 'Portugués', voz: 'pt-BR' },
    ]);
  });

  /**
   * Verifica que el código se normalice para elegir la voz sin modificar el identificador original.
   */
  it('reconoce el código aunque venga en mayúsculas o con espacios', () => {
    let idiomas: Idioma[] = [];

    servicio.getListaIdioma().subscribe((lista) => (idiomas = lista));
    httpMock.expectOne(URL_IDIOMAS).flush([{ codigo: ' FR ', nombre: 'Francés' }]);

    expect(idiomas[0].voz).toBe('fr-FR');
    expect(idiomas[0].id).toBe(' FR ');
  });

  /** Verifica las voces de respaldo: el propio código, o `es-CO` si el código viene vacío. */
  it('usa el propio código como voz para idiomas sin etiqueta conocida', () => {
    let idiomas: Idioma[] = [];

    servicio.getListaIdioma().subscribe((lista) => (idiomas = lista));
    httpMock.expectOne(URL_IDIOMAS).flush([
      { codigo: 'ja', nombre: 'Japonés' },
      { codigo: '  ', nombre: 'Sin código' },
    ]);

    expect(idiomas.map((idioma) => idioma.voz)).toEqual(['ja', 'es-CO']);
  });

  /** Verifica que el error del backend llegue al suscriptor. */
  it('propaga el error cuando el backend no responde', () => {
    let estado: number | undefined;

    servicio.getListaIdioma().subscribe({ error: (error) => (estado = error.status) });
    httpMock.expectOne(URL_IDIOMAS).flush(null, { status: 503, statusText: 'Service Unavailable' });

    expect(estado).toBe(503);
  });
});
