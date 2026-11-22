import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule, convertToParamMap } from '@angular/router';
import { Subject, of, throwError } from 'rxjs';
import type { Mock } from 'vitest';
import { AuthService } from '../../services/authservice';
import { RecuperacionService } from '../../services/recuperacionservice';
import { Recuperarcontrasenacomponent } from './recuperarcontrasenacomponent';

/**
 * Pruebas unitarias de {@link Recuperarcontrasenacomponent}.
 *
 * Validan los dos modos de la pantalla: la solicitud del enlace de recuperación y el
 * establecimiento de una nueva contraseña con el token del enlace, incluyendo la limpieza del token
 * en la dirección, las validaciones y los mensajes de error. La ruta activa y los servicios se
 * simulan.
 */
describe('Recuperarcontrasenacomponent', () => {
  let fixture: ComponentFixture<Recuperarcontrasenacomponent>;
  let html: HTMLElement;
  let servicio: { solicitarEnlace: Mock; restablecer: Mock };
  /** En `false`, la prueba simula que no hay un proveedor de RecuperacionService. */
  let servicioDisponible: boolean;
  let auth: { cerrarSesion: Mock };
  let navegar: Mock;

  const titulo = () => html.querySelector('h1')?.textContent?.trim();
  const alerta = () => html.querySelector('[role="alert"]')?.textContent?.trim();
  const botonEnviar = () => html.querySelector('button[type="submit"]') as HTMLButtonElement;

  /** Escribe en un campo del formulario como lo haría el usuario. */
  async function escribir(id: string, valor: string): Promise<void> {
    const campo = html.querySelector(`#${id}`) as HTMLInputElement;
    campo.value = valor;
    campo.dispatchEvent(new Event('input'));
    await fixture.whenStable();
  }

  /** Envía el formulario visible. */
  async function enviarFormulario(): Promise<void> {
    html.querySelector('form')?.dispatchEvent(new Event('submit'));
    await fixture.whenStable();
  }

  /**
   * Configura la ruta con el modo y los parámetros indicados y crea el componente. Con
   * `fallaNavegacion` falla la limpieza del token en la dirección.
   */
  async function abrir(
    modo: 'solicitar' | 'restablecer',
    query: Record<string, string[]> = {},
    { fallaNavegacion = false } = {},
  ): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, RouterModule.forRoot([])],
      declarations: [Recuperarcontrasenacomponent],
      providers: [
        { provide: RecuperacionService, useValue: servicioDisponible ? servicio : null },
        { provide: AuthService, useValue: auth },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { data: { modo }, queryParamMap: convertToParamMap(query) } },
        },
      ],
    }).compileComponents();

    navegar = vi
      .spyOn(TestBed.inject(Router), 'navigate')
      .mockImplementation(() =>
        fallaNavegacion ? Promise.reject(new Error('navegación cancelada')) : Promise.resolve(true),
      ) as Mock;
    fixture = TestBed.createComponent(Recuperarcontrasenacomponent);
    html = fixture.nativeElement;
    await fixture.whenStable();
  }

  beforeEach(() => {
    servicioDisponible = true;
    servicio = {
      solicitarEnlace: vi.fn().mockReturnValue(of(undefined)),
      restablecer: vi.fn().mockReturnValue(of(undefined)),
    };
    auth = { cerrarSesion: vi.fn() };
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  /** Modo `solicitar`: pedir el enlace por correo. */
  describe('solicitar enlace', () => {
    beforeEach(async () => {
      await abrir('solicitar');
    });

    /** Verifica el título y el campo de correo del modo de solicitud. */
    it('pide el correo de la cuenta', () => {
      expect(titulo()).toBe('Recupera tu acceso');
      expect(html.querySelector('#recuperacion-correo')).not.toBeNull();
    });

    /** Verifica que un correo inválido no se envíe y muestre su mensaje. */
    it('valida el correo antes de enviarlo', async () => {
      await escribir('recuperacion-correo', 'ana.correo.com');

      await enviarFormulario();

      expect(servicio.solicitarEnlace).not.toHaveBeenCalled();
      expect(html.querySelector('#recuperacion-correo-error')?.textContent?.trim()).toBe(
        'Ingresa un correo electrónico válido.',
      );
    });

    /** Verifica que el correo se recorte antes de enviarlo y que se muestre la confirmación. */
    it('envía el correo sin espacios y muestra la confirmación', async () => {
      await escribir('recuperacion-correo', '  ana@correo.com ');

      await enviarFormulario();

      expect(servicio.solicitarEnlace).toHaveBeenCalledWith('ana@correo.com');
      expect(html.querySelector('.recuperacion-confirmacion h2')?.textContent).toBe(
        'Revisa tu correo',
      );
    });

    /** Verifica que el botón Volver a solicitar regrese al formulario. */
    it('permite volver a pedir el enlace', async () => {
      await escribir('recuperacion-correo', 'ana@correo.com');
      await enviarFormulario();

      (html.querySelector('.recuperacion-confirmacion + button') as HTMLButtonElement).click();
      await fixture.whenStable();

      expect(html.querySelector('#recuperacion-correo')).not.toBeNull();
    });

    /** Verifica el mensaje de error cuando la solicitud falla. */
    it('muestra un error si la solicitud falla', async () => {
      servicio.solicitarEnlace.mockReturnValue(throwError(() => new Error('500')));
      await escribir('recuperacion-correo', 'ana@correo.com');

      await enviarFormulario();

      expect(alerta()).toBe(
        'No fue posible procesar la solicitud. Inténtalo nuevamente más tarde.',
      );
      expect(html.querySelector('.recuperacion-confirmacion')).toBeNull();
    });

    /** Verifica que no se envíe otra solicitud mientras la primera sigue pendiente. */
    it('evita enviar dos veces mientras espera respuesta', async () => {
      servicio.solicitarEnlace.mockReturnValue(new Subject());
      await escribir('recuperacion-correo', 'ana@correo.com');

      await enviarFormulario();
      await enviarFormulario();

      expect(servicio.solicitarEnlace).toHaveBeenCalledTimes(1);
      expect(botonEnviar().textContent).toContain('Procesando…');
      expect(botonEnviar().disabled).toBe(true);
    });
  });

  /** Modo `restablecer`: crear la contraseña nueva con el token. */
  describe('restablecer contraseña', () => {
    /**
     * Verifica que el token se quite de la URL reemplazando la entrada del historial y que se
     * muestre el formulario.
     */
    it('retira el token de la dirección al abrir el enlace', async () => {
      await abrir('restablecer', { token: ['token-123'] });

      expect(titulo()).toBe('Crea una nueva contraseña');
      expect(navegar).toHaveBeenCalledWith(
        [],
        expect.objectContaining({
          queryParams: { token: null },
          queryParamsHandling: 'merge',
          replaceUrl: true,
        }),
      );
      expect(html.querySelector('#recuperacion-password')).not.toBeNull();
    });

    /**
     * Verifica que un enlace sin token, con un token vacío o con tokens repetidos se considere
     * inválido.
     */
    it.each([
      ['sin token', {}],
      ['con un token vacío', { token: [''] }],
      ['con dos tokens', { token: ['uno', 'dos'] }],
    ])('rechaza un enlace %s', async (_caso, query) => {
      await abrir('restablecer', query);

      expect(alerta()).toContain('El enlace está incompleto o no es válido.');
      expect(html.querySelector('a.rc-principal')?.getAttribute('href')).toBe(
        '/recuperar-contrasena',
      );
    });

    /** Verifica el mensaje cuando falla la navegación que limpia el token de la dirección. */
    it('avisa si no se pudo limpiar la dirección', async () => {
      await abrir('restablecer', { token: ['token-123'] }, { fallaNavegacion: true });
      await fixture.whenStable();

      expect(alerta()).toBe(
        'No fue posible preparar la recuperación. Abre nuevamente el enlace del correo.',
      );
    });

    /** Formulario de nueva contraseña con un token válido. */
    describe('con un enlace válido', () => {
      beforeEach(async () => {
        await abrir('restablecer', { token: ['token-123'] });
      });

      /** Verifica que una confirmación distinta impida el envío y muestre su mensaje. */
      it('exige que la confirmación coincida', async () => {
        await escribir('recuperacion-password', 'NuevaClave2026');
        await escribir('recuperacion-confirmacion', 'NuevaClave2025');

        await enviarFormulario();

        expect(servicio.restablecer).not.toHaveBeenCalled();
        expect(html.querySelector('#recuperacion-confirmacion-error')?.textContent?.trim()).toBe(
          'Completa la confirmación y verifica que las contraseñas coincidan.',
        );
      });

      /** Verifica que una contraseña de menos de 8 caracteres no se envíe. */
      it('exige al menos 8 caracteres', async () => {
        await escribir('recuperacion-password', 'corta');
        await escribir('recuperacion-confirmacion', 'corta');

        await enviarFormulario();

        expect(servicio.restablecer).not.toHaveBeenCalled();
        expect(html.querySelector('#recuperacion-password-error')?.textContent?.trim()).toBe(
          'Revisa los requisitos de la nueva contraseña.',
        );
      });

      /**
       * Verifica que se envíen el token y la contraseña, se cierre la sesión local y se confirme el
       * cambio.
       */
      it('guarda la contraseña, cierra la sesión y confirma el cambio', async () => {
        await escribir('recuperacion-password', 'NuevaClave2026');
        await escribir('recuperacion-confirmacion', 'NuevaClave2026');

        await enviarFormulario();

        expect(servicio.restablecer).toHaveBeenCalledWith('token-123', 'NuevaClave2026');
        expect(auth.cerrarSesion).toHaveBeenCalled();
        expect(html.querySelector('.recuperacion-confirmacion h2')?.textContent).toBe(
          'Contraseña actualizada',
        );
      });

      /** Verifica el mensaje de enlace vencido y que la sesión no se cierre si el cambio falla. */
      it('explica qué hacer si el enlace venció', async () => {
        servicio.restablecer.mockReturnValue(throwError(() => new Error('400')));
        await escribir('recuperacion-password', 'NuevaClave2026');
        await escribir('recuperacion-confirmacion', 'NuevaClave2026');

        await enviarFormulario();

        expect(alerta()).toContain('Si el enlace venció o ya fue utilizado, solicita uno nuevo.');
        expect(auth.cerrarSesion).not.toHaveBeenCalled();
      });

      /** Verifica que el botón de visibilidad cambie el tipo de ambos campos. */
      it('muestra y oculta las contraseñas', async () => {
        const boton = html.querySelector('.rc-visibilidad') as HTMLButtonElement;

        boton.click();
        await fixture.whenStable();

        expect(html.querySelector('#recuperacion-password')?.getAttribute('type')).toBe('text');
        expect(html.querySelector('#recuperacion-confirmacion')?.getAttribute('type')).toBe('text');
        expect(boton.getAttribute('aria-pressed')).toBe('true');
      });
    });
  });

  /**
   * Verifica el aviso y el botón desactivado cuando no hay un proveedor de {@link
   * RecuperacionService}.
   */
  it('avisa cuando la recuperación no está disponible', async () => {
    servicioDisponible = false;

    await abrir('solicitar');

    expect(html.textContent).toContain('La recuperación de acceso aún no está disponible.');
    expect(botonEnviar().disabled).toBe(true);
  });
});
