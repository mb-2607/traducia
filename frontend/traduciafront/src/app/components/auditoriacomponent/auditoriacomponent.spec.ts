import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterModule } from '@angular/router';
import { Subject, of, throwError } from 'rxjs';
import type { Mock } from 'vitest';
import type { ActorAuditoria, RegistroAuditoria } from '../../models/auditoriamodel';
import { AuditoriaService } from '../../services/auditoriaservice';
import { Auditoriacomponent } from './auditoriacomponent';

/** Usuaria con plan orIA. */
const laura: ActorAuditoria = {
  id: 4,
  nombre: 'laura',
  correo: 'laura@correo.com',
  tipo: 'USER',
  planActual: 'ORIA',
};

/** Administrador sin plan disponible. */
const admin: ActorAuditoria = {
  id: 1,
  nombre: 'admin',
  correo: 'admin@traducia.com',
  tipo: 'ADMIN',
  planActual: null,
};

/** Proceso automático; trae un plan para comprobar que la vista lo ignora. */
const sistema: ActorAuditoria = {
  id: null,
  nombre: 'Sistema',
  correo: null,
  tipo: 'SYSTEM',
  planActual: 'FREE',
};

/**
 * Crea un registro de auditoría exitoso con los datos mínimos; `cambios` permite ajustar cualquier
 * campo.
 */
function evento(
  id: string,
  fecha: string,
  actor: ActorAuditoria = laura,
  cambios: Partial<RegistroAuditoria> = {},
): RegistroAuditoria {
  return {
    id,
    fecha,
    actor,
    accion: 'POST /traductor/traducir',
    resultado: 'SUCCESS',
    detalle: 'HTTP 200 · Duración: 35 ms · IP: 190.25.1.10',
    ...cambios,
  };
}

/**
 * Eventos de ejemplo: incluyen un fallo, una fecha inválida y un caso en el que el día cambia entre
 * UTC y Bogotá.
 */
const EVENTOS = [
  evento('101', '2026-05-08T14:00:00-05:00'),
  evento('102', '2026-05-10T09:30:00-05:00', admin, {
    accion: 'DELETE /user/eliminar/3',
    resultado: 'FAILURE',
  }),
  evento('103', 'fecha-rota', sistema, { accion: 'GET /plan' }),
  // 10 p. m. del 9 de mayo en Bogotá, aunque en UTC ya sea 10 de mayo.
  evento('104', '2026-05-10T03:00:00Z', laura, { accion: 'GET /traductor/historial' }),
];

/**
 * Pruebas unitarias de {@link Auditoriacomponent}.
 *
 * Validan la consulta de eventos de auditoría y su orden, la presentación del actor y su plan, los
 * filtros (texto, fechas en la zona horaria de Colombia, tipo de actor y resultado), la paginación
 * local y el diálogo de detalle. {@link AuditoriaService} se reemplaza por un doble de prueba.
 */
