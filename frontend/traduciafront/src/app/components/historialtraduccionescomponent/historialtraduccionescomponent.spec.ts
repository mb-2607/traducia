import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterModule } from '@angular/router';
import { Subject, of, throwError } from 'rxjs';
import type { Mock } from 'vitest';
import type {
  PageResponse,
  SolicitudTraduccionResponseDTO,
  TraduccionResponseDTO,
} from '../../models/traduccionmodel';
import { TraduccionService } from '../../services/traduccionservice';
import { Historialtraduccionescomponent } from './historialtraduccionescomponent';

/** Resultado del proveedor DeepL; `null` representa una traducción fallida. */
const deepl = (texto: string | null): TraduccionResponseDTO => ({
  estado: texto === null ? 'FALLIDA' : 'EXITOSA',
  textoTraducido: texto,
  tipoProveedor: 'TRADICIONAL',
  proveedorTraduccion: 'DEEPL',
});

/** Resultado del proveedor Gemma; `null` representa una traducción fallida. */
const gemma = (texto: string | null): TraduccionResponseDTO => ({
  estado: texto === null ? 'FALLIDA' : 'EXITOSA',
  textoTraducido: texto,
  tipoProveedor: 'IA',
  proveedorTraduccion: 'GEMMA',
});

/** Crea una solicitud de traducción del español al inglés. */
function solicitud(
  id: number,
  textoOriginal: string,
  fechaCreacion: string,
  listaTraduccion: TraduccionResponseDTO[] = [deepl('Translated text')],
): SolicitudTraduccionResponseDTO {
  return { id, textoOriginal, fechaCreacion, origen: 'es', destino: 'en', listaTraduccion };
}

/** Envuelve las solicitudes en una página como la que devuelve Spring Data. */
function pagina(
  content: SolicitudTraduccionResponseDTO[],
  extra: Partial<PageResponse<SolicitudTraduccionResponseDTO>> = {},
): PageResponse<SolicitudTraduccionResponseDTO> {
  return {
    content,
    number: 0,
    size: 10,
    totalPages: 1,
    totalElements: content.length,
    first: true,
    last: true,
    empty: content.length === 0,
    ...extra,
  };
}

/**
 * Pruebas unitarias de {@link Historialtraduccionescomponent}.
 *
 * Validan la carga paginada del historial, la adaptación de cada solicitud (título recortado,
 * resumen y orden por fecha), la búsqueda y el filtro por tipo, el manejo de errores y la consulta
 * del detalle por proveedor al desplegar una entrada. {@link TraduccionService} se simula con
 * funciones de Vitest.
 */
