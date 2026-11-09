import { DOCUMENT } from '@angular/common';
import {
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  Output,
  computed,
  inject,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PLANES } from '../../models/plan';
import type { Plan, PlanId } from '../../models/plan';
import { SuscripcionService } from '../../services/suscripcionservice';
type Vista = 'nueva' | 'archivos' | 'texto' | 'historial' | 'buscar';
const IDIOMAS = [
  { id: 'es', nombre: 'Español', voz: 'es-CO' },
  { id: 'en', nombre: 'Inglés', voz: 'en-US' },
  { id: 'fr', nombre: 'Francés', voz: 'fr-FR' },
  { id: 'pt', nombre: 'Portugués', voz: 'pt-BR' },
  { id: 'de', nombre: 'Alemán', voz: 'de-DE' },
  { id: 'it', nombre: 'Italiano', voz: 'it-IT' },
] as const;
type IdiomaId = (typeof IDIOMAS)[number]['id'];
const TITULOS: Record<Vista, string> = {
  nueva: 'Traductor',
  archivos: 'Archivos traducidos',
  texto: 'Textos traducidos',
  historial: 'Historial de traducciones',
  buscar: 'Buscar traducciones',
};

export interface TraduccionGuardada {
  id: string;
  titulo: string;
  tipo: 'texto' | 'archivo';
}

export type SolicitudTraduccion = {
  origen: IdiomaId;
  destino: IdiomaId;
  plan: PlanId;
  cantidadIas: number;
} & ({ tipo: 'texto'; texto: string } | { tipo: 'archivo'; archivo: File });

interface EventoDictado extends Event {
  resultIndex: number;
  results: ArrayLike<ArrayLike<{ transcript: string }>>;
}

interface ReconocimientoVoz {
  lang: string;
  continuous: boolean;
  interimResults: boolean;
  onresult: ((evento: EventoDictado) => void) | null;
  onerror: ((evento: Event & { error: string }) => void) | null;
  onend: (() => void) | null;
  start(): void;
  stop(): void;
  abort(): void;
}

type ConstructorVoz = new () => ReconocimientoVoz;

type VentanaConDictado = Window & {
  SpeechRecognition?: ConstructorVoz;
  webkitSpeechRecognition?: ConstructorVoz;
};

@Component({
  selector: 'app-paginaprincipalcomponent',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './paginaprincipalcomponent.html',
  styleUrls: ['./paginaprincipalcomponent.css'],
})
export class Paginaprincipalcomponent implements OnDestroy {
  @Input() plan?: PlanId;
  @Input() archivosUsadosHoy = 0;
  @Input() traducciones: readonly TraduccionGuardada[] = [];
  @Input() procesando = false;
  @Output() readonly solicitudTraduccion = new EventEmitter<SolicitudTraduccion>();
  @Output() readonly abrirTraduccion = new EventEmitter<string>();

