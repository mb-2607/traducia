import { TestBed } from '@angular/core/testing';
import { RouterModule } from '@angular/router';
import { Noencontradocomponent } from './noencontradocomponent';

/**
 * Pruebas unitarias de {@link Noencontradocomponent}.
 *
 * Validan el contenido de la página 404 y los enlaces para salir de ella.
 */
describe('Noencontradocomponent', () => {
  let html: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RouterModule.forRoot([])],
      declarations: [Noencontradocomponent],
    }).compileComponents();

    const fixture = TestBed.createComponent(Noencontradocomponent);
    html = fixture.nativeElement;
    await fixture.whenStable();
  });

  /** Verifica que se muestren el código 404 y su mensaje. */
  it('muestra el código 404 con su mensaje', () => {
    expect(html.querySelector('.error-code')?.textContent?.trim()).toBe('404');
    expect(html.querySelector('.error-msg')?.textContent).toContain(
      'no es lo que estabas buscando',
    );
  });

  /** Verifica que la página tenga dos enlaces y que el del tesoro lleve a la raíz. */
  it('ofrece enlaces para salir de la página', () => {
    const enlaces = html.querySelectorAll('a[href]');

    expect(enlaces).toHaveLength(2);
    expect(html.querySelector('.treasure-btn')?.getAttribute('href')).toBe('/');
  });
});