describe('Historialtraduccionescomponent', () => {
  let fixture: ComponentFixture<Historialtraduccionescomponent>;
  let html: HTMLElement;
  let servicio: { obtenerHistorial: Mock; obtenerPorId: Mock };

  const titulos = () =>
    Array.from(html.querySelectorAll('.traduccion-info strong')).map((s) => s.textContent);
  const botonPorTexto = (texto: string): HTMLButtonElement => {
    const encontrado = Array.from(html.querySelectorAll('button')).find(
      (b) => b.textContent?.trim() === texto,
    );
    if (!encontrado) {
      throw new Error(`No hay un botón «${texto}» en pantalla`);
    }
    return encontrado;
  };

  /** Crea el componente y espera la carga inicial. */
  async function abrirHistorial(): Promise<void> {
    fixture = TestBed.createComponent(Historialtraduccionescomponent);
    html = fixture.nativeElement;
    await fixture.whenStable();
  }

  /** Devuelve la primera entrada del historial o falla si la lista está vacía. */
  function primeraEntrada(): Element {
    const entrada = html.querySelector('.historial-lista li');
    if (!entrada) {
      throw new Error('El historial no muestra ninguna entrada');
    }
    return entrada;
  }

  /** Abre o cierra el `<details>` de una entrada y dispara el evento `toggle`. */
  async function desplegar(entrada: Element, abierta = true): Promise<void> {
    const detalle = entrada.querySelector('details') as HTMLDetailsElement;
    detalle.open = abierta;
    detalle.dispatchEvent(new Event('toggle'));
    await fixture.whenStable();
  }

  beforeEach(async () => {
    servicio = {
      obtenerHistorial: vi.fn().mockReturnValue(of(pagina([]))),
      obtenerPorId: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [RouterModule.forRoot([])],
      declarations: [Historialtraduccionescomponent],
      providers: [{ provide: TraduccionService, useValue: servicio }],
    }).compileComponents();
  });

  /** Verifica que al iniciar se pida la página 0 con 10 registros. */
  it('pide la primera página al abrir el historial', async () => {
    await abrirHistorial();

    expect(servicio.obtenerHistorial).toHaveBeenCalledWith(0, 10);
  });

  /** Verifica el mensaje de carga mientras el servicio no responde. */
  it('muestra un mensaje mientras carga', async () => {
    servicio.obtenerHistorial.mockReturnValue(new Subject());

    await abrirHistorial();

    expect(html.textContent).toContain('Cargando historial...');
  });

  /** Verifica el mensaje para un historial vacío. */
  it('avisa cuando el usuario todavía no tiene traducciones', async () => {
    await abrirHistorial();

    expect(html.querySelector('.historial-vacio h2')?.textContent).toBe(
      'Aún no tienes traducciones',
    );
  });

  /**
   * Verifica que las entradas se ordenen por fecha descendente y que el elemento `time` conserve la
   * fecha original.
   */
  it('ordena las traducciones de la más reciente a la más antigua', async () => {
    servicio.obtenerHistorial.mockReturnValue(
      of(
        pagina([
          solicitud(1, 'Hola', '2026-03-01T10:00:00'),
          solicitud(2, 'Buenas noches', '2026-05-20T21:15:00'),
          solicitud(3, 'Gracias', '2026-04-11T08:30:00'),
        ]),
      ),
    );

    await abrirHistorial();

    expect(titulos()).toEqual(['Buenas noches', 'Gracias', 'Hola']);
    expect(html.querySelector('time')?.getAttribute('datetime')).toBe('2026-05-20T21:15:00');
  });

  /**
   * Verifica que los textos de más de 60 caracteres se recorten y terminen en puntos suspensivos.
   */
  it('recorta los títulos largos a 60 caracteres', async () => {
    const largo = 'Este es un texto bastante largo que el usuario pegó en el traductor ayer';
    servicio.obtenerHistorial.mockReturnValue(of(pagina([solicitud(1, largo, '2026-05-10')])));

    await abrirHistorial();

    expect(titulos()[0]).toBe(`${largo.slice(0, 60)}...`);
  });

  /**
   * Verifica que el resumen use la primera traducción exitosa y muestre «Sin resultado disponible»
   * si todas fallaron.
   */
  it('usa la primera traducción exitosa como resumen', async () => {
    servicio.obtenerHistorial.mockReturnValue(
      of(
        pagina([
          solicitud(1, 'Buenos días', '2026-05-10', [deepl(null), gemma('Good morning')]),
          solicitud(2, 'Hasta luego', '2026-05-09', [deepl(null)]),
        ]),
      ),
    );

    await abrirHistorial();
    const resumenes = Array.from(html.querySelectorAll('.traduccion-detalle > div:last-child p'));

    expect(resumenes.map((p) => p.textContent)).toEqual([
      'Good morning',
      'Sin resultado disponible',
    ]);
  });

  /**
   * Verifica que el contador muestre los resultados visibles y el total de elementos del backend.
   */
  it('muestra cuántos resultados hay en total', async () => {
    servicio.obtenerHistorial.mockReturnValue(
      of(pagina([solicitud(1, 'Hola', '2026-05-10')], { totalElements: 23, totalPages: 3 })),
    );

    await abrirHistorial();

    expect(html.querySelector('.historial-contador')?.textContent).toMatch(/Resultados: 1\s+de 23/);
  });

  /** Búsqueda y filtro por tipo sobre la página cargada. */
  describe('búsqueda y filtros', () => {
    beforeEach(async () => {
      servicio.obtenerHistorial.mockReturnValue(
        of(
          pagina([
            solicitud(1, 'Reunión con el cliente', '2026-05-10'),
            solicitud(2, 'Receta de ajiaco', '2026-05-09'),
          ]),
        ),
      );
      await abrirHistorial();
    });

    /** Verifica que la búsqueda revise el texto original sin distinguir mayúsculas. */
    it('busca en el texto original sin importar mayúsculas', async () => {
      const campo = html.querySelector('#buscar-historial') as HTMLInputElement;
      campo.value = 'AJIACO';
      campo.dispatchEvent(new Event('input'));
      await fixture.whenStable();

      expect(titulos()).toEqual(['Receta de ajiaco']);
    });

    /** Verifica el mensaje de «sin coincidencias» al filtrar por un tipo que no tiene registros. */
    it('indica cuando ningún registro coincide con el filtro', async () => {
      const tipo = html.querySelector('#tipo-historial') as HTMLSelectElement;
      tipo.value = 'archivo';
      tipo.dispatchEvent(new Event('change'));
      await fixture.whenStable();

      expect(titulos()).toEqual([]);
      expect(html.querySelector('.historial-vacio h2')?.textContent).toBe(
        'No encontramos coincidencias',
      );
    });

    /** Verifica que un valor de filtro desconocido no oculte registros. */
    it('ignora filtros que no existen', async () => {
      fixture.componentInstance['actualizarFiltro']('favoritos');
      await fixture.whenStable();

      expect(titulos()).toHaveLength(2);
    });
  });

  /** Errores al consultar el historial. */
  describe('cuando falla la consulta', () => {
    beforeEach(async () => {
      servicio.obtenerHistorial.mockReturnValue(throwError(() => new Error('sin conexión')));
      await abrirHistorial();
    });

    /** Verifica que se muestre el mensaje de error cuando el servicio falla. */
    it('muestra el error', () => {
      expect(html.querySelector('[role="alert"]')?.textContent).toContain(
        'No se pudo cargar el historial de traducciones.',
      );
    });

    /**
     * Verifica que el botón Reintentar vuelva a consultar y muestre los datos si la nueva consulta
     * funciona.
     */
    it('permite reintentar la carga', async () => {
      servicio.obtenerHistorial.mockReturnValue(of(pagina([solicitud(1, 'Hola', '2026-05-10')])));

      botonPorTexto('Reintentar').click();
      await fixture.whenStable();

      expect(servicio.obtenerHistorial).toHaveBeenCalledTimes(2);
      expect(titulos()).toEqual(['Hola']);
    });
  });

  /** Navegación entre las páginas del backend (3 páginas de ejemplo). */
  describe('paginación', () => {
    beforeEach(async () => {
      servicio.obtenerHistorial.mockImplementation((numero: number) =>
        of(
          pagina([solicitud(numero + 1, `Página ${numero + 1}`, '2026-05-10')], {
            number: numero,
            totalPages: 3,
            totalElements: 25,
          }),
        ),
      );
      await abrirHistorial();
    });

    /** Verifica el texto de la página actual y que en la primera página solo se pueda avanzar. */
    it('muestra la página actual y bloquea el botón Anterior en la primera', () => {
      expect(html.querySelector('.historial-paginacion span')?.textContent).toBe('Página 1 de 3');
      expect(botonPorTexto('Anterior').disabled).toBe(true);
      expect(botonPorTexto('Siguiente').disabled).toBe(false);
    });

    /** Verifica que Siguiente y Anterior pidan al servicio la página correspondiente. */
    it('avanza y retrocede entre páginas', async () => {
      botonPorTexto('Siguiente').click();
      await fixture.whenStable();
      expect(servicio.obtenerHistorial).toHaveBeenLastCalledWith(1, 10);
      expect(titulos()).toEqual(['Página 2']);

      botonPorTexto('Anterior').click();
      await fixture.whenStable();
      expect(servicio.obtenerHistorial).toHaveBeenLastCalledWith(0, 10);
    });

    /**
     * Verifica que en la última página no se pueda avanzar, ni con el botón ni llamando al método.
     */
    it('no pasa de la última página', async () => {
      botonPorTexto('Siguiente').click();
      await fixture.whenStable();
      botonPorTexto('Siguiente').click();
      await fixture.whenStable();

      expect(botonPorTexto('Siguiente').disabled).toBe(true);
      fixture.componentInstance['paginaSiguiente']();
      expect(servicio.obtenerHistorial).toHaveBeenCalledTimes(3);
    });
  });

  /** Consulta del detalle de una traducción al desplegarla. */
  describe('detalle por proveedor', () => {
    beforeEach(async () => {
      servicio.obtenerHistorial.mockReturnValue(
        of(pagina([solicitud(7, 'Buenos días', '2026-05-10', [deepl('Good morning')])])),
      );
      servicio.obtenerPorId.mockReturnValue(
        of(solicitud(7, 'Buenos días', '2026-05-10', [deepl('Good morning'), gemma(null)])),
      );
      await abrirHistorial();
    });

    /**
     * Verifica que al desplegar una entrada se consulte su detalle y se muestre el resultado de
     * cada proveedor, incluido el que falló.
     */
    it('consulta el detalle al desplegar una traducción', async () => {
      await desplegar(primeraEntrada());

      const detalle = html.querySelector('.traduccion-detalle')?.textContent;
      expect(servicio.obtenerPorId).toHaveBeenCalledWith(7);
      expect(detalle).toContain('DEEPL');
      expect(detalle).toContain('Good morning');
      expect(detalle).toContain('No fue posible generar la traducción con este proveedor.');
    });

    /**
     * Verifica que el detalle se consulte una sola vez aunque la entrada se cierre y se vuelva a
     * abrir.
     */
    it('no repite la consulta si el detalle ya se cargó', async () => {
      const entrada = primeraEntrada();

      await desplegar(entrada);
      await desplegar(entrada, false);
      await desplegar(entrada);

      expect(servicio.obtenerPorId).toHaveBeenCalledTimes(1);
    });

    /** Verifica que cerrar una entrada no genere peticiones. */
    it('no consulta nada al cerrar una traducción', async () => {
      await desplegar(primeraEntrada(), false);

      expect(servicio.obtenerPorId).not.toHaveBeenCalled();
    });

    /**
     * Verifica que, si falla el detalle, se siga mostrando el resumen y desaparezca el mensaje de
     * carga.
     */
    it('mantiene el resumen si falla la consulta del detalle', async () => {
      servicio.obtenerPorId.mockReturnValue(throwError(() => new Error('500')));

      await desplegar(primeraEntrada());

      expect(html.querySelector('.traduccion-detalle')?.textContent).toContain('Good morning');
      expect(html.textContent).not.toContain('Cargando detalle...');
    });
  });
});
