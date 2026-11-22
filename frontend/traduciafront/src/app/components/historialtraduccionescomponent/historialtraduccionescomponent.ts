import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import type {
  SolicitudTraduccionResponseDTO,
  TraduccionGuardada,
} from '../../models/traduccionmodel';
import { TraduccionService } from '../../services/traduccionservice';

type FiltroHistorial = 'todos' | TraduccionGuardada['tipo'];

/** Datos que necesita el historial para mostrar y abrir una traducción. */
interface EntradaHistorial extends TraduccionGuardada {
  readonly fecha: string;
  readonly origen: string;
  readonly destino: string;
  readonly original: string;
  readonly resultado: string;
}

const FORMATO_FECHA = new Intl.DateTimeFormat('es-CO', {
  day: 'numeric',
  month: 'short',
  year: 'numeric',
});

const LONGITUD_MAXIMA_TITULO = 60;
const TAMANO_PAGINA = 10;

/** Adapta el DTO del backend al modelo visual que usa la lista del historial. */
function adaptarEntrada(solicitud: SolicitudTraduccionResponseDTO): EntradaHistorial {
  const textoLimpio = solicitud.textoOriginal.trim();
  const titulo =
    textoLimpio.length > LONGITUD_MAXIMA_TITULO
      ? `${textoLimpio.slice(0, LONGITUD_MAXIMA_TITULO)}...`
      : textoLimpio;

  const traduccionExitosa = solicitud.listaTraduccion.find(
    (item) => item.estado === 'EXITOSA' && item.textoTraducido !== null,
  );

  return {
    id: String(solicitud.id),
    titulo,
    tipo: 'texto',
    fecha: solicitud.fechaCreacion,
    origen: solicitud.origen,
    destino: solicitud.destino,
    original: solicitud.textoOriginal,
    resultado: traduccionExitosa?.textoTraducido ?? 'Sin resultado disponible',
  };
}

/** Formatea una fecha ISO a formato legible en español de Colombia. */
function formatearFecha(fechaIso: string): string {
  const fecha = new Date(fechaIso);
  return Number.isNaN(fecha.getTime()) ? fechaIso : FORMATO_FECHA.format(fecha);
}

/**
 * Muestra las traducciones por fecha y permite filtrarlas sin salir
 * del panel de usuario.
 */
@Component({
  selector: 'app-historialtraduccionescomponent',
  standalone: false,
  templateUrl: './historialtraduccionescomponent.html',
  styleUrl: './historialtraduccionescomponent.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Historialtraduccionescomponent implements OnInit {
  private readonly traduccionService = inject(TraduccionService);
  private readonly destroyRef = inject(DestroyRef);

  private readonly traducciones = signal<readonly EntradaHistorial[]>([]);
  private readonly busqueda = signal('');
  private readonly filtro = signal<FiltroHistorial>('todos');

  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly paginaActual = signal(0);
  protected readonly totalPaginas = signal(0);
  protected readonly totalElementos = signal(0);

  protected readonly cargandoDetalleId = signal<string | null>(null);
  protected readonly detallesPorId = signal<Readonly<Record<string, SolicitudTraduccionResponseDTO>>>(
    {},
  );

  /** Indica si hay registros antes de aplicar la búsqueda o el filtro. */
  protected readonly tieneTraducciones = computed(() => this.traducciones().length > 0);

  /** Indica si se puede retroceder a una página anterior. */
  protected readonly puedeRetroceder = computed(() => this.paginaActual() > 0);

  /** Indica si existen páginas posteriores por consultar. */
  protected readonly puedeAvanzar = computed(
    () => this.paginaActual() + 1 < this.totalPaginas(),
  );

  /** Traducciones que coinciden con los filtros, de la más reciente a la más antigua. */
  protected readonly traduccionesVisibles = computed(() => {
    const consulta = this.busqueda().trim().toLocaleLowerCase('es');
    const filtro = this.filtro();

    return this.traducciones()
      .filter((traduccion) => {
        const coincideTipo = filtro === 'todos' || traduccion.tipo === filtro;
        const coincideTexto =
          traduccion.titulo.toLocaleLowerCase('es').includes(consulta) ||
          traduccion.original.toLocaleLowerCase('es').includes(consulta);

        return coincideTipo && coincideTexto;
      })
      .sort((primera, segunda) => Date.parse(segunda.fecha) - Date.parse(primera.fecha))
      .map((traduccion) => ({
        ...traduccion,
        fechaVisible: formatearFecha(traduccion.fecha),
      }));
  });

  /** Carga la primera página del historial al inicializar el componente. */
  ngOnInit(): void {
    this.cargarHistorial(0);
  }

  /** Consulta al backend la página indicada del historial del usuario. */
  protected cargarHistorial(pagina = 0): void {
    this.cargando.set(true);
    this.error.set(null);
    this.cargandoDetalleId.set(null);

    this.traduccionService
      .obtenerHistorial(pagina, TAMANO_PAGINA)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (respuesta) => {
          this.traducciones.set(respuesta.content.map(adaptarEntrada));
          this.paginaActual.set(respuesta.number);
          this.totalPaginas.set(respuesta.totalPages);
          this.totalElementos.set(respuesta.totalElements);
          this.cargando.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar el historial de traducciones.');
          this.cargando.set(false);
        },
      });
  }

  /** Consulta el detalle por proveedor cuando se despliega un elemento del historial. */
  protected alAlternarDetalle(evento: Event, id: string): void {
    if (!(evento.target instanceof HTMLDetailsElement) || !evento.target.open) {
      return;
    }

    if (this.detallesPorId()[id]) {
      return;
    }

    const idNumerico = Number(id);
    if (Number.isNaN(idNumerico)) {
      return;
    }

    this.cargandoDetalleId.set(id);

    this.traduccionService
      .obtenerPorId(idNumerico)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (detalle) => {
          this.detallesPorId.update((actual) => ({ ...actual, [id]: detalle }));
          this.cargandoDetalleId.set(null);
        },
        error: () => {
          this.cargandoDetalleId.set(null);
        },
      });
  }

  /** Navega a la página anterior del historial. */
  protected paginaAnterior(): void {
    if (this.puedeRetroceder()) {
      this.cargarHistorial(this.paginaActual() - 1);
    }
  }

  /** Navega a la página siguiente del historial. */
  protected paginaSiguiente(): void {
    if (this.puedeAvanzar()) {
      this.cargarHistorial(this.paginaActual() + 1);
    }
  }

  /** Actualiza la búsqueda usando el título o el texto original de la traducción. */
  protected actualizarBusqueda(valor: string): void {
    this.busqueda.set(valor);
  }

  /** Aplica uno de los tres filtros disponibles en la plantilla. */
  protected actualizarFiltro(valor: string): void {
    if (valor === 'todos' || valor === 'texto' || valor === 'archivo') {
      this.filtro.set(valor);
    }
  }
}
