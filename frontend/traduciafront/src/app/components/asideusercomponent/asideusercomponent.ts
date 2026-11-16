import {ChangeDetectorRef, Component, DestroyRef, inject, OnInit} from '@angular/core';
import {DOCUMENT} from '@angular/common';
import {TemaService} from '../../services/temaservice';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';

/**
 * Componente encargado de gestionar el panel lateral de navegación del área de usuario.
 *
 * Permite controlar la visibilidad del panel según el tamaño de la pantalla y gestionar el cambio entre
 * los temas claro y oscuro mediante TemaService.
 *
 * Se suscribe a los cambios del tema para mantener sincronizada la interfaz y utiliza takeUntilDestroyed para liberar
 * automáticamente la suscripción al destruirse el componente.
 */
@Component({
  selector: 'app-asideusercomponent',
  standalone: false,
  styleUrl: './asideusercomponent.css',
  templateUrl: './asideusercomponent.html',
  host: { '[class.plegado]': '!panelAbierto' },
})
export class AsideUserComponent implements OnInit {

  /** Referencia al mecanismo de detección de cambios de Angular. */
  private cdr: ChangeDetectorRef = inject(ChangeDetectorRef);

  /** Permite gestionar la destrucción del componente y sus suscripciones. */
  private destroyRef = inject(DestroyRef);

  /** Servicio encargado de consultar y modificar el tema de la aplicación. */
  temaService: TemaService = inject(TemaService);

  /** Referencia al documento actual para consultar las características de la ventana. */
  private readonly documento: Document = inject(DOCUMENT);

  /**
   * Indica si el panel lateral está abierto.
   * Inicialmente, permanece cerrado en pantallas móviles y abierto en las demás.
   */
  panelAbierto= !(this.documento.defaultView?.matchMedia?.('(max-width: 44rem)').matches ?? false);

  /** Indica si actualmente se encuentra activo el tema oscuro. */
  temaOscuro = false;

  /**
   * Inicializa la suscripción a los cambios del tema mediante TemaService.
   * Actualiza el estado local cada vez que se recibe una modificación.
   * La suscripción finaliza automáticamente al destruirse el componente.
   */
  ngOnInit() {
    this.temaService.getTemaOscuroAsObservable().pipe(takeUntilDestroyed(this.destroyRef)).subscribe(
      (data: boolean) => {
        this.temaOscuro = data;
      }
    )
  }

  /**
   * Alterna entre el tema claro y oscuro utilizando TemaService.
   * Marca el componente para una nueva comprobación de cambios.
   */
  alternarTema(): void {
    this.temaService.setTemaOscuro(!this.temaOscuro);
    this.cdr.markForCheck();
  }
}