  private readonly documento = inject(DOCUMENT);
  private readonly suscripcion = inject(SuscripcionService);
  private reconocimiento: ReconocimientoVoz | null = null;
  readonly idiomas = IDIOMAS;
  readonly texto = signal('');
  readonly escuchando = signal(false);
  readonly errorDictado = signal('');
  readonly cantidadPalabras = computed(() => {
    const palabras = this.texto()
      .normalize()
      .match(/[\p{L}\p{N}]+(?:[’'-][\p{L}\p{N}]+)*/gu);

    return palabras?.length ?? 0;
  });

  vista: Vista = 'nueva';
  panelAbierto = !(this.documento.defaultView?.matchMedia?.('(max-width: 44rem)').matches ?? false);
  temaOscuro = this.documento.documentElement.dataset['tema'] !== 'claro';
  origen: IdiomaId = 'es';
  destino: IdiomaId = 'en';
  archivo: File | null = null;
  busqueda = '';
  aviso = '';

  get planActual(): Plan {
    return PLANES[this.plan ?? this.suscripcion.planActivo()];
  }

  get tituloVista(): string {
    return TITULOS[this.vista];
  }

  get traduccionesVisibles(): readonly TraduccionGuardada[] {
    switch (this.vista) {
      case 'archivos':
        return this.traducciones.filter((item) => item.tipo === 'archivo');

      case 'texto':
        return this.traducciones.filter((item) => item.tipo === 'texto');

      case 'buscar': {
        const consulta = this.busqueda.trim().toLocaleLowerCase('es');

        return this.traducciones.filter((item) =>
          item.titulo.toLocaleLowerCase('es').includes(consulta),
        );
      }

      default:
        return this.traducciones;
    }
  }

  get errorValidacion(): string {
    const { palabras, archivosPorDia } = this.planActual;

    if (this.origen === this.destino) {
      return 'Elige idiomas de origen y destino diferentes.';
    }

    if (this.archivo) {
      if (this.archivo.size === 0) {
        return 'El archivo está vacío.';
      }

      if (archivosPorDia !== null && this.archivosUsadosHoy >= archivosPorDia) {
        return 'Alcanzaste el cupo diario de archivos de este plan.';
      }
    } else if (palabras !== null && this.cantidadPalabras() > palabras) {
      return `Este plan permite hasta ${palabras} palabras por traducción.`;
    }

    return '';
  }

  get puedeTraducir(): boolean {
    return (
      !this.procesando &&
      !this.escuchando() &&
      (this.archivo !== null || this.texto().trim().length > 0) &&
      !this.errorValidacion
    );
  }

  alternarTema(): void {
    this.temaOscuro = !this.temaOscuro;
    this.documento.documentElement.dataset['tema'] = this.temaOscuro ? 'oscuro' : 'claro';
  }

  nuevaTraduccion(): void {
    if (this.procesando) {
      this.vista = 'nueva';
      return;
    }

    this.cancelarDictado();
    this.texto.set('');
    this.errorDictado.set('');
    this.archivo = null;
    this.aviso = '';
    this.vista = 'nueva';
  }

  cambiarVista(vista: Vista): void {
    this.cancelarDictado();
    this.vista = vista;
  }

  seleccionarArchivo(input: HTMLInputElement): void {
    const seleccionado = input.files?.item(0);

    if (seleccionado) {
      this.archivo = seleccionado;
      this.aviso = '';
      this.errorDictado.set('');
    }

    input.value = '';
  }

  traducir(): void {
    if (!this.puedeTraducir) {
      return;
    }

    const plan = this.planActual;
    let solicitud: SolicitudTraduccion;

    if (this.archivo !== null) {
      solicitud = {
        tipo: 'archivo',
        archivo: this.archivo,
        origen: this.origen,
        destino: this.destino,
        plan: plan.id,
        cantidadIas: plan.ias,
      };
    } else {
      solicitud = {
        tipo: 'texto',
        texto: this.texto().trim(),
        origen: this.origen,
        destino: this.destino,
        plan: plan.id,
        cantidadIas: plan.ias,
      };
    }

    this.solicitudTraduccion.emit(solicitud);
    this.aviso = 'Solicitud validada. La traducción se procesará cuando conectes el servicio.';
  }

  alternarDictado(): void {
    if (this.reconocimiento) {
      this.reconocimiento.stop();
      return;
    }

    this.errorDictado.set('');
    this.aviso = '';

    if (typeof window === 'undefined') {
      return;
    }

    const navegador: VentanaConDictado = window;
    const Reconocimiento = navegador.SpeechRecognition ?? navegador.webkitSpeechRecognition;

    if (!Reconocimiento) {
      this.errorDictado.set('Este navegador no admite el dictado por voz.');
      return;
    }

    const reconocimiento = new Reconocimiento();
    reconocimiento.lang = IDIOMAS.find((idioma) => idioma.id === this.origen)?.voz ?? 'es-CO';
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
        this.texto.update((actual) => [actual.trimEnd(), dictado].filter(Boolean).join(' '));
      }
    };

    reconocimiento.onerror = (evento) => {
      if (this.reconocimiento !== reconocimiento) {
        return;
      }

      switch (evento.error) {
        case 'not-allowed':
          this.errorDictado.set('El navegador bloqueó el acceso al micrófono.');
          break;

        case 'no-speech':
          this.errorDictado.set('No se detectó voz. Pulsa el micrófono para intentarlo de nuevo.');
          break;

        default:
          this.errorDictado.set(
            'No se pudo completar el dictado. Revisa el micrófono e inténtalo de nuevo.',
          );
      }
    };

    reconocimiento.onend = () => {
      if (this.reconocimiento === reconocimiento) {
        this.reconocimiento = null;
        this.escuchando.set(false);
      }
    };

    this.reconocimiento = reconocimiento;

    try {
      reconocimiento.start();
      this.escuchando.set(true);
    } catch {
      this.reconocimiento = null;
      this.errorDictado.set('No se pudo iniciar el dictado.');
    }
  }

  ngOnDestroy(): void {
    this.cancelarDictado();
  }

  private cancelarDictado(): void {
    const reconocimiento = this.reconocimiento;
    this.reconocimiento = null;
    this.escuchando.set(false);
    reconocimiento?.abort();
  }
}
