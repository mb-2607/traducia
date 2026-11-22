import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Subject, of, throwError } from 'rxjs';
import type { Mock } from 'vitest';
import type {
  ResultadoAdministracion,
  UsuarioAdministracion,
} from '../../models/usuarioadminmodel';
import { UsuariosAdminService } from '../../services/usuariosadminservice';
import { Listausuarioscomponent } from './listausuarioscomponent';

/** Crea un usuario activo con plan gratuito; el correo se arma a partir del nombre. */
function usuario(
  id: number,
  username: string,
  cambios: Partial<UsuarioAdministracion> = {},
): UsuarioAdministracion {
  return {
    id,
    username,
    email: `${username}@correo.com`,
    role: 'USER',
    planType: 'FREE',
    enabled: true,
    accountNonLocked: true,
    ...cambios,
  };
}

/** Usuarios de ejemplo con distintos roles, planes y estados de cuenta. */
const USUARIOS = [
  usuario(1, 'laura', { planType: 'ORIA' }),
  usuario(2, 'andres', { role: 'ADMIN', planType: 'INFINITIA' }),
  usuario(3, 'sofia', { enabled: false }),
  usuario(4, 'mateo', { enabled: false, accountNonLocked: false }),
];

/**
 * Pruebas unitarias de {@link Listausuarioscomponent}.
 *
 * Validan el listado de usuarios, las etiquetas de rol, plan y estado, la búsqueda y los filtros,
 * la paginación local, el modal de detalle y edición, y la confirmación de operaciones contra
 * {@link UsuariosAdminService}, que se reemplaza por un doble de prueba.
 */
