import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  EventEmitter,
  Output,
  ViewChild,
  inject,
  signal,
} from '@angular/core';
import type { AfterViewInit } from '@angular/core';
import { TerminosService } from '../../services/terminosservice';

/**
 * Presenta los términos antes de permitir el registro o el acceso.
 *
 * La aceptación se administra mediante TerminosService.
 * Rechazar o presionar Escape mantiene bloqueado el acceso.
 */
@Component({
  selector: 'app-terminocomponent',
  standalone: false,
  templateUrl: './terminocomponent.html',
  styleUrl: './terminocomponent.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Terminocomponent implements AfterViewInit {
  private readonly terminos = inject(TerminosService);

  @ViewChild('dialogo')
  private dialogo?: ElementRef<HTMLDialogElement>;

  @Output()
  readonly terminosAceptadosChange = new EventEmitter<boolean>();

  readonly error = signal(false);

  /** Abre el diálogo cuando su elemento ya está disponible. */
  ngAfterViewInit(): void {
    this.abrir();
  }

  /** Permite mostrar nuevamente el modal si todavía no se aceptaron los términos. */
  abrir(): void {
    if (this.terminos.aceptados()) {
      return;
    }

    const dialogo = this.dialogo?.nativeElement;

    if (dialogo && !dialogo.open && typeof dialogo.showModal === 'function') {
      dialogo.showModal();
    }
  }

  /** Solo una aceptación explícita habilita los formularios. */
  aceptar(): void {
    if (!this.terminos.aceptados()) {
      this.terminos.aceptar();
    }

    this.error.set(false);
    this.dialogo?.nativeElement.close();
    this.terminosAceptadosChange.emit(true);
  }

  /** Evita cerrar el diálogo sin aceptar las condiciones. */
  rechazar(evento?: Event): void {
    evento?.preventDefault();

    this.error.set(true);
    this.terminosAceptadosChange.emit(false);
  }
}
