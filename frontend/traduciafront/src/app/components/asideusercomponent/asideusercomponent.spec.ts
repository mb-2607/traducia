import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterModule } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import type { Mock } from 'vitest';
import { TemaService } from '../../services/temaservice';
import { AsideUserComponent } from './asideusercomponent';

/**
 * Pruebas unitarias de {@link AsideUserComponent}.
 *
 * Validan el panel lateral del área de usuario: su estado inicial según el tamaño de pantalla, el
 * botón para plegarlo, los enlaces a cada sección y el cambio de tema. {@link TemaService} se
 * simula con un `BehaviorSubject` para controlar el tema desde la prueba.
 */
describe('AsideUserComponent', () => {
  let fixture: ComponentFixture<AsideUserComponent>;
  let html: HTMLElement;
  let temaOscuro$: BehaviorSubject<boolean>;
  let temaService: { getTemaOscuroAsObservable: () => unknown; setTemaOscuro: Mock };

  const botonPanel = () => html.querySelector('.lateral-cabecera button') as HTMLButtonElement;
  const botonTema = () => html.querySelector('.boton-tema') as HTMLButtonElement;
  const navegacion = () => html.querySelector('#traductor-navegacion') as HTMLElement;

  /** Crea el componente y espera a que termine el primer renderizado. */
  async function crearComponente(): Promise<void> {
    fixture = TestBed.createComponent(AsideUserComponent);
    html = fixture.nativeElement;
    await fixture.whenStable();
  }

  beforeEach(async () => {
    temaOscuro$ = new BehaviorSubject(true);
    temaService = {
      getTemaOscuroAsObservable: () => temaOscuro$.asObservable(),
      setTemaOscuro: vi.fn((valor: boolean) => temaOscuro$.next(valor)),
    };

    await TestBed.configureTestingModule({
      imports: [RouterModule.forRoot([])],
      declarations: [AsideUserComponent],
      providers: [{ provide: TemaService, useValue: temaService }],
    }).compileComponents();
  });

  /** Pantallas grandes: jsdom no implementa `matchMedia`, así que el panel arranca abierto. */
  describe('en escritorio', () => {
    beforeEach(async () => {
      await crearComponente();
    });

    /**
     * Verifica que en escritorio el panel empiece abierto, con su título visible y la navegación
     * sin ocultar.
     */
    it('empieza con el panel abierto', () => {
      expect(fixture.componentInstance.panelAbierto).toBe(true);
      expect(html.querySelector('.lateral-titulo')?.textContent).toBe('Traducciones');
      expect(navegacion().hidden).toBe(false);
    });

    /**
     * Verifica que la navegación lateral apunte a nueva traducción, biblioteca de archivos,
     * biblioteca de textos e historial.
     */
    it('enlaza a las secciones del dashboard', () => {
      const rutas = Array.from(html.querySelectorAll('nav a')).map((a) => a.getAttribute('href'));

      expect(rutas).toEqual([
        '/dashboard/new',
        '/dashboard/biblioteca/archivos',
        '/dashboard/biblioteca/texto',
        '/dashboard/historial',
      ]);
    });

    /**
     * Verifica que el botón de la cabecera alterne el panel y actualice `aria-expanded`, la
     * etiqueta accesible, la clase `plegado` del host y la visibilidad de la navegación.
     */
    it('pliega y despliega el panel con el botón de la cabecera', async () => {
      botonPanel().click();
      await fixture.whenStable();

      expect(botonPanel().getAttribute('aria-expanded')).toBe('false');
      expect(botonPanel().getAttribute('aria-label')).toBe('Expandir panel');
      expect(html.classList).toContain('plegado');
      expect(navegacion().hidden).toBe(true);

      botonPanel().click();
      await fixture.whenStable();

      expect(html.classList).not.toContain('plegado');
      expect(navegacion().hidden).toBe(false);
    });

    /** Verifica que con el tema oscuro activo el botón ofrezca cambiar al tema claro. */
    it('muestra la opción de pasar a tema claro cuando el oscuro está activo', () => {
      expect(botonTema().getAttribute('aria-label')).toBe('Activar tema claro');
      expect(botonTema().textContent).toContain('Tema claro');
    });

    /**
     * Verifica que el botón de tema llame a `setTemaOscuro` con el valor contrario y que la
     * interfaz refleje el nuevo tema.
     */
    it('cambia el tema al pulsar el botón', async () => {
      botonTema().click();
      await fixture.whenStable();

      expect(temaService.setTemaOscuro).toHaveBeenCalledWith(false);
      expect(botonTema().getAttribute('aria-label')).toBe('Activar tema oscuro');
      expect(botonTema().textContent).toContain('Tema oscuro');
    });

    /**
     * Verifica que el componente siga los cambios de tema emitidos por el servicio aunque no los
     * haya provocado él.
     */
    it('se entera si el tema cambia desde otra parte de la aplicación', async () => {
      temaOscuro$.next(false);
      await fixture.whenStable();

      expect(fixture.componentInstance.temaOscuro).toBe(false);
    });
  });

  /**
   * Pantallas móviles: se simula `matchMedia` para que la consulta `(max-width: 44rem)` coincida.
   */
  describe('en pantallas pequeñas', () => {
    const ventana = document.defaultView!;

    beforeEach(async () => {
      Object.defineProperty(ventana, 'matchMedia', {
        configurable: true,
        value: vi.fn().mockReturnValue({ matches: true }),
      });
      await crearComponente();
    });

    afterEach(() => {
      Reflect.deleteProperty(ventana, 'matchMedia');
    });

    /** Verifica que en pantallas pequeñas el panel empiece plegado y sin el título visible. */
    it('empieza con el panel plegado', () => {
      expect(ventana.matchMedia).toHaveBeenCalledWith('(max-width: 44rem)');
      expect(fixture.componentInstance.panelAbierto).toBe(false);
      expect(html.classList).toContain('plegado');
      expect(html.querySelector('.lateral-titulo')).toBeNull();
    });
  });
});
