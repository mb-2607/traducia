import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterModule } from '@angular/router';
import { Navbarcomponent } from './navbarcomponent';

/**
 * Pruebas unitarias de {@link Navbarcomponent}.
 *
 * Validan el logo y los enlaces de la barra de navegación, tanto con los valores por defecto como
 * con los que recibe del componente padre.
 */
describe('Navbarcomponent', () => {
  let fixture: ComponentFixture<Navbarcomponent>;
  let html: HTMLElement;

  const enlaces = () =>
    Array.from(html.querySelectorAll<HTMLAnchorElement>('.navbar-action')).map((a) =>
      a.getAttribute('href'),
    );

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RouterModule.forRoot([])],
      declarations: [Navbarcomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Navbarcomponent);
    html = fixture.nativeElement;
    await fixture.whenStable();
  });

  /** Verifica la imagen y el texto alternativo del logo. */
  it('muestra el logo de TraducIA', () => {
    const logo = html.querySelector<HTMLImageElement>('.navbar-logo');

    expect(logo?.getAttribute('src')).toBe('assets/logoempresa.png');
    expect(logo?.alt).toBe('TraducIA');
  });

  /** Verifica las rutas por defecto de los enlaces de plan y perfil. */
  it('lleva a planes y a perfil por defecto', () => {
    expect(enlaces()).toEqual(['/planes', '/perfil']);
  });

  /**
   * Verifica que las entradas `planRoute`, `profileRoute` y `logoSrc` cambien los enlaces y el
   * logo.
   */
  it('permite cambiar las rutas y el logo desde el componente padre', async () => {
    fixture.componentRef.setInput('planRoute', '/admin/planes');
    fixture.componentRef.setInput('profileRoute', '/admin/perfil');
    fixture.componentRef.setInput('logoSrc', 'assets/iconoempresa.png');
    await fixture.whenStable();

    expect(enlaces()).toEqual(['/admin/planes', '/admin/perfil']);
    expect(html.querySelector('.navbar-logo')?.getAttribute('src')).toBe('assets/iconoempresa.png');
  });
});
