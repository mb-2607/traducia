import { ActivatedRoute } from '@angular/router';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';

import type { TraduccionGuardada } from '../../models/traduccionmodel';

type FiltroBiblioteca = 'todos' | TraduccionGuardada['tipo'];

/** Datos que necesita la vista para mostrar el contenido de una traducción guardada. */
interface EntradaBiblioteca extends TraduccionGuardada {
  readonly resultado: string;
}

const TITULOS: Readonly<Record<FiltroBiblioteca, string>> = {
  todos: 'Biblioteca de traducciones',
  archivo: 'Archivos traducidos',
  texto: 'Textos traducidos',
};

/**
 * Muestra las traducciones guardadas en el área de usuario.
 *
 * Lee el tipo desde la configuración de la ruta y permite buscar por título.
 * Distingue entre una biblioteca vacía y una búsqueda sin coincidencias.
 */
@Component({
  selector: 'app-bibliotecatraduccionescomponent',
  standalone: false,
  templateUrl: './bibliotecatraduccionescomponent.html',
  styleUrl: './bibliotecatraduccionescomponent.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Bibliotecatraduccionescomponent {
  private readonly ruta = inject(ActivatedRoute);
  private readonly filtro = this.obtenerFiltro();
  private readonly consulta = signal('');

  /** Registros que se muestran en la biblioteca. */
  private readonly traducciones = signal<readonly EntradaBiblioteca[]>([]);
  /** Título correspondiente a la vista seleccionada. */
  protected readonly titulo = TITULOS[this.filtro];

  /** Registros que pertenecen al tipo configurado en la ruta. */
  private readonly traduccionesPorTipo = computed(() => {
    if (this.filtro === 'todos') {
      return this.traducciones();
    }

    return this.traducciones().filter((traduccion) => traduccion.tipo === this.filtro);
  });

  /** Indica si hay registros del tipo elegido antes de filtrar por título. */
  protected readonly tieneTraducciones = computed(() => this.traduccionesPorTipo().length > 0);

  /** Traducciones cuyo título coincide con la búsqueda actual. */
  protected readonly traduccionesVisibles = computed(() => {
    const consulta = this.consulta().trim().toLocaleLowerCase('es');

    return this.traduccionesPorTipo().filter((traduccion) =>
      traduccion.titulo.toLocaleLowerCase('es').includes(consulta),
    );
  });

  /**
   * Actualiza el texto usado para filtrar títulos.
   *
   * @param valor Contenido actual del campo de búsqueda.
   */
  protected actualizarBusqueda(valor: string): void {
    this.consulta.set(valor);
  }

  /**
   * Lee el filtro configurado para la ruta actual.
   *
   * @returns El tipo indicado por la ruta o `todos` para la vista general.
   */
  private obtenerFiltro(): FiltroBiblioteca {
    const tipo: unknown = this.ruta.snapshot.data['tipo'];

    if (tipo === 'archivo' || tipo === 'texto') {
      return tipo;
    }

    return 'todos';
  }
}
