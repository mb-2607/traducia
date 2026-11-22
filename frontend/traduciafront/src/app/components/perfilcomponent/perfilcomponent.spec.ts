import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { BehaviorSubject, Subject, of, throwError } from 'rxjs';
import type { Mock } from 'vitest';
import type { PerfilUsuario } from '../../models/perfilmodel';
import type { User } from '../../models/usermodel';
import { AuthService } from '../../services/authservice';
import { PerfilService } from '../../services/perfilservice';
import { Perfilcomponent } from './perfilcomponent';

/** Usuario guardado en la sesión. */
const sesionLaura: User = {
  nombre: 'Laura Martínez',
  correo: 'laura@correo.com',
  rol: 'USER',
  plan: 'oria',
};

/** Perfil completo que devuelve el servidor. */
const perfilLaura: PerfilUsuario = {
  nombre: 'Laura Martínez',
  correo: 'laura@correo.com',
  rol: 'USER',
  plan: 'oria',
  ciudad: 'Bogotá',
  edad: 28,
  fotoUrl: null,
};

/**
 * Pruebas unitarias de {@link Perfilcomponent}.
 *
 * Validan la carga del perfil a partir de la sesión y del servicio, la edición y el guardado de
 * datos, la vista previa y la retirada de la foto, la protección frente a direcciones de imagen
 * inseguras y el cierre de sesión. {@link AuthService} y {@link PerfilService} se simulan.
 */
