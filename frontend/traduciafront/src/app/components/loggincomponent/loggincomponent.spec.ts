import { HttpErrorResponse } from '@angular/common/http';
import { signal } from '@angular/core';
import type { WritableSignal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { of, throwError } from 'rxjs';
import type { Mock } from 'vitest';
import type { User } from '../../models/usermodel';
import { AuthService } from '../../services/authservice';
import { TerminosService } from '../../services/terminosservice';
import { Terminocomponent } from '../terminocomponent/terminocomponent';
import { Loggincomponent } from './loggincomponent';

/** Usuario que devuelve el inicio de sesión simulado. */
const laura: User = { nombre: 'laura', correo: 'laura@correo.com', rol: 'USER', plan: 'gratuito' };

/**
 * Pruebas unitarias de {@link Loggincomponent}.
 *
 * Validan los formularios de inicio de sesión y registro: validaciones y mensajes, normalización de
 * los datos, navegación según el rol, errores del backend y el bloqueo mientras no se acepten los
 * términos. {@link AuthService} y {@link TerminosService} se simulan; {@link Terminocomponent} se
 * usa real porque el login interactúa con su modal.
 */
describe('Loggincomponent', () => {
  let fixture: ComponentFixture<Loggincomponent>;
  let component: Loggincomponent;
  let html: HTMLElement;
  let auth: { iniciarSesion: Mock; registrarUsuario: Mock };
  let terminos: {
    aceptados: WritableSignal<boolean>;
    persistenciaDisponible: WritableSignal<boolean>;
    aceptar: Mock;
  };
  let navegar: Mock;

  const mensaje = () => html.querySelector('.acceso-mensaje')?.textContent?.trim();
  const enviar = () => html.querySelector<HTMLButtonElement>('.boton-acceso')!;

  /** Escribe en un campo del formulario como lo haría el usuario. */
  async function escribir(id: string, valor: string): Promise<void> {
    const campo = html.querySelector(`#${id}`) as HTMLInputElement;
    campo.value = valor;
    campo.dispatchEvent(new Event('input'));
    await fixture.whenStable();
  }

  /** Llena el formulario de registro con datos válidos; `cambios` permite alterar algún campo. */
  async function llenarRegistro(cambios: Partial<Record<string, string>> = {}): Promise<void> {
    component.registro.setValue({
      nombre: 'camilo',
      ciudad: 'Bogotá',
      correo: 'camilo@correo.com',
      edad: '24',
      password: 'Clave2026*',
      confirmacion: 'Clave2026*',
      ...cambios,
    });
    await fixture.whenStable();
  }

  /** Crea el componente y espera el primer renderizado. */
  async function crearComponente(): Promise<void> {
    fixture = TestBed.createComponent(Loggincomponent);
    component = fixture.componentInstance;
    html = fixture.nativeElement;
    await fixture.whenStable();
  }

  beforeEach(async () => {
    auth = {
      iniciarSesion: vi.fn().mockReturnValue(of(laura)),
      registrarUsuario: vi.fn().mockReturnValue(of(laura)),
    };
    terminos = {
      aceptados: signal(true),
      persistenciaDisponible: signal(true),
      aceptar: vi.fn(() => terminos.aceptados.set(true)),
    };

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, RouterModule.forRoot([])],
      declarations: [Loggincomponent, Terminocomponent],
      providers: [
        { provide: AuthService, useValue: auth },
        { provide: TerminosService, useValue: terminos },
      ],
    }).compileComponents();

    navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true) as Mock;
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  /** Formulario de acceso. */
  describe('inicio de sesión', () => {
    beforeEach(async () => {
      await crearComponente();
    });

    /** Verifica que la página empiece en modo de inicio de sesión. */
    it('muestra el formulario de acceso por defecto', () => {
      expect(html.querySelector('h1')?.textContent).toContain('Bienvenido a TraducIA');
      expect(html.querySelector('#acceso-nombre')).not.toBeNull();
      expect(html.querySelector('#registro-correo')).toBeNull();
    });

    /**
     * Verifica que con campos vacíos no se llame al servicio y se muestren los mensajes de
     * validación.
     */
    it('no envía el formulario si faltan datos', async () => {
      component.iniciarSesion();
      await fixture.whenStable();

      expect(auth.iniciarSesion).not.toHaveBeenCalled();
      expect(mensaje()).toBe('Revisa los campos para continuar.');
      expect(html.querySelector('#error-acceso-nombre')?.textContent?.trim()).toBe(
        'Completa este campo.',
      );
    });

    /** Verifica que un nombre de usuario con solo espacios no sea válido. */
    it('no acepta un nombre de usuario formado solo por espacios', async () => {
      await escribir('acceso-nombre', '    ');
      await escribir('acceso-password', 'Clave2026*');

      component.iniciarSesion();

      expect(auth.iniciarSesion).not.toHaveBeenCalled();
    });

    /**
     * Verifica que se recorte el nombre de usuario pero la contraseña se envíe exactamente como se
     * escribió.
     */
    it('envía las credenciales sin espacios sobrantes en el usuario', async () => {
      const solicitados: unknown[] = [];
      component.accesoSolicitado.subscribe((datos) => solicitados.push(datos));
      await escribir('acceso-nombre', '  laura ');
      await escribir('acceso-password', ' Clave2026* ');

      enviar().click();
      await fixture.whenStable();

      const esperado = { nombre: 'laura', password: ' Clave2026* ' };
      expect(auth.iniciarSesion).toHaveBeenCalledWith(esperado);
      expect(solicitados).toEqual([esperado]);
    });

    /** Verifica que un usuario con rol USER termine en `/dashboard/new`. */
    it('lleva al usuario a su dashboard', async () => {
      await escribir('acceso-nombre', 'laura');
      await escribir('acceso-password', 'Clave2026*');

      component.iniciarSesion();
      await fixture.whenStable();

      expect(navegar).toHaveBeenCalledWith(['/dashboard/new']);
      expect(mensaje()).toBe('');
    });

    /** Verifica que un usuario con rol ADMIN termine en `/admin`. */
    it('lleva al administrador a su panel', async () => {
      auth.iniciarSesion.mockReturnValue(of({ ...laura, rol: 'ADMIN' }));
      await escribir('acceso-nombre', 'admin');
      await escribir('acceso-password', 'Clave2026*');

      component.iniciarSesion();

      expect(navegar).toHaveBeenCalledWith(['/admin']);
    });

    /**
     * Verifica que se muestre el mensaje del backend cuando rechaza el acceso y que no haya
     * navegación.
     */
    it('muestra el mensaje que envía el backend cuando rechaza el acceso', async () => {
      auth.iniciarSesion.mockReturnValue(
        throwError(
          () => new HttpErrorResponse({ status: 403, error: 'Debes verificar tu correo primero.' }),
        ),
      );
      await escribir('acceso-nombre', 'laura');
      await escribir('acceso-password', 'Clave2026*');

      component.iniciarSesion();
      await fixture.whenStable();

      expect(mensaje()).toBe('Debes verificar tu correo primero.');
      expect(navegar).not.toHaveBeenCalled();
    });

    /** Verifica el mensaje genérico cuando el error no trae texto. */
    it('muestra un mensaje genérico si el error no trae detalle', async () => {
      auth.iniciarSesion.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 0 })));
      await escribir('acceso-nombre', 'laura');
      await escribir('acceso-password', 'Clave2026*');

      component.iniciarSesion();
      await fixture.whenStable();

      expect(mensaje()).toBe('No fue posible iniciar sesión. Revisa tus datos.');
    });

    /** Verifica que el botón del ojo alterne el tipo del campo y su etiqueta accesible. */
    it('muestra u oculta la contraseña', async () => {
      const ojo = html.querySelector('.boton-ojo') as HTMLButtonElement;

      ojo.click();
      await fixture.whenStable();
      expect(html.querySelector('#acceso-password')?.getAttribute('type')).toBe('text');
      expect(ojo.getAttribute('aria-label')).toBe('Ocultar contraseña');

      ojo.click();
      await fixture.whenStable();
      expect(html.querySelector('#acceso-password')?.getAttribute('type')).toBe('password');
    });

    /** Verifica el enlace a la recuperación de contraseña. */
    it('enlaza a la recuperación de contraseña', () => {
      expect(html.querySelector('.acceso-recuperar')?.getAttribute('href')).toBe(
        '/recuperar-contrasena',
      );
    });
  });

  /** Formulario de creación de cuenta. */
  describe('registro', () => {
    beforeEach(async () => {
      await crearComponente();
      (html.querySelector('.boton-cambiar') as HTMLButtonElement).click();
      await fixture.whenStable();
    });

    /** Verifica que el botón del pie cambie al formulario de registro. */
    it('cambia al formulario de registro', () => {
      expect(component.modoRegistro()).toBe(true);
      expect(html.querySelector('h1')?.textContent).toContain('Crea tu cuenta');
      expect(html.querySelector('#registro-correo')).not.toBeNull();
    });

    /**
     * Verifica que al cambiar de modo se borren las contraseñas, se conserven los demás datos y las
     * contraseñas vuelvan a ocultarse.
     */
    it('borra las contraseñas al cambiar de formulario', async () => {
      await llenarRegistro();
      component.passwordVisible.set(true);

      component.cambiarModo();

      expect(component.registro.controls.password.value).toBe('');
      expect(component.registro.controls.confirmacion.value).toBe('');
      expect(component.registro.controls.nombre.value).toBe('camilo');
      expect(component.passwordVisible()).toBe(false);
    });

    /**
     * Verifica que el registro recorte los textos, envíe al backend solo usuario, correo y
     * contraseña, y emita además la ciudad y la edad como número.
     */
    it('crea la cuenta enviando al backend solo usuario, correo y contraseña', async () => {
      const emitidos: unknown[] = [];
      component.registroSolicitado.subscribe((datos) => emitidos.push(datos));
      await llenarRegistro({
        nombre: ' camilo ',
        ciudad: ' Bogotá ',
        correo: ' camilo@correo.com ',
      });

      enviar().click();
      await fixture.whenStable();

      expect(auth.registrarUsuario).toHaveBeenCalledWith({
        nombre: 'camilo',
        correo: 'camilo@correo.com',
        password: 'Clave2026*',
      });
      expect(emitidos).toEqual([
        {
          nombre: 'camilo',
          correo: 'camilo@correo.com',
          password: 'Clave2026*',
          ciudad: 'Bogotá',
          edad: 24,
        },
      ]);
      expect(mensaje()).toBe(
        'Cuenta creada. Revisa tu correo para verificarla antes de iniciar sesión.',
      );
    });

    /**
     * Verifica que contraseñas distintas impidan el registro y muestren el mensaje correspondiente.
     */
    it('exige que las contraseñas coincidan', async () => {
      await llenarRegistro({ confirmacion: 'OtraClave2026' });

      component.registrarCuenta();
      await fixture.whenStable();

      expect(auth.registrarUsuario).not.toHaveBeenCalled();
      expect(mensaje()).toBe('Revisa los campos para crear tu cuenta.');
      expect(html.querySelector('#error-confirmacion')?.textContent?.trim()).toBe(
        'Las contraseñas deben coincidir.',
      );
    });

    /** Verifica que se muestre el mensaje del backend cuando rechaza el registro. */
    it('muestra el error del backend, por ejemplo un correo repetido', async () => {
      auth.registrarUsuario.mockReturnValue(
        throwError(
          () => new HttpErrorResponse({ status: 409, error: 'El correo ya está registrado' }),
        ),
      );
      await llenarRegistro();

      component.registrarCuenta();
      await fixture.whenStable();

      expect(mensaje()).toBe('El correo ya está registrado');
    });

    /** Verifica el mensaje genérico cuando el registro falla sin texto. */
    it('muestra un mensaje genérico si el registro falla sin detalle', async () => {
      auth.registrarUsuario.mockReturnValue(throwError(() => new Error('timeout')));
      await llenarRegistro();

      component.registrarCuenta();
      await fixture.whenStable();

      expect(mensaje()).toBe('No fue posible crear la cuenta.');
    });

    /** Mensajes que devuelve `errorCampo` para cada tipo de error. */
    describe('mensajes de validación', () => {
      const errorDe = (campo: keyof Loggincomponent['registro']['controls'], valor: string) => {
        const control = component.registro.controls[campo];
        control.setValue(valor);
        control.markAsTouched();
        return component.errorCampo(control);
      };

      /** Verifica que un campo no tocado no muestre error aunque sea inválido. */
      it('no muestra errores en campos que el usuario no ha tocado', () => {
        expect(component.errorCampo(component.registro.controls.nombre)).toBe('');
      });

      /**
       * Verifica el mensaje de cada tipo de error: campo obligatorio, correo, longitudes, rango de
       * edad y formato.
       */
      it.each([
        ['nombre', '', 'Completa este campo.'],
        ['nombre', 'a', 'Ingresa al menos 2 caracteres.'],
        ['correo', 'camilo.correo.com', 'Ingresa un correo válido.'],
        ['password', 'corta', 'Ingresa al menos 8 caracteres.'],
        ['ciudad', 'x'.repeat(81), 'Ingresa hasta 80 caracteres.'],
        ['edad', '150', 'Ingresa una edad entre 1 y 120 años.'],
        ['edad', '0', 'Ingresa una edad entre 1 y 120 años.'],
        ['edad', 'dos', 'Revisa el formato de este campo.'],
        ['ciudad', '   ', 'Revisa el formato de este campo.'],
      ] as const)('%s = "%s" → %s', (campo, valor, esperado) => {
        expect(errorDe(campo, valor)).toBe(esperado);
      });
    });
  });

  /** Relación entre los formularios y el modal de términos. */
  describe('términos y condiciones', () => {
    /**
     * Verifica que sin aceptar los términos el botón esté desactivado, no se llame al servicio y el
     * modal siga abierto.
     */
    it('bloquea el acceso hasta aceptar los términos', async () => {
      terminos.aceptados.set(false);
      await crearComponente();
      const abrir = vi.spyOn(HTMLDialogElement.prototype, 'showModal');
      await escribir('acceso-nombre', 'laura');
      await escribir('acceso-password', 'Clave2026*');

      component.iniciarSesion();
      await fixture.whenStable();

      expect(enviar().disabled).toBe(true);
      expect(auth.iniciarSesion).not.toHaveBeenCalled();
      expect(mensaje()).toBe('Debes aceptar los términos para continuar.');
      expect(abrir).not.toHaveBeenCalled(); // el modal ya estaba abierto desde el inicio
      expect((html.querySelector('dialog') as HTMLDialogElement).open).toBe(true);
    });

    /** Verifica que intentar registrarse sin aceptar vuelva a abrir el modal de términos. */
    it('vuelve a abrir el modal si el usuario lo había cerrado', async () => {
      terminos.aceptados.set(false);
      await crearComponente();
      (html.querySelector('dialog') as HTMLDialogElement).close();

      component.registrarCuenta();

      expect((html.querySelector('dialog') as HTMLDialogElement).open).toBe(true);
      expect(auth.registrarUsuario).not.toHaveBeenCalled();
    });

    /** Verifica que aceptar en el modal registre la aceptación y habilite el envío. */
    it('habilita los formularios cuando el usuario acepta en el modal', async () => {
      terminos.aceptados.set(false);
      await crearComponente();

      (html.querySelector('.tc-aceptar') as HTMLButtonElement).click();
      await fixture.whenStable();

      expect(terminos.aceptar).toHaveBeenCalled();
      expect(enviar().disabled).toBe(false);
      expect(mensaje()).toBe('');
    });

    /** Verifica el aviso cuando el navegador no permite guardar la aceptación. */
    it('avisa cuando la aceptación solo vale para esta sesión', async () => {
      await crearComponente();
      terminos.persistenciaDisponible.set(false);

      component.actualizarAceptacionTerminos(true);

      expect(component.mensaje()).toBe(
        'Aceptación válida para esta sesión. El navegador no permitió recordarla.',
      );
    });

    /** Verifica el mensaje cuando el usuario rechaza los términos. */
    it('recuerda que debe aceptar si rechaza los términos', async () => {
      await crearComponente();

      component.actualizarAceptacionTerminos(false);

      expect(component.mensaje()).toBe('Debes aceptar los términos para continuar.');
    });
  });

  /**
   * Verifica que el botón de Google emita `googleSolicitado` y avise que la integración está
   * pendiente.
   */
  it('avisa que el acceso con Google aún no está conectado', async () => {
    await crearComponente();
    const solicitado = vi.fn();
    component.googleSolicitado.subscribe(solicitado);

    (html.querySelector('.boton-google') as HTMLButtonElement).click();
    await fixture.whenStable();

    expect(solicitado).toHaveBeenCalled();
    expect(mensaje()).toBe('El acceso con Google está pendiente de conexión.');
  });
});
