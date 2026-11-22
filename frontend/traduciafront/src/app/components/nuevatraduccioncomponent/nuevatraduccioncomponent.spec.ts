import { HttpErrorResponse } from '@angular/common/http';
import { signal } from '@angular/core';
import type { WritableSignal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { BehaviorSubject, Subject, of, throwError } from 'rxjs';
import type { Mock } from 'vitest';
import { MAX_ARCHIVO_BYTES } from '../../api-config';
import type { Idioma } from '../../models/idiomamodel';
import type {
  SolicitudTraduccionResponseDTO,
  TraduccionResponseDTO,
} from '../../models/traduccionmodel';
import type { User } from '../../models/usermodel';
import { AuthService } from '../../services/authservice';
import { DocumentoService } from '../../services/documentoservice';
import { GrabacionService } from '../../services/grabacionservice';
import { IdiomaService } from '../../services/idiomaservice';
import { TraduccionService } from '../../services/traduccionservice';
import { TranscripcionService } from '../../services/transcripcionservice';
import { Archivoselectorcomponent } from '../archivoselectorcomponent/archivoselectorcomponent';
import { Idiomaselectorcomponent } from '../idiomaselectorcomponent/idiomaselectorcomponent';
import { Nuevatraduccioncomponent } from './nuevatraduccioncomponent';

/** Idiomas disponibles de ejemplo, en un orden distinto al que elige el componente. */
const IDIOMAS: Idioma[] = [
  { id: 'fr', nombre: 'Francés', voz: 'fr-FR' },
  { id: 'en', nombre: 'Inglés', voz: 'en-US' },
  { id: 'es', nombre: 'Español', voz: 'es-CO' },
];

/** Resultado de un proveedor; `null` representa una traducción fallida. */
function resultado(
  proveedorTraduccion: TraduccionResponseDTO['proveedorTraduccion'],
  textoTraducido: string | null,
): TraduccionResponseDTO {
  return {
    estado: textoTraducido === null ? 'FALLIDA' : 'EXITOSA',
    textoTraducido,
    tipoProveedor: proveedorTraduccion === 'DEEPL' ? 'TRADICIONAL' : 'IA',
    proveedorTraduccion,
  };
}

/** Crea la respuesta del backend con los resultados indicados. */
function respuestaCon(...listaTraduccion: TraduccionResponseDTO[]): SolicitudTraduccionResponseDTO {
  return {
    id: 30,
    fechaCreacion: '2026-05-10T10:00:00',
    textoOriginal: 'Buenos días',
    origen: 'es',
    destino: 'en',
    listaTraduccion,
  };
}

/** Crea un documento de prueba con el nombre y el tamaño indicados. */
function documento(nombre = 'informe.pdf', bytes = 128): File {
  return new File([new Uint8Array(bytes)], nombre, { type: 'application/pdf' });
}

/** Crea un error HTTP del backend con el estado y el detalle indicados. */
function errorHttp(status: number, detail?: string): HttpErrorResponse {
  return new HttpErrorResponse({ status, error: detail ? { detail } : null });
}

/**
 * Pruebas unitarias de {@link Nuevatraduccioncomponent}.
 *
 * Validan la carga de idiomas, el límite de caracteres según el plan y el servidor, la
 * traducción y sus errores, la extracción de documentos, el dictado por voz (grabación y
 * transcripción) y la incorporación de texto sin recortarlo. Todos los servicios se simulan;
 * los que el componente declara en sus propios `providers` (grabación y traducción) se
 * reemplazan con `overrideComponent`. Los selectores de idioma y de archivo se usan reales.
 */
describe('Nuevatraduccioncomponent', () => {
  let fixture: ComponentFixture<Nuevatraduccioncomponent>;
  let component: Nuevatraduccioncomponent;
  let html: HTMLElement;
  let sesion$: BehaviorSubject<User | null>;
  let jwt: string | null;
  let idiomaService: { getListaIdioma: Mock };
  let traduccionService: { traducir: Mock };
  let documentoService: { extraer: Mock };
  let transcripcionService: { transcribir: Mock };
  let grabacion: {
    estado: WritableSignal<'inactivo' | 'solicitando' | 'grabando' | 'deteniendo'>;
    iniciar: Mock;
    detener: Mock;
    cancelar: Mock;
  };

  const textarea = () => html.querySelector('textarea') as HTMLTextAreaElement;
  const botonTraducir = () => html.querySelector('.boton-traducir') as HTMLButtonElement;
  const errores = () =>
    Array.from(html.querySelectorAll('.error')).map((e) => e.textContent?.trim());
  const texto = () => component.form.controls.texto;

  /** Escribe en el área de texto como lo haría el usuario. */
  async function escribir(valor: string): Promise<void> {
    textarea().value = valor;
    textarea().dispatchEvent(new Event('input'));
    await fixture.whenStable();
  }

  /** Crea el componente y espera la carga inicial. */
  async function crearComponente(): Promise<void> {
    fixture = TestBed.createComponent(Nuevatraduccioncomponent);
    component = fixture.componentInstance;
    html = fixture.nativeElement;
    await fixture.whenStable();
  }

  beforeEach(async () => {
    sesion$ = new BehaviorSubject<User | null>(null);
    jwt = 'jwt-de-prueba';
    idiomaService = { getListaIdioma: vi.fn().mockReturnValue(of(IDIOMAS)) };
    traduccionService = {
      traducir: vi.fn().mockReturnValue(of(respuestaCon(resultado('DEEPL', 'Good morning')))),
    };
    documentoService = { extraer: vi.fn() };
    transcripcionService = { transcribir: vi.fn() };
    grabacion = {
      estado: signal('inactivo'),
      iniciar: vi.fn(),
      detener: vi.fn(),
      cancelar: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule],
      declarations: [Nuevatraduccioncomponent, Idiomaselectorcomponent, Archivoselectorcomponent],
      providers: [
        {
          provide: AuthService,
          useValue: {
            getUserAsObservable: () => sesion$.asObservable(),
            getUser: () => sesion$.value,
            getJwt: () => jwt,
          },
        },
        { provide: IdiomaService, useValue: idiomaService },
        { provide: DocumentoService, useValue: documentoService },
        { provide: TranscripcionService, useValue: transcripcionService },
      ],
    })
      .overrideComponent(Nuevatraduccioncomponent, {
        set: {
          providers: [
            { provide: GrabacionService, useValue: grabacion },
            { provide: TraduccionService, useValue: traduccionService },
          ],
        },
      })
      .compileComponents();
  });

  describe('idiomas', () => {
    /** Verifica que se carguen los idiomas y se preseleccionen español como origen e inglés como destino. */
    it('carga los idiomas y propone traducir de español a inglés', async () => {
      await crearComponente();

      expect(component.listaIdioma()).toEqual(IDIOMAS);
      expect(component.form.getRawValue()).toMatchObject({ origen: 'es', destino: 'en' });
    });

    /** Verifica que, sin español ni inglés, se elijan los dos primeros idiomas distintos. */
    it('elige otros idiomas cuando no están español ni inglés', async () => {
      idiomaService.getListaIdioma.mockReturnValue(
        of([
          { id: 'pt', nombre: 'Portugués', voz: 'pt-BR' },
          { id: 'it', nombre: 'Italiano', voz: 'it-IT' },
        ]),
      );

      await crearComponente();

      expect(component.form.getRawValue()).toMatchObject({ origen: 'pt', destino: 'it' });
    });

    /** Verifica el aviso cuando solo hay un idioma disponible. */
    it('avisa cuando no hay suficientes idiomas para traducir', async () => {
      idiomaService.getListaIdioma.mockReturnValue(of([IDIOMAS[2]]));

      await crearComponente();

      expect(errores()).toContain('No hay suficientes idiomas disponibles para traducir.');
    });

    /** Verifica el error de carga de idiomas y que el botón de reintentar vuelva a consultar. */
    it('muestra un error y permite reintentar si falla la carga', async () => {
      idiomaService.getListaIdioma.mockReturnValue(throwError(() => new Error('503')));
      await crearComponente();

      expect(errores()).toContain('No fue posible cargar los idiomas. Inténtalo nuevamente.');

      idiomaService.getListaIdioma.mockReturnValue(of(IDIOMAS));
      (html.querySelector('.estado button') as HTMLButtonElement).click();
      await fixture.whenStable();

      expect(idiomaService.getListaIdioma).toHaveBeenCalledTimes(2);
      expect(component.errorIdiomas()).toBe('');
    });

    /** Verifica que no se lance otra consulta mientras la primera sigue pendiente y que no se pueda traducir. */
    it('no repite la consulta mientras los idiomas siguen cargando', async () => {
      idiomaService.getListaIdioma.mockReturnValue(new Subject());
      await crearComponente();

      component.cargarIdiomas();

      expect(idiomaService.getListaIdioma).toHaveBeenCalledTimes(1);
      expect(botonTraducir().disabled).toBe(true);
    });
  });

  describe('límite de caracteres', () => {
    /** Verifica que sin sesión se use el límite de 500 caracteres del plan gratuito. */
    it('usa el límite del plan gratuito cuando no hay sesión', async () => {
      await crearComponente();

      expect(component.limiteCaracteres()).toBe(500);
      expect(html.querySelector('#contador')?.textContent).toMatch(/0\s*\/\s*500 caracteres/);
    });

    /** Verifica que con el plan orIA el límite suba a 2000 caracteres. */
    it('amplía el límite según el plan del usuario', async () => {
      sesion$.next({ nombre: 'laura', correo: 'laura@correo.com', rol: 'USER', plan: 'oria' });
      await crearComponente();

      await escribir('a'.repeat(1500));

      expect(component.limiteCaracteres()).toBe(2000);
      expect(texto().valid).toBe(true);
    });

    /** Verifica que al cerrar sesión se vuelva al límite gratuito y un texto largo quede inválido. */
    it('vuelve al límite gratuito si el usuario cierra sesión', async () => {
      sesion$.next({ nombre: 'laura', correo: 'laura@correo.com', rol: 'USER', plan: 'infinitia' });
      await crearComponente();
      texto().setValue('a'.repeat(800));

      sesion$.next(null);
      await fixture.whenStable();

      expect(component.limiteCaracteres()).toBe(500);
      expect(texto().hasError('maxlength')).toBe(true);
    });
  });

  describe('traducir', () => {
    beforeEach(async () => {
      await crearComponente();
    });

    /** Verifica que un texto con solo espacios no se envíe y muestre los mensajes de error. */
    it('no envía un texto vacío o con solo espacios', async () => {
      await escribir('    ');

      component.traducir();
      await fixture.whenStable();

      expect(traduccionService.traducir).not.toHaveBeenCalled();
      expect(errores()).toContain('Escribe un texto que no contenga únicamente espacios.');
      expect(errores()).toContain('Revisa el texto, su límite y los idiomas seleccionados.');
    });

    /** Verifica el error cuando el origen y el destino son iguales y que el botón quede desactivado. */
    it('no permite elegir el mismo idioma de origen y destino', async () => {
      await escribir('Hola');
      component.form.controls.destino.setValue('es');
      await fixture.whenStable();

      expect(component.form.hasError('mismosIdiomas')).toBe(true);
      expect(errores()).toContain('Elige idiomas de origen y destino diferentes.');
      expect(botonTraducir().disabled).toBe(true);
    });

    /** Verifica que sin sesión no se envíe la traducción y se pida iniciar sesión. */
    it('pide iniciar sesión antes de traducir', async () => {
      jwt = null;
      await escribir('Buenos días');

      component.traducir();

      expect(traduccionService.traducir).not.toHaveBeenCalled();
      expect(component.error()).toBe(
        'Inicia sesión para utilizar documentos, audio y traducciones.',
      );
    });

    /** Verifica que el texto se envíe con sus espacios y saltos de línea originales. */
    it('envía el texto tal como lo escribió el usuario', async () => {
      await escribir('  Buenos días\ncolega  ');

      botonTraducir().click();
      await fixture.whenStable();

      expect(traduccionService.traducir).toHaveBeenCalledWith({
        texto: '  Buenos días\ncolega  ',
        origen: 'es',
        destino: 'en',
      });
    });

    /** Verifica que se guarden los resultados y se muestre la traducción exitosa. */
    it('muestra el resultado de los proveedores', async () => {
      await escribir('Buenos días');

      component.traducir();
      await fixture.whenStable();

      expect(component.resultados()).toEqual([resultado('DEEPL', 'Good morning')]);
      expect(component.error()).toBe('');
      expect(component.aviso()).toBe('');
      expect(html.textContent).toContain('Good morning');
    });

    /** Verifica el aviso cuando algunos proveedores fallan y otros no. */
    it('avisa cuando solo algunos proveedores respondieron', async () => {
      traduccionService.traducir.mockReturnValue(
        of(respuestaCon(resultado('DEEPL', 'Good morning'), resultado('GEMMA', null))),
      );
      await escribir('Buenos días');

      component.traducir();

      expect(component.resultados()).toHaveLength(2);
      expect(component.aviso()).toBe(
        'Algunos proveedores no respondieron. Puedes consultar los resultados disponibles.',
      );
    });

    /** Verifica el error cuando ningún proveedor pudo traducir. */
    it('avisa cuando ningún proveedor pudo traducir', async () => {
      traduccionService.traducir.mockReturnValue(
        of(respuestaCon(resultado('DEEPL', null), resultado('GEMMA', null))),
      );
      await escribir('Buenos días');

      component.traducir();

      expect(component.error()).toBe('Ningún proveedor pudo completar la traducción.');
    });

    /** Verifica que el formulario se bloquee mientras se traduce y se libere al recibir la respuesta. */
    it('bloquea el formulario mientras espera la respuesta', async () => {
      const pendiente = new Subject<SolicitudTraduccionResponseDTO>();
      traduccionService.traducir.mockReturnValue(pendiente);
      await escribir('Buenos días');

      component.traducir();
      await fixture.whenStable();

      expect(component.estado()).toBe('traduciendo');
      expect(component.mensajeEstado()).toBe('Traduciendo el texto…');
      expect(component.form.disabled).toBe(true);

      pendiente.next(respuestaCon(resultado('DEEPL', 'Good morning')));
      pendiente.complete();
      await fixture.whenStable();

      expect(component.ocupado()).toBe(false);
      expect(component.form.enabled).toBe(true);
    });

    /** Verifica que se muestre el detalle que envía el backend y que el formulario quede libre. */
    it('muestra el detalle del backend si rechaza la traducción', async () => {
      traduccionService.traducir.mockReturnValue(
        throwError(() => errorHttp(400, 'El texto supera el límite de tu plan.')),
      );
      await escribir('Buenos días');

      component.traducir();

      expect(component.error()).toBe('El texto supera el límite de tu plan.');
      expect(component.form.enabled).toBe(true);
    });

    /** Verifica el mensaje genérico cuando el error del backend no trae detalle. */
    it('muestra un mensaje genérico si el error no trae detalle', async () => {
      traduccionService.traducir.mockReturnValue(throwError(() => errorHttp(500)));
      await escribir('Buenos días');

      component.traducir();

      expect(component.error()).toBe('No fue posible completar la traducción.');
    });

    /** Verifica que una sesión vencida o sin permisos pida volver a iniciar sesión. */
    it('pide volver a iniciar sesión si el backend responde 401', async () => {
      traduccionService.traducir.mockReturnValue(throwError(() => errorHttp(401)));
      await escribir('Buenos días');

      component.traducir();

      expect(component.error()).toBe(
        'La sesión venció o no tiene permiso. Vuelve a iniciar sesión.',
      );
    });

    /** Verifica el mensaje cuando el backend no responde (estado 0). */
    it('avisa cuando no hay conexión con el backend', async () => {
      traduccionService.traducir.mockReturnValue(throwError(() => errorHttp(0)));
      await escribir('Buenos días');

      component.traducir();

      expect(component.error()).toBe(
        'No se pudo conectar con el backend. Revisa su dirección y la configuración CORS.',
      );
    });

    /** Verifica que al editar el formulario se borren los resultados anteriores. */
    it('borra los resultados al editar el texto', async () => {
      await escribir('Buenos días');
      component.traducir();
      expect(component.resultados()).toHaveLength(1);

      await escribir('Buenas noches');

      expect(component.resultados()).toEqual([]);
    });
  });

  describe('documentos', () => {
    beforeEach(async () => {
      await crearComponente();
    });

    /** Verifica que se extraiga el documento en el idioma de origen y se agregue su texto al editor. */
    it('extrae el texto del documento y lo agrega al editor', () => {
      const archivo = documento();
      documentoService.extraer.mockReturnValue(
        of({
          texto: 'Contrato de arriendo',
          caracteres: 20,
          limiteCaracteres: 500,
          excedeLimite: false,
        }),
      );

      component.alSeleccionarArchivo(archivo);

      expect(documentoService.extraer).toHaveBeenCalledWith(archivo, 'es');
      expect(texto().value).toBe('Contrato de arriendo');
      expect(component.aviso()).toBe('Texto de «informe.pdf» incorporado.');
      expect(component.estado()).toBe('reposo');
    });

    /** Verifica que lo que ya estaba escrito se conserve y el documento se agregue en un párrafo nuevo. */
    it('conserva lo escrito y agrega el documento en un párrafo nuevo', () => {
      texto().setValue('Notas previas');
      documentoService.extraer.mockReturnValue(
        of({
          texto: 'Contrato de arriendo',
          caracteres: 20,
          limiteCaracteres: 500,
          excedeLimite: false,
        }),
      );

      component.alSeleccionarArchivo(documento());

      expect(texto().value).toBe('Notas previas\n\nContrato de arriendo');
    });

    /** Verifica que el límite que informa el servidor reemplace al del plan y se pida reducir el texto. */
    it('aplica el límite del servidor y pide reducir un texto demasiado largo', () => {
      documentoService.extraer.mockReturnValue(
        of({ texto: 'a'.repeat(900), caracteres: 900, limiteCaracteres: 800, excedeLimite: true }),
      );

      component.alSeleccionarArchivo(documento());

      expect(component.limiteCaracteres()).toBe(800);
      expect(texto().value).toHaveLength(900);
      expect(texto().hasError('maxlength')).toBe(true);
      expect(component.aviso()).toContain('Reduce el texto a 800 caracteres para traducir.');
    });

    /** Verifica que un formato no admitido se rechace sin llamar al backend. */
    it('rechaza formatos no admitidos', () => {
      component.alSeleccionarArchivo(documento('instalador.exe'));

      expect(component.error()).toBe('Selecciona un documento o imagen de un formato admitido.');
      expect(documentoService.extraer).not.toHaveBeenCalled();
    });

    /** Verifica que se rechacen los archivos vacíos y los de más de 10 MB. */
    it('rechaza archivos vacíos o de más de 10 MB', () => {
      component.alSeleccionarArchivo(documento('vacio.pdf', 0));
      expect(component.error()).toBe('Selecciona un archivo no vacío de hasta 10 MB.');

      component.alSeleccionarArchivo(documento('enorme.pdf', MAX_ARCHIVO_BYTES + 1));
      expect(component.error()).toBe('Selecciona un archivo no vacío de hasta 10 MB.');

      expect(documentoService.extraer).not.toHaveBeenCalled();
    });

    /** Verifica que se pida elegir el idioma de origen antes de extraer el documento. */
    it('pide elegir el idioma de origen antes de extraer', () => {
      component.form.controls.origen.setValue('');

      component.alSeleccionarArchivo(documento());

      expect(component.error()).toBe('Selecciona el idioma de origen del documento.');
      expect(documentoService.extraer).not.toHaveBeenCalled();
    });

    /** Verifica que sin sesión no se envíe el documento. */
    it('no envía el documento sin sesión', () => {
      jwt = null;

      component.alSeleccionarArchivo(documento());

      expect(component.error()).toBe(
        'Inicia sesión para utilizar documentos, audio y traducciones.',
      );
      expect(documentoService.extraer).not.toHaveBeenCalled();
    });

    /** Verifica que el editor se bloquee mientras se extrae el documento. */
    it('bloquea el editor mientras extrae el documento', () => {
      documentoService.extraer.mockReturnValue(new Subject());

      component.alSeleccionarArchivo(documento());

      expect(component.estado()).toBe('extrayendo');
      expect(component.mensajeEstado()).toBe('Extrayendo el texto del documento…');
      expect(component.form.disabled).toBe(true);
    });

    /** Verifica que se muestre el detalle del backend si no puede extraer el documento. */
    it('muestra el error si no se puede extraer el documento', () => {
      documentoService.extraer.mockReturnValue(
        throwError(() => errorHttp(422, 'El PDF está protegido con contraseña.')),
      );

      component.alSeleccionarArchivo(documento());

      expect(component.error()).toBe('El PDF está protegido con contraseña.');
      expect(component.form.enabled).toBe(true);
    });

    /** Verifica el error cuando el documento no tiene texto que incorporar. */
    it('avisa si el documento no tiene texto', () => {
      documentoService.extraer.mockReturnValue(
        of({ texto: '   ', caracteres: 3, limiteCaracteres: 500, excedeLimite: false }),
      );

      component.alSeleccionarArchivo(documento());

      expect(component.error()).toBe('No se encontró texto para incorporar.');
      expect(texto().value).toBe('');
    });

    /** Verifica que el motivo emitido por el selector de archivos se muestre como error. */
    it('muestra el motivo cuando el selector rechaza un archivo', async () => {
      const selector: Archivoselectorcomponent = fixture.debugElement.query(
        By.directive(Archivoselectorcomponent),
      ).componentInstance;

      selector.archivoRechazado.emit('El archivo está vacío.');
      await fixture.whenStable();

      expect(errores()).toContain('El archivo está vacío.');
    });
  });

  describe('dictado por voz', () => {
    const audio = new File(['audio'], 'grabacion.webm', { type: 'audio/webm' });

    beforeEach(async () => {
      await crearComponente();
    });

    /** Verifica que la grabación se transcriba en el idioma de origen y el texto se agregue al editor. */
    it('graba, transcribe y agrega el texto', async () => {
      grabacion.iniciar.mockResolvedValue(audio);
      transcripcionService.transcribir.mockReturnValue(
        of({ texto: 'Hola desde el micrófono', caracteres: 23 }),
      );

      await component.iniciarGrabacion();

      expect(transcripcionService.transcribir).toHaveBeenCalledWith(audio, 'es');
      expect(texto().value).toBe('Hola desde el micrófono');
      expect(component.aviso()).toBe('Audio transcrito. Revisa el texto antes de traducir.');
      expect(component.estado()).toBe('reposo');
    });

    /** Verifica que sin idioma de origen se deje la detección del idioma al backend. */
    it('usa detección automática si no hay idioma de origen', async () => {
      component.form.controls.origen.setValue('');
      grabacion.iniciar.mockResolvedValue(audio);
      transcripcionService.transcribir.mockReturnValue(of({ texto: 'Hello', caracteres: 5 }));

      await component.iniciarGrabacion();

      expect(transcripcionService.transcribir).toHaveBeenCalledWith(audio, undefined);
    });

    /** Verifica los mensajes mientras se pide el permiso y mientras se graba. */
    it('indica qué hacer mientras pide el micrófono y mientras graba', () => {
      grabacion.iniciar.mockReturnValue(new Promise(() => undefined));

      void component.iniciarGrabacion();
      grabacion.estado.set('solicitando');
      expect(component.mensajeEstado()).toBe('Permite el acceso al micrófono para comenzar.');

      grabacion.estado.set('grabando');
      expect(component.escuchando()).toBe(true);
      expect(component.mensajeEstado()).toBe(
        'Grabando. Pulsa detener para transcribir. Máximo: 2 minutos.',
      );
      expect(component.form.disabled).toBe(true);
    });

    /** Verifica que detener y cancelar deleguen en el servicio de grabación. */
    it('detiene o cancela la grabación a través del servicio', () => {
      component.detenerGrabacion();
      component.cancelarGrabacion();

      expect(grabacion.detener).toHaveBeenCalled();
      expect(grabacion.cancelar).toHaveBeenCalled();
    });

    /** Verifica el aviso cuando el usuario cancela la grabación y que no se transcriba nada. */
    it('avisa cuando se cancela la grabación', async () => {
      grabacion.iniciar.mockResolvedValue(null);

      await component.iniciarGrabacion();

      expect(component.aviso()).toBe('Grabación cancelada.');
      expect(component.estado()).toBe('reposo');
      expect(transcripcionService.transcribir).not.toHaveBeenCalled();
    });

    /** Verifica el mensaje según el motivo por el que no se pudo usar el micrófono. */
    it.each([
      [
        'el permiso fue denegado',
        new DOMException('denegado', 'NotAllowedError'),
        'Permite el acceso al micrófono en la configuración del navegador.',
      ],
      [
        'no hay micrófono',
        new DOMException('no encontrado', 'NotFoundError'),
        'No se encontró un micrófono disponible.',
      ],
      [
        'la página no es segura',
        new Error('El micrófono requiere HTTPS o localhost.'),
        'El micrófono requiere HTTPS o localhost.',
      ],
    ])('explica el problema cuando %s', async (_caso, problema, mensaje) => {
      grabacion.iniciar.mockRejectedValue(problema);

      await component.iniciarGrabacion();

      expect(component.error()).toBe(mensaje);
      expect(component.estado()).toBe('reposo');
    });

    /** Verifica que sin sesión no se pida el micrófono. */
    it('no graba sin sesión', async () => {
      jwt = null;

      await component.iniciarGrabacion();

      expect(grabacion.iniciar).not.toHaveBeenCalled();
      expect(component.error()).toBe(
        'Inicia sesión para utilizar documentos, audio y traducciones.',
      );
    });

    /** Verifica el mensaje genérico cuando falla la transcripción y que el editor quede libre. */
    it('muestra un error si falla la transcripción', async () => {
      grabacion.iniciar.mockResolvedValue(audio);
      transcripcionService.transcribir.mockReturnValue(throwError(() => errorHttp(500)));

      await component.iniciarGrabacion();

      expect(component.error()).toBe('No fue posible transcribir el audio.');
      expect(component.form.enabled).toBe(true);
    });
  });

  describe('contenido adicional', () => {
    beforeEach(async () => {
      await crearComponente();
    });

    /** Verifica que el texto adicional se agregue en un párrafo nuevo. */
    it('agrega el texto en un párrafo nuevo', () => {
      texto().setValue('Hola');

      component.agregarTexto('mundo');

      expect(texto().value).toBe('Hola\n\nmundo');
      expect(component.aviso()).toBe('Texto incorporado.');
    });

    /** Verifica que se conserve todo el texto aunque supere el límite y se pida reducirlo. */
    it('conserva el texto aunque supere el límite y pide reducirlo', () => {
      texto().setValue('a'.repeat(495));

      component.agregarTexto('123456');

      expect(texto().value).toHaveLength(503);
      expect(texto().hasError('maxlength')).toBe(true);
      expect(component.aviso()).toContain('Reduce el texto a 500 caracteres para traducir.');
    });

    /** Verifica que un texto adicional vacío no modifique el editor y se informe. */
    it('avisa si el texto adicional está vacío', () => {
      component.agregarTexto('   ');

      expect(texto().value).toBe('');
      expect(component.error()).toBe('No se encontró texto para incorporar.');
    });

    /** Verifica que no se agregue texto mientras hay una operación en curso. */
    it('no agrega texto mientras hay una operación en curso', async () => {
      traduccionService.traducir.mockReturnValue(new Subject());
      await escribir('Buenos días');
      component.traducir();

      component.agregarTexto('interrupción');

      expect(texto().value).toBe('Buenos días');
    });
  });
});
