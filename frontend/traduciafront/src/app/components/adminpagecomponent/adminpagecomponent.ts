import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/**
 * Página de inicio del área de administración de TraducIA.
 *
 * Reúne los accesos a la gestión de usuarios y al registro de auditoría.
 * El total de usuarios lo proporciona el componente o la integración
 * responsable de consultar ese dato.
 */
@Component({
  selector: 'app-adminpagecomponent',
  standalone: false,
  templateUrl: './adminpagecomponent.html',
  styleUrl: './adminpagecomponent.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Adminpagecomponent {
  /**
   * Total de usuarios registrados.
   *
   * `null` indica que el total todavía no está disponible.
   * `0` representa un total consultado sin usuarios registrados.
   */
  readonly totalUsuarios = input<number | null>(null);
}
