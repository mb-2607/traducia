import { ChangeDetectionStrategy, Component, computed, signal } from '@angular/core';
import type { TraduccionGuardada } from '../../models/traduccionmodel';

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
export class Historialtraduccionescomponent {
  private readonly traducciones = signal<readonly EntradaHistorial[]>([]);
  private readonly busqueda = signal('');
  private readonly filtro = signal<FiltroHistorial>('todos');

  /** Indica si hay registros antes de aplicar la búsqueda o el filtro. */
  protected readonly tieneTraducciones = computed(() => this.traducciones().length > 0);

  /** Traducciones que coinciden con los filtros, de la más reciente a la más antigua. */
  protected readonly traduccionesVisibles = computed(() => {
    const consulta = this.busqueda().trim().toLocaleLowerCase('es');
    const filtro = this.filtro();

    return this.traducciones()
      .filter((traduccion) => {
        const coincideTipo = filtro === 'todos' || traduccion.tipo === filtro;
        const coincideTitulo = traduccion.titulo.toLocaleLowerCase('es').includes(consulta);

        return coincideTipo && coincideTitulo;
      })
      .sort((primera, segunda) => Date.parse(segunda.fecha) - Date.parse(primera.fecha))
      .map((traduccion) => ({
        ...traduccion,
        fechaVisible: FORMATO_FECHA.format(new Date(traduccion.fecha)),
      }));
  });

  /** Actualiza la búsqueda usando el título de la traducción. */
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
