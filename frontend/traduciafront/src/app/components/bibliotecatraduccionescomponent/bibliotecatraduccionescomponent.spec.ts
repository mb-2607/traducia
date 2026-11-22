import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { Bibliotecatraduccionescomponent } from './bibliotecatraduccionescomponent';

/** Traducciones guardadas de ejemplo, de ambos tipos. */
const GUARDADAS = [
  { id: '1', titulo: 'Contrato de arriendo', tipo: 'archivo', resultado: 'Lease agreement' },
  { id: '2', titulo: 'Correo para el profesor', tipo: 'texto', resultado: 'Dear professor' },
  { id: '3', titulo: 'Hoja de vida', tipo: 'archivo', resultado: 'Résumé' },
] as const;

/**
 * Pruebas unitarias de {@link Bibliotecatraduccionescomponent}.
 *
 * Validan el título según el tipo configurado en la ruta, el filtrado por tipo, la búsqueda por
 * título y la diferencia entre una biblioteca vacía y una búsqueda sin coincidencias. La ruta
 * activa se simula con un `ActivatedRoute` falso.
 */
describe('Bibliotecatraduccionescomponent', () => {
  let fixture: ComponentFixture<Bibliotecatraduccionescomponent>;
  let html: HTMLElement;

  /** Configura la ruta con el tipo indicado y crea el componente. */
  async function abrirBiblioteca(tipo?: string): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [RouterModule.forRoot([])],
      declarations: [Bibliotecatraduccionescomponent],
      providers: [{ provide: ActivatedRoute, useValue: { snapshot: { data: { tipo } } } }],
    }).compileComponents();

    fixture = TestBed.createComponent(Bibliotecatraduccionescomponent);
    html = fixture.nativeElement;
    await fixture.whenStable();
  }

  /** Carga las traducciones de ejemplo en la señal interna del componente. */
  async function cargarGuardadas(): Promise<void> {
    // Todavía no existe un servicio que llene la biblioteca, así que se cargan a mano.
    fixture.componentInstance['traducciones'].set(GUARDADAS);
    await fixture.whenStable();
  }

  /** Escribe en el buscador como lo haría el usuario. */
  async function buscar(texto: string): Promise<void> {
    const campo = html.querySelector('#buscar-biblioteca') as HTMLInputElement;
    campo.value = texto;
    campo.dispatchEvent(new Event('input'));
    await fixture.whenStable();
  }

  const titulos = () =>
    Array.from(html.querySelectorAll('.biblioteca-nombre')).map((s) => s.textContent);

  /**
   * Verifica el título de la vista para cada tipo de ruta, incluido un tipo desconocido que cae en
   * la vista general.
   */
  it.each([
    ['archivo', 'Archivos traducidos'],
    ['texto', 'Textos traducidos'],
    [undefined, 'Biblioteca de traducciones'],
    ['otro', 'Biblioteca de traducciones'],
  ])('con el tipo %s muestra el título "%s"', async (tipo, titulo) => {
    await abrirBiblioteca(tipo);

    expect(html.querySelector('h1')?.textContent).toBe(titulo);
  });

  /** Verifica el contador en cero y el mensaje de biblioteca vacía. */
  it('avisa cuando todavía no hay traducciones guardadas', async () => {
    await abrirBiblioteca('texto');

    expect(html.querySelector('.biblioteca-contador')?.textContent).toContain('Resultados: 0');
    expect(html.querySelector('.biblioteca-vacia h2')?.textContent).toBe(
      'Aún no hay traducciones guardadas en esta vista',
    );
  });

  /** Verifica que sin tipo en la ruta se muestren archivos y textos. */
  it('en la vista general muestra todos los tipos', async () => {
    await abrirBiblioteca();
    await cargarGuardadas();

    expect(titulos()).toEqual(['Contrato de arriendo', 'Correo para el profesor', 'Hoja de vida']);
  });

  /** Verifica que la ruta de archivos muestre solo archivos y que el contador lo refleje. */
  it('filtra por el tipo configurado en la ruta', async () => {
    await abrirBiblioteca('archivo');
    await cargarGuardadas();

    expect(titulos()).toEqual(['Contrato de arriendo', 'Hoja de vida']);
    expect(html.querySelector('.biblioteca-contador')?.textContent).toContain('Resultados: 2');
  });

  /** Verifica que la búsqueda ignore las mayúsculas y los espacios alrededor del texto. */
  it('busca por título sin importar mayúsculas', async () => {
    await abrirBiblioteca();
    await cargarGuardadas();

    await buscar('  HOJA ');

    expect(titulos()).toEqual(['Hoja de vida']);
  });

  /**
   * Verifica que, si hay traducciones pero ninguna coincide, se muestre «No encontramos
   * coincidencias» en lugar del mensaje de biblioteca vacía.
   */
  it('diferencia una búsqueda sin coincidencias de una biblioteca vacía', async () => {
    await abrirBiblioteca('texto');
    await cargarGuardadas();

    await buscar('contrato');

    expect(titulos()).toEqual([]);
    expect(html.querySelector('.biblioteca-vacia h2')?.textContent).toBe(
      'No encontramos coincidencias',
    );
  });

  /** Verifica que cada entrada muestre su tipo y el texto traducido. */
  it('muestra el contenido traducido de cada entrada', async () => {
    await abrirBiblioteca('texto');
    await cargarGuardadas();

    expect(html.querySelector('.biblioteca-detalle p')?.textContent).toBe('Dear professor');
    expect(html.querySelector('.biblioteca-tipo')?.textContent?.trim()).toBe('Texto');
  });
});
