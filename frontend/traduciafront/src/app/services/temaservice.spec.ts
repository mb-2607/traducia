import { TestBed } from '@angular/core/testing';
import { TemaService } from './temaservice';

/**
 * Pruebas unitarias de {@link TemaService}.
 *
 * Validan el tema inicial, la preferencia guardada, el cambio de tema y su aplicación en el
 * atributo `data-tema` del documento.
 */
describe('TemaService', () => {
  const raiz = document.documentElement;

  beforeEach(() => {
    localStorage.clear();
    delete raiz.dataset['tema'];
    TestBed.configureTestingModule({});
  });

  afterEach(() => {
    localStorage.clear();
    delete raiz.dataset['tema'];
  });

  /** Verifica que sin una preferencia guardada se use el tema oscuro. */
  it('usa el tema oscuro cuando no hay una preferencia guardada', () => {
    const servicio = TestBed.inject(TemaService);
    let oscuro: boolean | undefined;
    servicio.getTemaOscuroAsObservable().subscribe((valor) => (oscuro = valor));

    expect(oscuro).toBe(true);
    expect(raiz.dataset['tema']).toBe('oscuro');
  });

  /** Verifica que se aplique el tema claro si así quedó guardado. */
  it('respeta la preferencia guardada por el usuario', () => {
    localStorage.setItem('tema', 'false');

    TestBed.inject(TemaService);

    expect(raiz.dataset['tema']).toBe('claro');
  });

  /** Verifica que `setTemaOscuro` emita el nuevo valor, lo guarde y lo aplique al documento. */
  it('cambia el tema, lo guarda y avisa a los suscriptores', () => {
    const servicio = TestBed.inject(TemaService);
    const valores: boolean[] = [];
    servicio.getTemaOscuroAsObservable().subscribe((valor) => valores.push(valor));

    servicio.setTemaOscuro(false);

    expect(valores).toEqual([true, false]);
    expect(raiz.dataset['tema']).toBe('claro');
    expect(localStorage.getItem('tema')).toBe('false');
  });

  /** Verifica el valor de `data-tema` para cada tema. */
  it('aplica el atributo data-tema en la raíz del documento', () => {
    const servicio = TestBed.inject(TemaService);

    servicio.aplicarTema(false);
    expect(raiz.getAttribute('data-tema')).toBe('claro');

    servicio.aplicarTema(true);
    expect(raiz.getAttribute('data-tema')).toBe('oscuro');
  });
});