describe('Auditoriacomponent', () => {
  let fixture: ComponentFixture<Auditoriacomponent>;
  let component: Auditoriacomponent;
  let html: HTMLElement;
  let servicio: { listar: Mock } | null;

  const ids = () =>
    Array.from(html.querySelectorAll('tbody th.auditoria-id')).map((th) => th.textContent?.trim());
  const dialogo = () => html.querySelector('dialog') as HTMLDialogElement;

  /** Espera a que Angular termine de aplicar los cambios pendientes. */
  async function actualizar(): Promise<void> {
    await fixture.whenStable();
  }

  /** Configura el módulo de pruebas con el servicio simulado y crea el componente. */
  async function crearComponente(): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [RouterModule.forRoot([])],
      declarations: [Auditoriacomponent],
      providers: [{ provide: AuditoriaService, useValue: servicio }],
    }).compileComponents();

    fixture = TestBed.createComponent(Auditoriacomponent);
    component = fixture.componentInstance;
    html = fixture.nativeElement;
    await actualizar();
  }

  beforeEach(() => {
    servicio = { listar: vi.fn().mockReturnValue(of(EVENTOS)) };
  });

  /** Carga de eventos desde el servicio. */
  describe('consulta', () => {
    /**
     * Verifica que los eventos se muestren del más reciente al más antiguo y que el resumen indique
     * el total encontrado.
     */
    it('ordena los eventos del más reciente al más antiguo', async () => {
      await crearComponente();

      expect(ids()).toEqual(['102', '104', '101', '103']);
      expect(html.querySelector('.auditoria-resumen')?.textContent).toContain(
        'Eventos encontrados: 4',
      );
    });

    /**
     * Verifica que un evento con fecha no interpretable quede al final y muestre los textos de
     * fecha y hora no disponibles.
     */
    it('deja al final los eventos con fecha inválida', async () => {
      await crearComponente();
      const ultima = html.querySelectorAll('tbody tr')[3];

      expect(ultima.textContent).toContain('Fecha no disponible');
      expect(ultima.textContent).toContain('Hora no disponible');
    });

    /** Verifica que se informe al administrador cuando la consulta termina correctamente. */
    it('confirma que la auditoría se actualizó', async () => {
      await crearComponente();

      expect(html.querySelector('.au-mensaje')?.textContent).toContain(
        'El registro de auditoría se actualizó correctamente.',
      );
    });

    /**
     * Verifica que una fecha en UTC (03:00 Z) se presente con la hora de Bogotá (22:00 del día
     * anterior).
     */
    it('muestra la hora en zona horaria de Colombia', async () => {
      await crearComponente();
      const fila = Array.from(html.querySelectorAll('tbody tr')).find((tr) =>
        tr.textContent?.includes('104'),
      )!;

      expect(fila.querySelector('.au-hora')?.textContent?.trim()).toBe('22:00:00');
    });

    /**
     * Verifica que el resumen indique que se están cargando eventos mientras el servicio no
     * responde.
     */
    it('muestra un estado de carga mientras espera la respuesta', async () => {
      servicio!.listar.mockReturnValue(new Subject());

      await crearComponente();

      expect(html.querySelector('.auditoria-resumen')?.textContent?.trim()).toBe(
        'Cargando eventos…',
      );
    });

    /** Verifica el mensaje de error cuando la primera consulta falla y no hay datos previos. */
    it('informa el error cuando nunca se pudo consultar la auditoría', async () => {
      servicio!.listar.mockReturnValue(throwError(() => new Error('500')));

      await crearComponente();

      expect(html.querySelector('[role="alert"]')?.textContent?.trim()).toBe(
        'No fue posible consultar la auditoría. Inténtalo nuevamente.',
      );
    });

    /**
     * Verifica que, si falla una consulta posterior, la tabla conserve los eventos de la última
     * respuesta correcta y lo explique en el mensaje.
     */
    it('conserva los datos anteriores si falla una actualización', async () => {
      await crearComponente();
      servicio!.listar.mockReturnValue(throwError(() => new Error('500')));

      component['cargarRegistros']();
      await actualizar();

      expect(ids()).toHaveLength(4);
      expect(component['error']()).toBe(
        'No fue posible actualizar la auditoría. Se conservan los datos de la última consulta correcta.',
      );
    });

    /**
     * Verifica que sin un proveedor de {@link AuditoriaService} la tabla quede vacía y se muestre
     * un aviso.
     */
    it('avisa cuando la consulta no está disponible', async () => {
      servicio = null;

      await crearComponente();

      expect(ids()).toEqual([]);
      expect(html.querySelector('.au-mensaje')).not.toBeNull();
    });
  });

  /** Cómo se muestra quién realizó cada acción. */
  describe('presentación del actor', () => {
    beforeEach(async () => {
      await crearComponente();
    });

    const filaDe = (id: string) =>
      Array.from(html.querySelectorAll('tbody tr')).find((tr) =>
        tr.querySelector('th')?.textContent?.includes(id),
      )!;

    /** Verifica que el plan `ORIA` se muestre como «orIA» y se use como color del actor. */
    it('muestra el nombre comercial del plan del usuario', () => {
      expect(filaDe('101').querySelector('.auditoria-plan')?.textContent?.trim()).toBe('orIA');
      expect(filaDe('101').querySelector('.auditoria-actor')?.getAttribute('data-plan')).toBe(
        'ORIA',
      );
    });

    /**
     * Verifica que los eventos del sistema se presenten como proceso automático, sin plan aunque el
     * registro traiga uno.
     */
    it('presenta los procesos automáticos sin plan de usuario', () => {
      const actor = filaDe('103').querySelector('.auditoria-actor-datos')!.textContent;

      expect(actor).toContain('Sistema');
      expect(actor).toContain('Proceso automático');
      expect(actor).toContain('Sin plan de usuario');
    });

    /** Verifica el texto que se muestra cuando el actor no tiene plan registrado. */
    it('indica cuando el plan no está disponible', () => {
      expect(filaDe('102').querySelector('.auditoria-plan')?.textContent?.trim()).toBe(
        'Plan no disponible',
      );
    });
  });

  /** Búsqueda y filtros aplicados sobre la lista cargada. */
  describe('filtros', () => {
    beforeEach(async () => {
      await crearComponente();
    });

    /**
     * Verifica que la búsqueda revise la acción y el correo del actor sin distinguir mayúsculas.
     */
    it('busca por id, nombre, correo o acción', async () => {
      const campo = html.querySelector('#auditoria-busqueda') as HTMLInputElement;
      campo.value = 'ELIMINAR';
      campo.dispatchEvent(new Event('input'));
      await actualizar();

      expect(ids()).toEqual(['102']);

      component['buscar']('laura@correo');
      await actualizar();

      expect(ids()).toEqual(['104', '101']);
    });

    /** Verifica que el filtro por fechas compare el día en Bogotá y no el día en UTC. */
    it('filtra por rango de fechas usando el día en Colombia', async () => {
      component['cambiarDesde']('2026-05-09');
      component['cambiarHasta']('2026-05-09');
      await actualizar();

      expect(ids()).toEqual(['104']);
    });

    /**
     * Verifica que un evento sin fecha válida no aparezca cuando hay un filtro de fechas activo.
     */
    it('excluye los eventos sin fecha cuando hay un rango', async () => {
      component['cambiarDesde']('2026-01-01');
      await actualizar();

      expect(ids()).not.toContain('103');
    });

    /**
     * Verifica que un rango invertido no muestre resultados y presente el mensaje de error del
     * rango.
     */
    it('avisa cuando la fecha inicial es posterior a la final', async () => {
      component['cambiarDesde']('2026-05-10');
      component['cambiarHasta']('2026-05-01');
      await actualizar();

      expect(ids()).toEqual([]);
      expect(html.querySelector('#auditoria-rango-error')?.textContent).toContain(
        'La fecha inicial debe ser anterior o igual a la fecha final.',
      );
    });

    /** Verifica los filtros por tipo de actor y por resultado de la operación. */
    it('filtra por tipo de actor y por resultado', async () => {
      component['cambiarTipoActor']('USER');
      await actualizar();
      expect(ids()).toEqual(['104', '101']);

      component['cambiarTipoActor']('');
      component['cambiarResultado']('FAILURE');
      await actualizar();
      expect(ids()).toEqual(['102']);
    });

    /** Verifica que los valores fuera de las opciones permitidas no se apliquen como filtro. */
    it('ignora valores de filtro que no existen', async () => {
      component['cambiarTipoActor']('ROOT');
      component['cambiarResultado']('OK');
      await actualizar();

      expect(ids()).toHaveLength(4);
    });

    /**
     * Verifica que limpiar los filtros restablezca la lista completa sin llamar otra vez al
     * servicio.
     */
    it('limpia todos los filtros sin volver a consultar', async () => {
      component['buscar']('nada que coincida');
      component['cambiarResultado']('PENDING');
      await actualizar();
      expect(html.querySelector('.auditoria-vacio')?.textContent).toContain(
        'Puedes ajustar o limpiar los filtros',
      );

      component['limpiarFiltros']();
      await actualizar();

      expect(ids()).toHaveLength(4);
      expect(servicio!.listar).toHaveBeenCalledTimes(1);
    });
  });

  /** Paginación local con 12 eventos generados. */
  describe('paginación', () => {
    beforeEach(async () => {
      const muchos = Array.from({ length: 12 }, (_, i) =>
        evento(String(i + 1), `2026-05-${String(i + 1).padStart(2, '0')}T10:00:00-05:00`),
      );
      servicio!.listar.mockReturnValue(of(muchos));
      await crearComponente();
    });

    /** Verifica que la primera página muestre 10 eventos empezando por el más reciente. */
    it('muestra 10 eventos por página', () => {
      expect(ids()).toHaveLength(10);
      expect(ids()[0]).toBe('12');
    });

    /**
     * Verifica la navegación a la segunda página y que pedir una página inexistente se ajuste a la
     * última.
     */
    it('lleva a la segunda página y no se sale del rango', async () => {
      component['irAPagina'](2);
      await actualizar();
      expect(ids()).toEqual(['2', '1']);

      component['irAPagina'](5);
      expect(component['pagina']()).toBe(2);
    });

    /**
     * Verifica que se acepten 25 filas por página y se ignoren los tamaños que no están entre las
     * opciones.
     */
    it('acepta solo los tamaños de página ofrecidos', async () => {
      component['cambiarTamano']('25');
      await actualizar();
      expect(ids()).toHaveLength(12);

      component['cambiarTamano']('100');
      expect(component['tamanoPagina']()).toBe(25);
    });
  });

  /** Diálogo con la información completa de un evento. */
  describe('detalle', () => {
    beforeEach(async () => {
      await crearComponente();
    });

    /**
     * Verifica que el botón «Ver detalle» abra el diálogo con la acción, la fecha en hora de
     * Colombia y el resultado.
     */
    it('abre el detalle completo de un evento', async () => {
      (html.querySelector('tbody tr button') as HTMLButtonElement).click();
      await actualizar();

      expect(dialogo().open).toBe(true);
      expect(dialogo().textContent).toContain('DELETE /user/eliminar/3');
      expect(dialogo().textContent).toContain('(Colombia)');
      expect(dialogo().textContent).toContain('Fallido');
    });

    /**
     * Verifica el texto alternativo del detalle cuando el evento no tiene información adicional.
     */
    it('indica cuando el evento no trae información adicional', async () => {
      servicio!.listar.mockReturnValue(
        of([evento('200', '2026-05-10T10:00:00Z', laura, { detalle: null })]),
      );
      component['cargarRegistros']();
      await actualizar();

      (html.querySelector('tbody tr button') as HTMLButtonElement).click();
      await actualizar();

      expect(dialogo().textContent).toContain('Este evento no incluye información adicional.');
    });

    /** Verifica que el botón Cerrar cierre el diálogo y limpie la selección. */
    it('cierra el detalle con el botón Cerrar', async () => {
      (html.querySelector('tbody tr button') as HTMLButtonElement).click();
      await actualizar();

      (dialogo().querySelector('header button') as HTMLButtonElement).click();
      await actualizar();

      expect(dialogo().open).toBe(false);
      expect(component['seleccionado']()).toBeNull();
    });

    /**
     * Verifica que al pulsar Escape se cancele el cierre nativo y el componente limpie la
     * selección.
     */
    it('sincroniza la selección al cerrar con Escape', async () => {
      (html.querySelector('tbody tr button') as HTMLButtonElement).click();
      await actualizar();
      const escape = new Event('cancel', { cancelable: true });

      dialogo().dispatchEvent(escape);
      await actualizar();

      expect(escape.defaultPrevented).toBe(true);
      expect(component['seleccionado']()).toBeNull();
    });
  });
});