describe('Listausuarioscomponent', () => {
  let fixture: ComponentFixture<Listausuarioscomponent>;
  let component: Listausuarioscomponent;
  let html: HTMLElement;
  let servicio: { listar: Mock; ejecutar: Mock };
  /** En `false`, la prueba simula que no hay un proveedor de UsuariosAdminService. */
  let servicioDisponible: boolean;

  const filas = () => Array.from(html.querySelectorAll('tbody tr'));
  const nombres = () =>
    Array.from(html.querySelectorAll('.enlace-usuario')).map((b) => b.textContent?.trim());
  const modal = () => html.querySelector('dialog') as HTMLDialogElement;
  const botonModal = (texto: string): HTMLButtonElement => {
    const encontrado = Array.from(modal().querySelectorAll('button')).find(
      (b) => b.textContent?.trim() === texto,
    );
    if (!encontrado) {
      throw new Error(`No hay un botón «${texto}» en pantalla`);
    }
    return encontrado;
  };

  /** Ejecuta la detección de cambios y espera a que termine. */
  async function actualizar(): Promise<void> {
    fixture.detectChanges();
    await fixture.whenStable();
  }

  /** Cambia el valor de un campo de filtro y dispara el evento indicado. */
  async function cambiarCampo(selector: string, valor: string, evento = 'change'): Promise<void> {
    const campo = html.querySelector(selector) as HTMLInputElement | HTMLSelectElement;
    campo.value = valor;
    campo.dispatchEvent(new Event(evento));
    await actualizar();
  }

  /** Configura el módulo de pruebas con el servicio simulado y crea el componente. */
  async function crearComponente(): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, RouterModule.forRoot([])],
      declarations: [Listausuarioscomponent],
      providers: [
        { provide: UsuariosAdminService, useValue: servicioDisponible ? servicio : null },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Listausuarioscomponent);
    component = fixture.componentInstance;
    html = fixture.nativeElement;
    await actualizar();
  }

  beforeEach(() => {
    servicioDisponible = true;
    servicio = { listar: vi.fn().mockReturnValue(of(USUARIOS)), ejecutar: vi.fn() };
  });

  /** Tabla de usuarios, búsqueda y filtros. */
  describe('listado', () => {
    beforeEach(async () => {
      await crearComponente();
    });

    /** Verifica que los usuarios se consulten una vez al iniciar y aparezcan en la tabla. */
    it('carga los usuarios al abrir la página', () => {
      expect(servicio.listar).toHaveBeenCalledTimes(1);
      expect(nombres()).toEqual(['laura', 'andres', 'sofia', 'mateo']);
    });

    /**
     * Verifica que el rol, el plan y el estado se muestren con etiquetas en español y no con los
     * valores del backend.
     */
    it('muestra rol, plan y estado con etiquetas legibles', () => {
      const celdas = (fila: Element) =>
        Array.from(fila.querySelectorAll('td')).map((td) => td.textContent?.trim());

      expect(celdas(filas()[1]).slice(0, 4)).toEqual([
        'andres@correo.com',
        'Administrador',
        'InfinitIA',
        'Activo',
      ]);
      expect(celdas(filas()[2])[3]).toBe('Deshabilitado');
    });

    /** Verifica que una cuenta bloqueada y deshabilitada a la vez se muestre como «Bloqueado». */
    it('da prioridad al bloqueo cuando la cuenta también está deshabilitada', () => {
      expect(filas()[3].querySelector('.etiqueta')?.textContent).toBe('Bloqueado');
    });

    /** Verifica que la búsqueda revise el correo sin distinguir mayúsculas. */
    it('busca por usuario o correo sin importar mayúsculas', async () => {
      await cambiarCampo('#usuarios-busqueda', 'SOFIA@', 'input');

      expect(nombres()).toEqual(['sofia']);
    });

    /** Verifica el filtro por plan. */
    it('filtra por plan', async () => {
      component['cambiarPlan']('ORIA');
      await actualizar();

      expect(nombres()).toEqual(['laura']);
    });

    /** Verifica el filtro por estado de la cuenta. */
    it('filtra por estado de la cuenta', async () => {
      component['cambiarEstado']('bloqueado');
      await actualizar();

      expect(nombres()).toEqual(['mateo']);
    });

    /** Verifica que los valores de plan o estado desconocidos no se apliquen. */
    it('ignora valores de filtro desconocidos', async () => {
      component['cambiarPlan']('PREMIUM');
      component['cambiarEstado']('eliminado');
      await actualizar();

      expect(nombres()).toHaveLength(4);
    });

    /** Verifica el mensaje de tabla vacía y la sugerencia de cambiar la búsqueda. */
    it('sugiere cambiar los filtros cuando no hay resultados', async () => {
      await cambiarCampo('#usuarios-busqueda', 'nadie', 'input');

      expect(html.querySelector('.vacio')?.textContent).toContain('No hay usuarios para mostrar.');
      expect(html.querySelector('.vacio')?.textContent).toContain(
        'Prueba con otra búsqueda o cambia los filtros.',
      );
    });
  });

  /** Paginación local con 23 usuarios generados. */
  describe('paginación', () => {
    beforeEach(async () => {
      const muchos = Array.from({ length: 23 }, (_, i) => usuario(i + 1, `usuario${i + 1}`));
      servicio.listar.mockReturnValue(of(muchos));
      await crearComponente();
    });

    /** Verifica que la primera página muestre 10 usuarios. */
    it('muestra 10 usuarios por página', () => {
      expect(filas()).toHaveLength(10);
      expect(nombres()[0]).toBe('usuario1');
    });

    /**
     * Verifica la navegación a la última página y que los números fuera de rango se ajusten a la
     * primera o a la última.
     */
    it('se mueve entre páginas sin salirse del rango', async () => {
      component['irAPagina'](3);
      await actualizar();
      expect(nombres()).toEqual(['usuario21', 'usuario22', 'usuario23']);

      component['irAPagina'](10);
      await actualizar();
      expect(component['pagina']()).toBe(3);

      component['irAPagina'](-1);
      await actualizar();
      expect(component['pagina']()).toBe(1);
    });

    /** Verifica que con 25 filas por página se vean los 23 usuarios. */
    it('permite elegir 25 usuarios por página', async () => {
      component['cambiarTamano']('25');
      await actualizar();

      expect(filas()).toHaveLength(23);
    });

    /** Verifica que se ignore un tamaño de página que no está entre las opciones. */
    it('no acepta tamaños de página arbitrarios', () => {
      component['cambiarTamano']('7');

      expect(component['tamanoPagina']()).toBe(10);
    });

    /** Verifica que una nueva búsqueda regrese a la primera página. */
    it('vuelve a la primera página al buscar', async () => {
      component['irAPagina'](2);
      component['buscar']('usuario');
      await actualizar();

      expect(component['pagina']()).toBe(1);
    });
  });

  /** Errores de carga y ausencia del servicio. */
  describe('carga fallida o no disponible', () => {
    /** Verifica el mensaje de error cuando falla la consulta. */
    it('informa si no se pudieron cargar los usuarios', async () => {
      servicio.listar.mockReturnValue(throwError(() => new Error('500')));

      await crearComponente();

      expect(html.querySelector('[role="alert"]')?.textContent).toBe(
        'No fue posible cargar los usuarios. Inténtalo nuevamente.',
      );
    });

    /**
     * Verifica el aviso y el botón de actualizar desactivado cuando no hay un proveedor de {@link
     * UsuariosAdminService}.
     */
    it('desactiva la gestión cuando no hay servicio', async () => {
      servicioDisponible = false;

      await crearComponente();

      expect(html.textContent).toContain(
        'La consulta y la gestión de usuarios aún no están disponibles.',
      );
      expect((html.querySelector('.cabecera .boton') as HTMLButtonElement).disabled).toBe(true);
    });
  });

  /** Modal de detalle y formulario de edición. */
  describe('detalle y edición', () => {
    beforeEach(async () => {
      await crearComponente();
    });

    /** Verifica que pulsar el nombre abra el modal con el detalle de la cuenta. */
    it('abre el detalle al pulsar el nombre de un usuario', async () => {
      (html.querySelector('.enlace-usuario') as HTMLButtonElement).click();
      await actualizar();

      expect(modal().open).toBe(true);
      expect(component['tituloModal']()).toBe('Detalle del usuario');
      expect(modal().textContent).toContain('laura@correo.com');
    });

    /** Verifica que el formulario de edición se llene con el usuario y el correo actuales. */
    it('carga los datos actuales en el formulario de edición', async () => {
      component['editar'](USUARIOS[0]);
      await actualizar();

      expect(component['tituloModal']()).toBe('Editar usuario');
      expect(component['formulario'].getRawValue()).toEqual({
        username: 'laura',
        email: 'laura@correo.com',
      });
    });

    /** Verifica que los cambios se recorten y pasen a la confirmación sin enviarse todavía. */
    it('pide confirmación antes de guardar los cambios', async () => {
      component['editar'](USUARIOS[0]);
      component['formulario'].setValue({ username: ' laura.m ', email: 'lauram@correo.com ' });

      component['prepararEdicion']();
      await actualizar();

      expect(component['tituloModal']()).toBe('Guardar cambios');
      expect(component['solicitud']()).toEqual({
        accion: 'actualizar',
        usuarioId: 1,
        cambios: { username: 'laura.m', email: 'lauram@correo.com' },
      });
      expect(modal().querySelector('.detalle')?.textContent).toContain('lauram@correo.com');
    });

    /** Verifica que un correo inválido mantenga el modal en modo de edición. */
    it('no avanza a la confirmación con un correo inválido', () => {
      component['editar'](USUARIOS[0]);
      component['formulario'].setValue({ username: 'laura', email: 'laura@' });

      component['prepararEdicion']();

      expect(component['modo']()).toBe('editar');
      expect(component['solicitud']()).toBeNull();
    });

    /** Verifica que cerrar el modal limpie el usuario seleccionado. */
    it('cierra el modal y descarta la selección', async () => {
      component['verDetalle'](USUARIOS[0]);
      await actualizar();

      botonModal('Cerrar').click();
      await actualizar();

      expect(modal().open).toBe(false);
      expect(component['seleccionado']()).toBeNull();
    });
  });

  /** Envío de las operaciones confirmadas y aplicación de la respuesta. */
  describe('confirmación de operaciones', () => {
    beforeEach(async () => {
      await crearComponente();
    });

    /**
     * Verifica que al confirmar se envíe la solicitud y la fila se reemplace con el usuario
     * devuelto por el servidor.
     */
    it('actualiza la fila con la respuesta del servidor', async () => {
      const actualizado = usuario(1, 'laura.m', { planType: 'ORIA' });
      servicio.ejecutar.mockReturnValue(
        of<ResultadoAdministracion>({ tipo: 'actualizado', usuario: actualizado }),
      );
      component['editar'](USUARIOS[0]);
      component['formulario'].setValue({ username: 'laura.m', email: 'laura.m@correo.com' });
      component['prepararEdicion']();
      await actualizar();

      botonModal('Confirmar').click();
      await actualizar();

      expect(servicio.ejecutar).toHaveBeenCalledWith({
        accion: 'actualizar',
        usuarioId: 1,
        cambios: { username: 'laura.m', email: 'laura.m@correo.com' },
      });
      expect(nombres()[0]).toBe('laura.m');
      expect(html.querySelector('.mensaje')?.textContent).toBe(
        'Los datos del usuario se actualizaron correctamente.',
      );
      expect(modal().open).toBe(false);
    });

    /**
     * Verifica que la eliminación pida confirmación con un botón de peligro y quite la fila al
     * confirmarse.
     */
    it('quita al usuario eliminado de la tabla', async () => {
      servicio.ejecutar.mockReturnValue(
        of<ResultadoAdministracion>({ tipo: 'eliminado', usuarioId: 3 }),
      );
      component['prepararAccion'](USUARIOS[2], 'eliminar');
      await actualizar();

      expect(component['tituloModal']()).toBe('Eliminar usuario');
      expect(botonModal('Confirmar').classList).toContain('peligro');

      botonModal('Confirmar').click();
      await actualizar();

      expect(nombres()).toEqual(['laura', 'andres', 'mateo']);
      expect(component['exito']()).toBe('El usuario se eliminó correctamente.');
    });

    /** Verifica que un error del servidor se muestre dentro del modal sin modificar la tabla. */
    it('mantiene el modal abierto y la tabla intacta si la operación falla', async () => {
      servicio.ejecutar.mockReturnValue(throwError(() => new Error('403')));
      component['prepararAccion'](USUARIOS[1], 'eliminar');
      await actualizar();

      botonModal('Confirmar').click();
      await actualizar();

      expect(modal().open).toBe(true);
      expect(modal().querySelector('[role="alert"]')?.textContent).toContain(
        'No fue posible completar la operación.',
      );
      expect(nombres()).toHaveLength(4);
    });

    /** Verifica que el modal no se pueda cerrar mientras la operación sigue pendiente. */
    it('no deja cerrar el modal mientras la operación está en curso', async () => {
      servicio.ejecutar.mockReturnValue(new Subject());
      component['prepararAccion'](USUARIOS[1], 'bloquear');
      await actualizar();

      botonModal('Confirmar').click();
      await actualizar();
      component['cerrarModal']();

      expect(modal().open).toBe(true);
      expect(modal().textContent).toContain('Procesando la solicitud…');
    });

    /**
     * Verifica que Escape cancele el cierre nativo y que el componente cierre el modal por su
     * cuenta.
     */
    it('evita que Escape cierre el modal de forma nativa', async () => {
      component['verDetalle'](USUARIOS[0]);
      await actualizar();
      const escape = new Event('cancel', { cancelable: true });

      modal().dispatchEvent(escape);

      expect(escape.defaultPrevented).toBe(true);
      expect(modal().open).toBe(false);
    });
  });
});
