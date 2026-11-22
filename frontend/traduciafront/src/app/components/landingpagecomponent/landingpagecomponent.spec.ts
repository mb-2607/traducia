import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, RouterModule } from '@angular/router';
import { Landingpagecomponent } from './landingpagecomponent';

/**
 * Pruebas unitarias de {@link Landingpagecomponent}.
 *
 * Validan la sección de planes, el formato de precios en pesos colombianos, la demostración de
 * traducción por idioma y la navegación al registro con el plan elegido.
 */
describe('Landingpagecomponent', () => {
  let fixture: ComponentFixture<Landingpagecomponent>;
  let component: Landingpagecomponent;
  let html: HTMLElement;

  const botonIdioma = (texto: string): HTMLButtonElement => {
    const encontrado = Array.from(
      html.querySelectorAll<HTMLButtonElement>('.demo-header button'),
    ).find((b) => b.textContent?.trim() === texto);
    if (!encontrado) {
      throw new Error(`No hay un botón «${texto}» en pantalla`);
    }
    return encontrado;
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RouterModule.forRoot([])],
      declarations: [Landingpagecomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Landingpagecomponent);
    component = fixture.componentInstance;
    html = fixture.nativeElement;
    await fixture.whenStable();
  });

  /**
   * Verifica que se muestren los tres planes y que solo orIA aparezca destacado como recomendado.
   */
  it('muestra los tres planes con orIA como recomendado', () => {
    const nombres = Array.from(html.querySelectorAll('.plan-name')).map((h) => h.textContent);
    const destacada = html.querySelector('.pricing-card.featured');

    expect(nombres).toEqual(['Free Pass', 'orIA', 'InfinitIA']);
    expect(html.querySelectorAll('.popular-badge')).toHaveLength(1);
    expect(destacada?.querySelector('.plan-name')?.textContent).toBe('orIA');
  });

  /**
   * Verifica el formato de precios con el separador de miles colombiano y el caso especial de `$0`.
   */
  it('formatea los precios en pesos colombianos', () => {
    expect(component.formatearPrecio(0)).toBe('$0');
    expect(component.formatearPrecio(19900)).toBe('$19.900');
    expect(component.formatearPrecio(39900)).toBe('$39.900');
  });

  /** Verifica que la tarjeta de InfinitIA describa archivos ilimitados y tres motores de IA. */
  it('describe los archivos ilimitados del plan InfinitIA', () => {
    const ultima = html.querySelectorAll('.pricing-card')[2];

    expect(ultima.textContent).toContain('Archivos PDF ilimitados por día');
    expect(ultima.textContent).toContain('Acceso a 3 motores de IA');
  });

  /** Verifica que la demostración arranque en inglés con su botón resaltado. */
  it('empieza la demostración en inglés', () => {
    expect(component.idiomaSeleccionado).toBe('EN');
    expect(botonIdioma('Inglés').classList).toContain('btn-primary-custom');
  });

  /**
   * Verifica que al pulsar Francés cambien el texto, la etiqueta del idioma y el botón resaltado.
   */
  it('cambia el texto de la demostración al elegir otro idioma', async () => {
    botonIdioma('Francés').click();
    await fixture.whenStable();

    expect(html.querySelector('.bg-highlight .demo-text')?.textContent).toContain(
      "L'intelligence artificielle",
    );
    expect(html.querySelector('.lang-tag.accent')?.textContent).toContain('(FR)');
    expect(botonIdioma('Francés').classList).toContain('btn-primary-custom');
    expect(botonIdioma('Inglés').classList).not.toContain('btn-primary-custom');
  });

  /** Verifica el texto de demostración en alemán. */
  it('muestra la traducción al alemán', () => {
    component.cambiarIdiomaDemo('DE');

    expect(component.textoDemoDestino).toContain('Künstliche Intelligenz');
  });

  /**
   * Verifica que un idioma sin texto de ejemplo cambie la selección pero conserve la última
   * traducción.
   */
  it('conserva el último texto si el idioma no tiene demostración', () => {
    const anterior = component.textoDemoDestino;

    component.cambiarIdiomaDemo('IT');

    expect(component.idiomaSeleccionado).toBe('IT');
    expect(component.textoDemoDestino).toBe(anterior);
  });

  /** Verifica que elegir un plan navegue a `/registro` con el plan como parámetro de consulta. */
  it('lleva al registro con el plan elegido', () => {
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    component.seleccionarPlan('oria');

    expect(navegar).toHaveBeenCalledWith(['/registro'], { queryParams: { plan: 'oria' } });
  });
});
