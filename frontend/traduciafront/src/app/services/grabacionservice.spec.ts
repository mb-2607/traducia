import { TestBed } from '@angular/core/testing';
import type { Mock } from 'vitest';
import { MAX_ARCHIVO_BYTES } from '../api-config';
import { GrabacionService } from './grabacionservice';

/** Pista de audio falsa: solo interesa saber si se detuvo. */
function microfonoFalso(): { flujo: MediaStream; pista: { stop: Mock } } {
  const pista = { stop: vi.fn() };
  return { flujo: { getTracks: () => [pista] } as unknown as MediaStream, pista };
}

/**
 * Imita lo que el servicio usa de `MediaRecorder` para controlar la grabación desde la prueba.
 * A diferencia del navegador, `stop()` dispara `onstop` de inmediato.
 */
class GrabadorFalso {
  static tiposSoportados: string[] = [];
  static instancias: GrabadorFalso[] = [];

  static isTypeSupported(tipo: string): boolean {
    return GrabadorFalso.tiposSoportados.includes(tipo);
  }

  state: RecordingState = 'inactive';
  readonly mimeType: string;
  ondataavailable: ((evento: BlobEvent) => void) | null = null;
  onerror: (() => void) | null = null;
  onstop: (() => void) | null = null;
  readonly start = vi.fn(() => {
    this.state = 'recording';
  });
  readonly stop = vi.fn(() => {
    this.state = 'inactive';
    this.onstop?.();
  });

  constructor(
    readonly flujo: MediaStream,
    opciones: MediaRecorderOptions,
  ) {
    this.mimeType = opciones.mimeType ?? '';
    GrabadorFalso.instancias.push(this);
  }

  /** Simula que el navegador entrega un fragmento de audio. */
  entregar(...partes: BlobPart[]): void {
    this.ondataavailable?.({ data: new Blob(partes) } as BlobEvent);
  }
}

/**
 * Pruebas unitarias de {@link GrabacionService}.
 *
 * Validan los requisitos del navegador antes de grabar, el ciclo completo de una nota de voz
 * (permiso, grabación, detención y archivo resultante), los límites de tamaño y duración, la
 * cancelación y la liberación del micrófono en todos los casos. El micrófono y `MediaRecorder`
 * se reemplazan por dobles de prueba.
 */