describe('Perfilcomponent', () => {
  let fixture: ComponentFixture<Perfilcomponent>;
  let component: Perfilcomponent;
  let html: HTMLElement;
  let sesion$: BehaviorSubject<User | null>;
  let auth: { getUserAsObservable: () => unknown; cerrarSesion: Mock };
  let servicio: { obtener: Mock; guardar: Mock };
  /** En `false`, la prueba simula que no hay un proveedor de PerfilService. */
  let servicioDisponible: boolean;
  let navegar: Mock;

  const campo = (id: string) => html.querySelector(`#${id}`) as HTMLInputElement;
  const boton = (texto: string): HTMLButtonElement => {
    const encontrado = Array.from(html.querySelectorAll('button')).find(
      (b) => b.textContent?.trim() === texto,
    );
    if (!encontrado) {
      throw new Error(`No hay un botón «${texto}» en pantalla`);
    }
    return encontrado;
  };

  /** Espera a que Angular termine de aplicar los cambios pendientes. */
  async function actualizar(): Promise<void> {
    await fixture.whenStable();
  }

  /** Escribe en un campo del formulario como lo haría el usuario. */
  async function escribir(id: string, valor: string): Promise<void> {
    campo(id).value = valor;
    campo(id).dispatchEvent(new Event('input'));
    await actualizar();
  }

  /** Simula la elección de una foto en el input de tipo file. */
  async function elegirFoto(archivo: File): Promise<void> {
    const input = html.querySelector('input[type="file"]') as HTMLInputElement;
    Object.defineProperty(input, 'files', { configurable: true, value: { item: () => archivo } });
    input.dispatchEvent(new Event('change'));
    // seleccionarFoto es asíncrono: espera a que la imagen "se decodifique".
    await new Promise((resolver) => setTimeout(resolver));
    await actualizar();
  }

  /** Configura el módulo de pruebas con los servicios simulados y crea el componente. */
  async function crearComponente(): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, RouterModule.forRoot([])],
      declarations: [Perfilcomponent],
      providers: [
        { provide: AuthService, useValue: auth },
        { provide: PerfilService, useValue: servicioDisponible ? servicio : null },
      ],
    }).compileComponents();

    navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true) as Mock;
    fixture = TestBed.createComponent(Perfilcomponent);
    component = fixture.componentInstance;
    html = fixture.nativeElement;
    await actualizar();
  }

  beforeEach(() => {
    sesion$ = new BehaviorSubject<User | null>(sesionLaura);
    auth = { getUserAsObservable: () => sesion$.asObservable(), cerrarSesion: vi.fn() };
    servicioDisponible = true;
    servicio = {
      obtener: vi.fn().mockReturnValue(of(perfilLaura)),
      guardar: vi.fn((solicitud) =>
        of({ ...perfilLaura, nombre: solicitud.datos.nombre, ciudad: solicitud.datos.ciudad }),
      ),
    };
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  /** Datos iniciales y consulta del perfil. */
  describe('carga del perfil', () => {
    /** Verifica que el formulario muestre los datos que devuelve el servicio. */
    it('muestra los datos completos del perfil', async () => {
      await crearComponente();

      expect(servicio.obtener).toHaveBeenCalledTimes(1);
      expect(campo('perfil-nombre').value).toBe('Laura Martínez');
      expect(campo('perfil-ciudad').value).toBe('Bogotá');
      expect(campo('perfil-edad').value).toBe('28');
      expect(campo('perfil-correo').value).toBe('laura@correo.com');
    });

    /**
     * Verifica que mientras carga el perfil se muestren los datos de la sesión y el mensaje de
     * carga.
     */
    it('usa los datos de la sesión mientras llega la respuesta', async () => {
      servicio.obtener.mockReturnValue(new Subject());

      await crearComponente();

      expect(campo('perfil-nombre').value).toBe('Laura Martínez');
      expect(campo('perfil-ciudad').value).toBe('');
      expect(html.textContent).toContain('Cargando los datos de tu perfil…');
    });

    /** Verifica que sin foto se muestren las iniciales del nombre. */
    it('muestra las iniciales del nombre cuando no hay foto', async () => {
      await crearComponente();

      expect(html.querySelector('.perfil-avatar span')?.textContent).toBe('LM');
    });

    /**
     * Verifica que el enlace Volver lleve al dashboard o al panel de administración según el rol.
     */
    it('vuelve al dashboard o al panel según el rol', async () => {
      await crearComponente();
      expect(html.querySelector('.perfil-cabecera a')?.getAttribute('href')).toBe('/dashboard');

      servicio.obtener.mockReturnValue(of({ ...perfilLaura, rol: 'ADMIN' }));
      component['reintentarCarga']();
      await actualizar();

      expect(html.querySelector('.perfil-cabecera a')?.getAttribute('href')).toBe('/admin');
    });

    /**
     * Verifica que un error de carga conserve los datos de la sesión y que Reintentar cargue el
     * perfil completo.
     */
    it('si falla la carga conserva los datos de la sesión y permite reintentar', async () => {
      servicio.obtener.mockReturnValue(throwError(() => new Error('500')));
      await crearComponente();

      expect(html.querySelector('[role="alert"]')?.textContent).toContain(
        'No fue posible cargar el perfil completo. Se muestran los datos de tu sesión.',
      );
      expect(campo('perfil-nombre').value).toBe('Laura Martínez');

      servicio.obtener.mockReturnValue(of(perfilLaura));
      boton('Reintentar').click();
      await actualizar();

      expect(servicio.obtener).toHaveBeenCalledTimes(2);
      expect(campo('perfil-ciudad').value).toBe('Bogotá');
      expect(html.querySelector('[role="alert"]')).toBeNull();
    });

    /** Verifica que sin sesión no se consulte el perfil y se invite a iniciar sesión. */
    it('pide iniciar sesión cuando no hay usuario', async () => {
      sesion$.next(null);

      await crearComponente();

      expect(servicio.obtener).not.toHaveBeenCalled();
      expect(html.textContent).toContain('Inicia sesión para consultar tu perfil.');
    });

    /**
     * Verifica que al cambiar de cuenta se descarten los datos anteriores y se consulte el nuevo
     * perfil.
     */
    it('recarga el perfil si cambia la cuenta de la sesión', async () => {
      await crearComponente();
      servicio.obtener.mockReturnValue(
        of({ ...perfilLaura, nombre: 'Andrés Gómez', correo: 'andres@correo.com', ciudad: null }),
      );

      sesion$.next({ ...sesionLaura, nombre: 'Andrés Gómez', correo: 'andres@correo.com' });
      await actualizar();

      expect(servicio.obtener).toHaveBeenCalledTimes(2);
      expect(campo('perfil-nombre').value).toBe('Andrés Gómez');
      expect(campo('perfil-ciudad').value).toBe('');
    });

    /**
     * Verifica el aviso y los datos de sesión cuando no hay un proveedor de {@link PerfilService}.
     */
    it('muestra solo los datos de la sesión si no hay servicio de perfil', async () => {
      servicioDisponible = false;

      await crearComponente();

      expect(html.textContent).toContain('La actualización del perfil aún no está disponible.');
      expect(campo('perfil-nombre').value).toBe('Laura Martínez');
    });
  });

  /** Edición y guardado de los datos personales. */
  describe('guardar cambios', () => {
    beforeEach(async () => {
      await crearComponente();
    });

    /** Verifica que enviar el formulario sin cambios no llame al servidor. */
    it('no llama al servidor si no hay cambios', async () => {
      html.querySelector('form')?.dispatchEvent(new Event('submit'));
      await actualizar();

      expect(servicio.guardar).not.toHaveBeenCalled();
    });

    /** Verifica que los datos se recorten y que una edad vacía se envíe como `null`. */
    it('envía los datos sin espacios sobrantes', async () => {
      await escribir('perfil-nombre', '  Laura M.  ');
      await escribir('perfil-ciudad', ' Medellín ');
      await escribir('perfil-edad', '');

      html.querySelector('form')?.dispatchEvent(new Event('submit'));
      await actualizar();

      expect(servicio.guardar).toHaveBeenCalledWith({
        datos: { nombre: 'Laura M.', ciudad: 'Medellín', edad: null },
        foto: null,
        quitarFoto: false,
      });
    });

    /**
     * Verifica el mensaje de éxito y que la respuesta se convierta en la nueva base del formulario.
     */
    it('confirma el guardado y toma la respuesta como nueva base', async () => {
      await escribir('perfil-nombre', 'Laura M.');

      component['guardar']();
      await actualizar();

      expect(html.textContent).toContain('Tu perfil se actualizó correctamente.');
      expect(campo('perfil-nombre').value).toBe('Laura M.');
      expect(component['hayCambios']()).toBe(false);
    });

    /**
     * Verifica que un nombre demasiado corto o una edad fuera del rango de 1 a 120 impidan guardar.
     */
    it.each([
      ['perfil-nombre', 'L', 'Escribe un nombre de entre 2 y 80 caracteres.'],
      ['perfil-edad', '130', null],
      ['perfil-edad', '0', null],
    ])('no guarda si %s = "%s" no es válido', async (id, valor, mensaje) => {
      await escribir(id, valor);

      component['guardar']();
      await actualizar();

      expect(servicio.guardar).not.toHaveBeenCalled();
      if (mensaje) {
        expect(html.querySelector('#perfil-nombre-error')?.textContent?.trim()).toBe(mensaje);
      }
    });

    /** Verifica que un error al guardar conserve lo que escribió el usuario. */
    it('conserva lo escrito si el servidor rechaza los cambios', async () => {
      servicio.guardar.mockReturnValue(throwError(() => new Error('400')));
      await escribir('perfil-nombre', 'Laura M.');

      component['guardar']();
      await actualizar();

      expect(html.querySelector('[role="alert"]')?.textContent).toContain(
        'No fue posible guardar los cambios.',
      );
      expect(campo('perfil-nombre').value).toBe('Laura M.');
    });

    /** Verifica que Descartar restaure los valores del último perfil confirmado. */
    it('descarta los cambios no guardados', async () => {
      await escribir('perfil-nombre', 'Otro nombre');

      boton('Descartar').click();
      await actualizar();

      expect(campo('perfil-nombre').value).toBe('Laura Martínez');
      expect(servicio.guardar).not.toHaveBeenCalled();
    });
  });

  /** Verifica que no se pueda guardar mientras el perfil completo sigue cargando. */
  it('no permite guardar antes de que termine la carga del perfil', async () => {
    servicio.obtener.mockReturnValue(new Subject());
    await crearComponente();
    component['formulario'].controls.nombre.setValue('Laura M.');

    component['guardar']();

    expect(servicio.guardar).not.toHaveBeenCalled();
  });

  /** Selección y vista previa de una foto nueva. */
  describe('foto de perfil', () => {
    beforeEach(async () => {
      // jsdom no crea URLs de objetos ni decodifica imágenes.
      URL.createObjectURL = vi.fn(() => 'blob:vista-previa');
      URL.revokeObjectURL = vi.fn();
      HTMLImageElement.prototype.decode = vi.fn(() => Promise.resolve());
      await crearComponente();
    });

    afterEach(() => {
      Reflect.deleteProperty(URL, 'createObjectURL');
      Reflect.deleteProperty(URL, 'revokeObjectURL');
      Reflect.deleteProperty(HTMLImageElement.prototype, 'decode');
    });

    /** Verifica que un archivo que no es imagen se rechace sin crear una vista previa. */
    it('rechaza archivos que no son imágenes', async () => {
      await elegirFoto(new File(['%PDF'], 'hoja-de-vida.pdf', { type: 'application/pdf' }));

      expect(html.querySelector('.pf-error')?.textContent).toBe(
        'Selecciona una imagen PNG, JPEG o WebP de hasta 10 MB que no esté vacía.',
      );
      expect(URL.createObjectURL).not.toHaveBeenCalled();
    });

    /** Verifica que una imagen de 0 bytes se rechace. */
    it('rechaza imágenes vacías', async () => {
      await elegirFoto(new File([], 'vacia.png', { type: 'image/png' }));

      expect(component['errorFoto']()).not.toBe('');
    });

    /**
     * Verifica que una imagen válida muestre la vista previa, avise del cambio pendiente y se envíe
     * al guardar.
     */
    it('muestra la vista previa y la envía al guardar', async () => {
      const foto = new File(['png'], 'yo.png', { type: 'image/png' });

      await elegirFoto(foto);

      expect(html.querySelector('.perfil-avatar img')?.getAttribute('src')).toBe(
        'blob:vista-previa',
      );
      expect(html.textContent).toContain('El cambio de foto se aplicará al guardar.');

      component['guardar']();

      expect(servicio.guardar).toHaveBeenCalledWith(
        expect.objectContaining({ foto, quitarFoto: false }),
      );
    });

    /**
     * Verifica que una imagen que no se puede decodificar muestre un error y libere la URL
     * temporal.
     */
    it('avisa si la imagen no se puede abrir y libera la URL temporal', async () => {
      vi.mocked(HTMLImageElement.prototype.decode).mockRejectedValue(new Error('imagen dañada'));

      await elegirFoto(new File(['xx'], 'rota.jpg', { type: 'image/jpeg' }));

      expect(component['errorFoto']()).toBe(
        'No fue posible abrir la imagen. Selecciona otro archivo.',
      );
      expect(URL.revokeObjectURL).toHaveBeenCalledWith('blob:vista-previa');
      expect(html.querySelector('.perfil-avatar img')).toBeNull();
    });

    /**
     * Verifica que descartar libere la URL de la vista previa y vuelvan a mostrarse las iniciales.
     */
    it('libera la vista previa al descartar los cambios', async () => {
      await elegirFoto(new File(['png'], 'yo.png', { type: 'image/png' }));

      component['descartar']();
      await actualizar();

      expect(URL.revokeObjectURL).toHaveBeenCalledWith('blob:vista-previa');
      expect(html.querySelector('.perfil-avatar span')?.textContent).toBe('LM');
    });
  });

  /** Foto que el perfil ya tiene en el servidor. */
  describe('foto guardada', () => {
    /**
     * Verifica que se muestre la foto guardada y que, al quitarla, se envíe `quitarFoto` al
     * guardar.
     */
    it('muestra la foto del servidor y permite quitarla', async () => {
      servicio.obtener.mockReturnValue(
        of({ ...perfilLaura, fotoUrl: 'https://cdn.traducia.com/laura.png' }),
      );
      await crearComponente();

      expect(html.querySelector('.perfil-avatar img')?.getAttribute('src')).toBe(
        'https://cdn.traducia.com/laura.png',
      );

      boton('Quitar foto').click();
      await actualizar();
      component['guardar']();

      expect(servicio.guardar).toHaveBeenCalledWith(
        expect.objectContaining({ foto: null, quitarFoto: true }),
      );
    });

    /** Verifica que una dirección `javascript:` no se use como imagen. */
    it('no usa direcciones de imagen inseguras', async () => {
      servicio.obtener.mockReturnValue(of({ ...perfilLaura, fotoUrl: 'javascript:alert(1)' }));

      await crearComponente();

      expect(html.querySelector('.perfil-avatar img')).toBeNull();
      expect(html.querySelector('.perfil-avatar span')?.textContent).toBe('LM');
    });

    /** Verifica que, si la imagen falla al cargar, se vuelvan a mostrar las iniciales. */
    it('vuelve a las iniciales si la imagen no carga', async () => {
      servicio.obtener.mockReturnValue(
        of({ ...perfilLaura, fotoUrl: 'https://cdn.traducia.com/x.png' }),
      );
      await crearComponente();

      const imagen = html.querySelector('.perfil-avatar img');
      expect(imagen).not.toBeNull();

      imagen?.dispatchEvent(new Event('error'));
      await actualizar();

      expect(html.querySelector('.perfil-avatar img')).toBeNull();
    });
  });

  /** Cierre de la sesión desde el perfil. */
  describe('cerrar sesión', () => {
    beforeEach(async () => {
      await crearComponente();
    });

    /** Verifica que cerrar sesión limpie la sesión y navegue al login. */
    it('cierra la sesión y lleva al login', async () => {
      boton('Cerrar sesión').click();

      expect(auth.cerrarSesion).toHaveBeenCalled();
      expect(navegar).toHaveBeenCalledWith(['/login']);
    });

    /** Verifica el mensaje cuando la navegación al login falla después de cerrar la sesión. */
    it('avisa si no pudo abrir el login', async () => {
      navegar.mockRejectedValue(new Error('navegación cancelada'));

      component['cerrarSesion']();
      await actualizar();

      expect(component['error']()).toBe(
        'La sesión se cerró, pero no fue posible abrir el inicio de sesión.',
      );
    });
  });
});
