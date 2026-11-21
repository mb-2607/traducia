import { DestroyRef, Injectable, inject, signal } from '@angular/core';
import { MAX_ARCHIVO_BYTES } from '../api-config';

/** Estados locales del dispositivo de grabación; no son estados del backend. */
type EstadoGrabacion = 'inactivo' | 'solicitando' | 'grabando' | 'deteniendo';

/** Límite de cada nota de voz para acotar duración y uso de memoria. */
const MAX_GRABACION_MS = 120_000;
const TIPOS_AUDIO = ['audio/webm;codecs=opus', 'audio/webm', 'audio/mp4'];

/**
 * Graba un único audio completo y libera el micrófono al finalizar o cancelar.
 * Se proporciona en el componente para limitar su ciclo de vida a la página.
 * No sube fragmentos individuales ni realiza reconocimiento de voz local.
 */
@Injectable()
export class GrabacionService {
  private readonly destroyRef = inject(DestroyRef);
  private readonly estadoInterno = signal<EstadoGrabacion>('inactivo');
  readonly estado = this.estadoInterno.asReadonly();
  private flujo: MediaStream | null = null;
  private grabador: MediaRecorder | null = null;
  private temporizador: ReturnType<typeof setTimeout> | undefined;
  private intento = 0;
  private resolver: ((archivo: File | null) => void) | null = null;
  private rechazar: ((error: unknown) => void) | null = null;

  constructor() {
    this.destroyRef.onDestroy(() => this.cancelar());
  }

  /** Resuelve al detener con un archivo completo, o con null al cancelar. */
  iniciar(): Promise<File | null> {
    if (this.estado() !== 'inactivo') {
      return Promise.reject(new Error('Ya hay una grabación en curso.'));
    }
    if (typeof window === 'undefined' || !window.isSecureContext) {
      return Promise.reject(new Error('El micrófono requiere HTTPS o localhost.'));
    }
    if (typeof MediaRecorder === 'undefined' || !navigator.mediaDevices?.getUserMedia) {
      return Promise.reject(new Error('Este navegador no permite grabar audio.'));
    }
    const mimeType = TIPOS_AUDIO.find((tipo) => MediaRecorder.isTypeSupported(tipo));
    if (!mimeType) {
      return Promise.reject(new Error('El navegador no admite WebM o MP4 para esta grabación.'));
    }
    this.estadoInterno.set('solicitando');
    const intento = ++this.intento;
    return new Promise<File | null>((resolve, reject) => {
      this.resolver = resolve;
      this.rechazar = reject;
      void this.preparar(intento, mimeType).catch((error: unknown) => {
        if (intento === this.intento) {
          this.finalizar(null, error);
        }
      });
    });
  }

  /** Solicita el último fragmento; el envío se realiza después del evento stop. */
  detener(): void {
    if (this.grabador?.state === 'recording') {
      this.estadoInterno.set('deteniendo');
      this.grabador.stop();
    }
  }

  /** Descarta la grabación y también invalida permisos pendientes de respuesta. */
  cancelar(): void {
    this.finalizar(null);
  }

  /** No conserva un permiso tardío si el usuario salió o canceló la solicitud. */
  private async preparar(intento: number, mimeType: string): Promise<void> {
    const flujo = await navigator.mediaDevices.getUserMedia({ audio: true });
    if (intento !== this.intento || this.destroyRef.destroyed) {
      flujo.getTracks().forEach((pista) => pista.stop());
      return;
    }
    this.flujo = flujo;
    const grabador = new MediaRecorder(flujo, { mimeType });
    this.grabador = grabador;
    const fragmentos: Blob[] = [];
    let bytes = 0;
    grabador.ondataavailable = (evento: BlobEvent) => {
      if (this.grabador !== grabador || evento.data.size === 0) {
        return;
      }
      bytes += evento.data.size;
      if (bytes > MAX_ARCHIVO_BYTES) {
        this.finalizar(null, new Error('El audio supera el máximo de 10 MB.'));
        return;
      }
      fragmentos.push(evento.data);
    };
    grabador.onerror = () => {
      this.finalizar(null, new Error('Se interrumpió la captura del micrófono.'));
    };
    grabador.onstop = () => {
      if (this.grabador !== grabador) {
        return;
      }
      if (bytes === 0) {
        this.finalizar(null, new Error('La grabación no contiene audio.'));
        return;
      }
      const tipo = grabador.mimeType || mimeType;
      const extension = tipo.startsWith('audio/mp4') ? 'mp4' : 'webm';
      const archivo = new File(fragmentos, `grabacion.${extension}`, {
        type: tipo.split(';')[0],
      });
      this.finalizar(archivo);
    };
    grabador.start(1000);
    this.estadoInterno.set('grabando');
    this.temporizador = setTimeout(() => this.detener(), MAX_GRABACION_MS);
  }

  /** Desconecta manejadores antes de detener y resuelve cada promesa una sola vez. */
  private finalizar(archivo: File | null, error?: unknown): void {
    const resolver = this.resolver;
    const rechazar = this.rechazar;
    this.resolver = null;
    this.rechazar = null;
    this.intento++;
    clearTimeout(this.temporizador);
    this.temporizador = undefined;
    const grabador = this.grabador;
    this.grabador = null;
    if (grabador) {
      grabador.ondataavailable = null;
      grabador.onerror = null;
      grabador.onstop = null;
      if (grabador.state !== 'inactive') {
        grabador.stop();
      }
    }
    this.flujo?.getTracks().forEach((pista) => pista.stop());
    this.flujo = null;
    this.estadoInterno.set('inactivo');
    if (error !== undefined) {
      rechazar?.(error);
    } else {
      resolver?.(archivo);
    }
  }
}
