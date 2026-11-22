import { Location } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule, convertToParamMap } from '@angular/router';
import type { ParamMap } from '@angular/router';
import { BehaviorSubject, Subject, of, throwError } from 'rxjs';
import type { Mock } from 'vitest';
import { VerificacionCorreoService } from '../../services/verificacioncorreoservice';
import { Verificarcorreocomponent } from './verificarcorreocomponent';

/**
 * Pruebas unitarias de {@link Verificarcorreocomponent}.
 *
 * Validan la verificación del correo a partir del token del enlace, los enlaces inválidos, los
 * reintentos, la cancelación al cambiar de enlace y el reenvío de un nuevo enlace. Los parámetros
 * de la ruta se simulan con un `BehaviorSubject` para poder cambiarlos durante la prueba.
 */
describe('Verificarcorreocomponent', () => {
  let fixture: ComponentFixture<Verificarcorreocomponent>;
  let component: Verificarcorreocomponent;
  let html: HTMLElement;
  let servicio: { verificar: Mock; reenviar: Mock } | null;
  let parametros$: BehaviorSubject<ParamMap>;
  let reemplazarUrl: Mock;

  const titulo = () => html.querySelector('#vc-titulo')?.textContent?.trim();
  const botonReintentar = () =>
    Array.from(html.querySelectorAll('button')).find((b) =>
      b.textContent?.includes('Reintentar verificación'),
    );

  /** Construye los parámetros de consulta con los tokens indicados. */
  const conToken = (...tokens: string[]) =>
    convertToParamMap(tokens.length ? { token: tokens } : {});

  /** Escribe en el campo de correo del formulario de reenvío. */
  async function escribirCorreo(valor: string): Promise<void> {
    const campo = html.querySelector('#vc-correo') as HTMLInputElement;
    campo.value = valor;
    campo.dispatchEvent(new Event('input'));
    await fixture.whenStable();
  }

  /** Configura la ruta con los parámetros indicados y crea el componente. */
  async function abrir(parametros: ParamMap = conToken()): Promise<void> {
    parametros$ = new BehaviorSubject(parametros);

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, RouterModule.forRoot([])],
      declarations: [Verificarcorreocomponent],
      providers: [
        { provide: VerificacionCorreoService, useValue: servicio },
        { provide: ActivatedRoute, useValue: { queryParamMap: parametros$, snapshot: {} } },
      ],
    }).compileComponents();

    reemplazarUrl = vi.spyOn(TestBed.inject(Location), 'replaceState') as Mock;
    fixture = TestBed.createComponent(Verificarcorreocomponent);
    component = fixture.componentInstance;
    html = fixture.nativeElement;
    await fixture.whenStable();
  }

  beforeEach(() => {
    servicio = {
      verificar: vi.fn().mockReturnValue(of(undefined)),
      reenviar: vi.fn().mockReturnValue(of(undefined)),
    };
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  /** Verifica que sin token se invite a revisar el correo y no se llame al servicio. */
  it('sin enlace invita a revisar el correo', async () => {
    await abrir();

    expect(titulo()).toBe('Verifica tu correo');
    expect(servicio!.verificar).not.toHaveBeenCalled();
    expect(reemplazarUrl).not.toHaveBeenCalled();
  });

  /**
   * Verifica que el token se envíe al servicio, se retire de la dirección y se muestre el éxito.
   */
  it('verifica el token del enlace y lo quita de la barra de direcciones', async () => {
    await abrir(conToken('token-123'));

    expect(servicio!.verificar).toHaveBeenCalledWith('token-123');
    expect(reemplazarUrl).toHaveBeenCalledTimes(1);
    expect(reemplazarUrl.mock.calls[0][0]).not.toContain('token');
    expect(titulo()).toBe('Correo verificado');
  });

  /** Verifica que tras verificar se ofrezca iniciar sesión y se oculte el formulario de reenvío. */
  it('después de verificar invita a iniciar sesión y oculta el reenvío', async () => {
    await abrir(conToken('token-123'));

    expect(html.querySelector('.vc-pie a')?.textContent?.trim()).toBe('Iniciar sesión');
    expect(html.querySelector('.vc-reenvio')).toBeNull();
  });

  /**
   * Verifica que los tokens con espacios, repetidos, vacíos o demasiado largos se traten como un
   * enlace incompleto.
   */
  it.each([
    ['con espacios', conToken('token con espacios')],
    ['repetido', conToken('uno', 'dos')],
    ['vacío', conToken('')],
    ['demasiado largo', conToken('a'.repeat(4097))],
  ])('no intenta verificar un token %s', async (_caso, parametros) => {
    await abrir(parametros);

    expect(titulo()).toBe('Enlace incompleto');
    expect(servicio!.verificar).not.toHaveBeenCalled();
  });

  /** Verifica el estado «verificando» mientras el servicio no responde. */
  it('muestra el estado de carga mientras el backend responde', async () => {
    servicio!.verificar.mockReturnValue(new Subject());

    await abrir(conToken('token-123'));

    expect(component.estado()).toBe('verificando');
    expect(titulo()).toBe('Verificando tu correo');
    expect(html.querySelector('[role="status"]')?.getAttribute('aria-busy')).toBe('true');
  });

  /** Errores del backend al verificar. */
  describe('cuando la verificación falla', () => {
    beforeEach(async () => {
      servicio!.verificar.mockReturnValue(throwError(() => new Error('410')));
      await abrir(conToken('token-123'));
    });

    /** Verifica el mensaje de error y el botón para reintentar. */
    it('informa el error y ofrece reintentar', () => {
      expect(titulo()).toBe('No pudimos verificar tu correo');
      expect(botonReintentar()).toBeDefined();
    });

    /** Verifica que reintentar use el mismo token y pueda terminar en éxito. */
    it('vuelve a intentar con el mismo token', async () => {
      servicio!.verificar.mockReturnValue(of(undefined));

      botonReintentar()!.click();
      await fixture.whenStable();

      expect(servicio!.verificar).toHaveBeenCalledTimes(2);
      expect(servicio!.verificar).toHaveBeenLastCalledWith('token-123');
      expect(titulo()).toBe('Correo verificado');
    });
  });

  /** Verifica que no se lance otra verificación mientras la primera sigue pendiente. */
  it('no permite reintentar mientras una verificación sigue en curso', async () => {
    servicio!.verificar.mockReturnValue(new Subject());
    await abrir(conToken('token-123'));

    component.reintentarVerificacion();

    expect(servicio!.verificar).toHaveBeenCalledTimes(1);
  });

  /**
   * Verifica que un enlace nuevo cancele la verificación anterior y que su respuesta tardía no
   * cambie el resultado.
   */
  it('cancela la verificación anterior si llega un enlace nuevo', async () => {
    const primera = new Subject<void>();
    servicio!.verificar.mockReturnValueOnce(primera).mockReturnValueOnce(of(undefined));
    await abrir(conToken('token-viejo'));

    parametros$.next(conToken('token-nuevo'));
    await fixture.whenStable();
    primera.error(new Error('respuesta tardía'));
    await fixture.whenStable();

    expect(servicio!.verificar).toHaveBeenLastCalledWith('token-nuevo');
    expect(component.estado()).toBe('verificado');
  });

  /** Formulario para pedir un nuevo enlace. */
  describe('reenvío del enlace', () => {
    beforeEach(async () => {
      await abrir();
    });

    /** Verifica que sin correo no se reenvíe y se muestre el mensaje de campo obligatorio. */
    it('pide el correo antes de reenviar', async () => {
      component.reenviarEnlace();
      await fixture.whenStable();

      expect(servicio!.reenviar).not.toHaveBeenCalled();
      expect(html.querySelector('#vc-correo-error')?.textContent?.trim()).toBe(
        'Ingresa el correo de tu cuenta.',
      );
    });

    /** Verifica que un correo inválido no se envíe y muestre su mensaje. */
    it('rechaza un correo mal escrito', async () => {
      await escribirCorreo('ana@');
      component.reenviarEnlace();
      await fixture.whenStable();

      expect(servicio!.reenviar).not.toHaveBeenCalled();
      expect(html.querySelector('#vc-correo-error')?.textContent?.trim()).toBe(
        'Ingresa un correo válido de hasta 254 caracteres.',
      );
    });

    /** Verifica que el correo se recorte, se confirme el reenvío y se limpie el formulario. */
    it('solicita el enlace y limpia el formulario', async () => {
      await escribirCorreo('  ana@correo.com  ');

      component.reenviarEnlace();
      await fixture.whenStable();

      expect(servicio!.reenviar).toHaveBeenCalledWith('ana@correo.com');
      expect(html.querySelector('.vc-aviso-exito')?.textContent).toContain(
        'Revisa tu correo para continuar con la verificación.',
      );
      expect(component.formulario.controls.correo.value).toBe('');
    });

    /** Verifica el mensaje cuando falla el reenvío. */
    it('muestra un error si no se pudo reenviar', async () => {
      servicio!.reenviar.mockReturnValue(throwError(() => new Error('429')));
      await escribirCorreo('ana@correo.com');

      component.reenviarEnlace();
      await fixture.whenStable();

      expect(html.querySelector('.vc-aviso-error')?.textContent?.trim()).toBe(
        'No pudimos solicitar el enlace. Revisa el correo e inténtalo nuevamente.',
      );
    });
  });

  /**
   * Verifica el aviso y el formulario desactivado cuando no hay un proveedor de {@link
   * VerificacionCorreoService}.
   */
  it('desactiva todo cuando el servicio no está disponible', async () => {
    servicio = null;

    await abrir(conToken('token-123'));

    expect(html.textContent).toContain(
      'La verificación y el reenvío de correos aún no están disponibles.',
    );
    expect((html.querySelector('fieldset') as HTMLFieldSetElement).disabled).toBe(true);
    expect(component.estado()).toBe('pendiente');
  });
});
