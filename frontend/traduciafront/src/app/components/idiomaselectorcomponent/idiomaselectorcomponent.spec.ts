import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import type { Idioma } from '../../models/idiomamodel';
import { Idiomaselectorcomponent } from './idiomaselectorcomponent';

/** Idiomas de ejemplo que recibe el selector. */
const IDIOMAS: Idioma[] = [
  { id: 'es', nombre: 'Español', voz: 'es-CO' },
  { id: 'en', nombre: 'Inglés', voz: 'en-US' },
  { id: 'fr', nombre: 'Francés', voz: 'fr-FR' },
];

/**
 * Pruebas unitarias de {@link Idiomaselectorcomponent}.
 *
 * Validan que el selector muestre el título y los idiomas recibidos, el texto de carga, la
 * sincronización en ambos sentidos con el `FormControl` y el estado de validación accesible.
 */
describe('Idiomaselectorcomponent', () => {
  let fixture: ComponentFixture<Idiomaselectorcomponent>;
  let html: HTMLElement;
  let control: FormControl<String>;

  const selector = () => html.querySelector('select') as HTMLSelectElement;
  const opciones = () => Array.from(selector().options).map((opcion) => opcion.textContent?.trim());

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule],
      declarations: [Idiomaselectorcomponent],
    }).compileComponents();

    control = new FormControl<String>('', { nonNullable: true, validators: Validators.required });

    fixture = TestBed.createComponent(Idiomaselectorcomponent);
    fixture.componentRef.setInput('titulo', 'Idioma de origen');
    fixture.componentRef.setInput('control', control);
    fixture.componentRef.setInput('listaIdioma', IDIOMAS);
    fixture.componentRef.setInput('cargando', false);
    html = fixture.nativeElement;
    await fixture.whenStable();
  });

  /**
   * Verifica el título del campo y que las opciones incluyan el texto de ayuda seguido de los
   * idiomas.
   */
  it('muestra el título y los idiomas disponibles', () => {
    expect(html.querySelector('.campo span')?.textContent).toBe('Idioma de origen');
    expect(opciones()).toEqual(['Elige un idioma', 'Español', 'Inglés', 'Francés']);
  });

  /** Verifica que la primera opción indique la carga mientras `cargando` es verdadero. */
  it('avisa mientras se cargan los idiomas', async () => {
    fixture.componentRef.setInput('cargando', true);
    await fixture.whenStable();

    expect(opciones()[0]).toBe('Cargando idiomas…');
  });

  /** Verifica que elegir una opción actualice el valor del control. */
  it('actualiza el control al elegir un idioma', () => {
    selector().value = 'fr';
    selector().dispatchEvent(new Event('change'));

    expect(control.value).toBe('fr');
  });

  /** Verifica que un cambio en el control se refleje en el selector. */
  it('refleja en pantalla el valor del control', async () => {
    control.setValue('en');
    await fixture.whenStable();

    expect(selector().value).toBe('en');
  });

  /**
   * Verifica que `aria-invalid` solo sea verdadero cuando el control es inválido y ya fue tocado.
   */
  it('marca el selector como inválido solo después de tocarlo', () => {
    expect(selector().getAttribute('aria-invalid')).toBe('false');

    control.markAsTouched();
    fixture.detectChanges();

    expect(selector().getAttribute('aria-invalid')).toBe('true');
  });

  /** Verifica que el selector no se renderice sin un control asociado. */
  it('no muestra el selector si no recibe un control', async () => {
    fixture.componentRef.setInput('control', undefined);
    await fixture.whenStable();

    expect(html.querySelector('select')).toBeNull();
  });
});