describe('GrabacionService', () => {
  let servicio: GrabacionService;
  let getUserMedia: Mock;
  let microfono: ReturnType<typeof microfonoFalso>;

  /** Cambia si el navegador considera la página un contexto seguro (HTTPS o localhost). */
  function contextoSeguro(valor: boolean): void {
    Object.defineProperty(window, 'isSecureContext', { configurable: true, value: valor });
  }

  /** Inicia la grabación y espera a que el micrófono quede grabando. */
  async function empezarGrabacion(): Promise<{
    resultado: Promise<File | null>;
    grabador: GrabadorFalso;
  }> {
    const resultado = servicio.iniciar();
    await vi.waitUntil(() => servicio.estado() === 'grabando');
    const grabador = GrabadorFalso.instancias.at(-1);
    if (!grabador) {
      throw new Error('El servicio no creó el grabador');
    }
    return { resultado, grabador };
  }

  beforeEach(() => {
    microfono = microfonoFalso();
    getUserMedia = vi.fn().mockResolvedValue(microfono.flujo);
    GrabadorFalso.tiposSoportados = ['audio/webm;codecs=opus', 'audio/webm', 'audio/mp4'];
    GrabadorFalso.instancias = [];

    contextoSeguro(true);
    vi.stubGlobal('MediaRecorder', GrabadorFalso);
    Object.defineProperty(navigator, 'mediaDevices', {
      configurable: true,
      value: { getUserMedia },
    });

    TestBed.configureTestingModule({});
    servicio = TestBed.inject(GrabacionService);
  });

  afterEach(() => {
    vi.useRealTimers();
    vi.unstubAllGlobals();
    Reflect.deleteProperty(navigator, 'mediaDevices');
    Reflect.deleteProperty(window, 'isSecureContext');
  });

  describe('requisitos del navegador', () => {
    /** Verifica que fuera de HTTPS o localhost no se pida el micrófono. */
    it('exige un contexto seguro', async () => {
      contextoSeguro(false);

      await expect(servicio.iniciar()).rejects.toThrow('El micrófono requiere HTTPS o localhost.');
      expect(getUserMedia).not.toHaveBeenCalled();
    });

    /** Verifica el mensaje cuando el navegador no tiene `MediaRecorder`. */
    it('avisa si el navegador no puede grabar', async () => {
      Reflect.deleteProperty(globalThis, 'MediaRecorder');

      await expect(servicio.iniciar()).rejects.toThrow('Este navegador no permite grabar audio.');
    });

    /** Verifica el mensaje cuando el navegador no expone `getUserMedia`. */
    it('avisa si el navegador no da acceso al micrófono', async () => {
      Reflect.deleteProperty(navigator, 'mediaDevices');

      await expect(servicio.iniciar()).rejects.toThrow('Este navegador no permite grabar audio.');
    });

    /** Verifica el mensaje cuando el navegador no admite ninguno de los formatos de audio. */
    it('exige un formato de audio compatible', async () => {
      GrabadorFalso.tiposSoportados = ['audio/ogg'];

      await expect(servicio.iniciar()).rejects.toThrow(
        'El navegador no admite WebM o MP4 para esta grabación.',
      );
      expect(servicio.estado()).toBe('inactivo');
    });
  });

  describe('grabación', () => {
    /**
     * Verifica que mientras el usuario responde el permiso el estado sea «solicitando» y que
     * luego empiece a grabar con el formato preferido, en fragmentos de un segundo.
     */
    it('pide el micrófono y empieza a grabar', async () => {
      const resultado = servicio.iniciar();
      expect(servicio.estado()).toBe('solicitando');

      await vi.waitUntil(() => servicio.estado() === 'grabando');
      const [grabador] = GrabadorFalso.instancias;

      expect(getUserMedia).toHaveBeenCalledWith({ audio: true });
      expect(grabador.mimeType).toBe('audio/webm;codecs=opus');
      expect(grabador.start).toHaveBeenCalledWith(1000);

      servicio.cancelar();
      await resultado;
    });

    /** Verifica que no se pueda iniciar una segunda grabación mientras hay una en curso. */
    it('no permite dos grabaciones a la vez', async () => {
      const { resultado } = await empezarGrabacion();

      await expect(servicio.iniciar()).rejects.toThrow('Ya hay una grabación en curso.');

      servicio.cancelar();
      await resultado;
    });

    /**
     * Verifica que al detener se entregue un único archivo WebM con todos los fragmentos y que
     * el micrófono quede libre.
     */
    it('al detener entrega el audio completo y libera el micrófono', async () => {
      const { resultado, grabador } = await empezarGrabacion();
      grabador.entregar('hola ');
      grabador.entregar('mundo');

      servicio.detener();
      const archivo = await resultado;

      expect(archivo).toBeInstanceOf(File);
      expect(archivo?.name).toBe('grabacion.webm');
      expect(archivo?.type).toBe('audio/webm');
      expect(archivo?.size).toBe(10);
      expect(microfono.pista.stop).toHaveBeenCalled();
      expect(servicio.estado()).toBe('inactivo');
    });

    /** Verifica el nombre y el tipo del archivo cuando el navegador solo graba en MP4. */
    it('genera un archivo MP4 en navegadores que no graban WebM', async () => {
      GrabadorFalso.tiposSoportados = ['audio/mp4'];
      const { resultado, grabador } = await empezarGrabacion();
      grabador.entregar('audio');

      servicio.detener();
      const archivo = await resultado;

      expect(archivo?.name).toBe('grabacion.mp4');
      expect(archivo?.type).toBe('audio/mp4');
    });

    /** Verifica que los fragmentos vacíos se ignoren y que una grabación sin audio sea un error. */
    it('rechaza una grabación sin audio', async () => {
      const { resultado, grabador } = await empezarGrabacion();
      grabador.entregar();

      servicio.detener();

      await expect(resultado).rejects.toThrow('La grabación no contiene audio.');
      expect(microfono.pista.stop).toHaveBeenCalled();
    });

    /** Verifica que detener sin una grabación activa no haga nada. */
    it('ignora detener cuando no hay nada grabando', () => {
      servicio.detener();

      expect(servicio.estado()).toBe('inactivo');
    });
  });

  describe('límites', () => {
    /**
     * Verifica que al pasar de 10 MB la grabación se corte con un error y se libere el micrófono.
     */
    it('corta la grabación si el audio supera los 10 MB', async () => {
      const { resultado, grabador } = await empezarGrabacion();
      grabador.entregar(new Uint8Array(MAX_ARCHIVO_BYTES));

      grabador.entregar(new Uint8Array(1));

      await expect(resultado).rejects.toThrow('El audio supera el máximo de 10 MB.');
      expect(grabador.stop).toHaveBeenCalled();
      expect(microfono.pista.stop).toHaveBeenCalled();
    });

    /** Verifica que la grabación se detenga sola a los dos minutos y entregue el audio. */
    it('se detiene sola a los dos minutos', async () => {
      vi.useFakeTimers();
      const { resultado, grabador } = await empezarGrabacion();
      grabador.entregar('nota larga');

      vi.advanceTimersByTime(119_000);
      expect(servicio.estado()).toBe('grabando');

      vi.advanceTimersByTime(1_000);

      await expect(resultado).resolves.toBeInstanceOf(File);
      expect(servicio.estado()).toBe('inactivo');
    });
  });

  describe('cancelación y errores', () => {
    /** Verifica que cancelar descarte el audio, resuelva con `null` y libere el micrófono. */
    it('cancelar descarta el audio y libera el micrófono', async () => {
      const { resultado, grabador } = await empezarGrabacion();
      grabador.entregar('esto no se envía');

      servicio.cancelar();

      await expect(resultado).resolves.toBeNull();
      expect(microfono.pista.stop).toHaveBeenCalled();
      expect(servicio.estado()).toBe('inactivo');
    });

    /** Verifica que si el usuario niega el permiso el error llegue a quien inició la grabación. */
    it('propaga el rechazo del permiso del micrófono', async () => {
      const denegado = new DOMException('Permiso denegado', 'NotAllowedError');
      getUserMedia.mockRejectedValue(denegado);

      await expect(servicio.iniciar()).rejects.toBe(denegado);
      expect(servicio.estado()).toBe('inactivo');
    });

    /**
     * Verifica que un permiso concedido después de cancelar no deje el micrófono encendido ni
     * cree un grabador.
     */
    it('apaga un permiso que llega tarde después de cancelar', async () => {
      let concederPermiso: (flujo: MediaStream) => void = () => undefined;
      getUserMedia.mockReturnValue(new Promise((resolver) => (concederPermiso = resolver)));
      const resultado = servicio.iniciar();

      servicio.cancelar();
      await expect(resultado).resolves.toBeNull();

      concederPermiso(microfono.flujo);
      await vi.waitUntil(() => microfono.pista.stop.mock.calls.length > 0);

      expect(GrabadorFalso.instancias).toHaveLength(0);
      expect(servicio.estado()).toBe('inactivo');
    });

    /** Verifica el mensaje cuando el navegador interrumpe la captura del micrófono. */
    it('informa si se interrumpe la captura', async () => {
      const { resultado, grabador } = await empezarGrabacion();

      grabador.onerror?.();

      await expect(resultado).rejects.toThrow('Se interrumpió la captura del micrófono.');
      expect(microfono.pista.stop).toHaveBeenCalled();
    });

    /**
     * Verifica que al destruir el contexto del servicio, por ejemplo al salir de la página, se
     * suelte el micrófono.
     */
    it('libera el micrófono cuando se destruye el servicio', async () => {
      const { resultado } = await empezarGrabacion();

      TestBed.resetTestingModule();

      await expect(resultado).resolves.toBeNull();
      expect(microfono.pista.stop).toHaveBeenCalled();
    });
  });
});
