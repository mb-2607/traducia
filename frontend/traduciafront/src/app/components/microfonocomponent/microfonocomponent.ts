import {Component, EventEmitter, inject, Input, Output} from '@angular/core';
import {DOCUMENT} from '@angular/common';

@Component({
  selector: 'app-microfonocomponent',
  standalone: false,
  styleUrl: './microfonocomponent.css',
  templateUrl: './microfonocomponent.html',
})
export class Microfonocomponent {
  @Input() deshabilitado = false;
  @Input() voz: string | null = null;

  @Output() readonly textoDictado = new EventEmitter<string>();
  @Output() readonly escuchandoChange = new EventEmitter<boolean>();
  @Output() readonly errorDictado = new EventEmitter<string>();

  private readonly documento: Document = inject(DOCUMENT);
  private readonly constructorVoz: ConstructorVoz | null = this.obtenerConstructor();
  private reconocimiento: ReconocimientoVoz | null = null;

  escuchando = false;

  get disponible(): boolean {
    return this.constructorVoz !== null;
  }

  get titulo(): string {
    if (!this.disponible) {
      return 'Tu navegador no admite el dictado por voz';
    }
    return this.escuchando ? 'Detener dictado' : 'Iniciar dictado';
  }

  alternar(): void {
    if (this.reconocimiento) {
      this.reconocimiento.stop();
      return;
    }

    if (!this.constructorVoz || !this.voz) {
      return;
    }

    const reconocimiento = new this.constructorVoz();
    reconocimiento.lang = this.voz;
    reconocimiento.continuous = false;
    reconocimiento.interimResults = false;

    reconocimiento.onresult = (evento) => {
      if (this.reconocimiento !== reconocimiento) {
        return;
      }

      const dictado = Array.from(evento.results)
        .slice(evento.resultIndex)
        .map((resultado) => resultado[0]?.transcript.trim() ?? '')
        .filter(Boolean)
        .join(' ');

      if (dictado) {
        this.textoDictado.emit(dictado);
      }
    };

    reconocimiento.onerror = (evento) => {
      if (this.reconocimiento !== reconocimiento) {
        return;
      }

      switch (evento.error) {
        case 'not-allowed':
          this.errorDictado.emit('El navegador bloqueó el acceso al micrófono.');
          break;

        case 'no-speech':
          this.errorDictado.emit('No se detectó voz. Pulsa el micrófono para intentarlo de nuevo.');
          break;

        default:
          this.errorDictado.emit('No se pudo completar el dictado. Revisa el micrófono e inténtalo de nuevo.');
      }
    };

    reconocimiento.onend = () => {
      if (this.reconocimiento === reconocimiento) {
        this.reconocimiento = null;
        this.cambiarEstado(false);
      }
    };

    this.reconocimiento = reconocimiento;

    try {
      reconocimiento.start();
      this.cambiarEstado(true);
    } catch {
      this.reconocimiento = null;
      this.errorDictado.emit('No se pudo iniciar el dictado.');
    }
  }

  ngOnDestroy(): void {
    // Sin emitir: el padre también se está destruyendo
    const reconocimiento = this.reconocimiento;
    this.reconocimiento = null;
    this.escuchando = false;
    reconocimiento?.abort();
  }

  private cambiarEstado(valor: boolean): void {
    if (this.escuchando === valor) {
      return;
    }
    this.escuchando = valor;
    this.escuchandoChange.emit(valor);
  }

  private obtenerConstructor(): ConstructorVoz | null {
    const ventana = this.documento.defaultView as VentanaConDictado | null;
    return ventana?.SpeechRecognition ?? ventana?.webkitSpeechRecognition ?? null;
  }
}

