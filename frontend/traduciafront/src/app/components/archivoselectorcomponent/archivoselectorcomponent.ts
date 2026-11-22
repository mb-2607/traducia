/**
 * Componente reutilizable encargado de gestionar la selección de archivos.
 *
 * Permite seleccionar archivos desde un elemento input de tipo file,
 * validar que no estén vacíos y comprobar que no superen el tamaño
 * máximo permitido de 5 MB.
 *
 * Comunica el resultado de la selección al componente padre mediante
 * eventos de salida, diferenciando entre archivos aceptados y rechazados.
 */
import {Component, EventEmitter, Input, Output} from '@angular/core';

/**
 * Tamaño máximo permitido para los archivos seleccionados, expresado en bytes.
 */
const TAMANO_MAXIMO_BYTES = 5 * 1024 * 1024;

@Component({
  selector: 'app-archivoselectorcomponent',
  standalone: false,
  styleUrl: './archivoselectorcomponent.css',
  templateUrl: './archivoselectorcomponent.html',
})
export class Archivoselectorcomponent {

  /** Indica si el selector de archivos debe permanecer deshabilitado. */
  @Input() deshabilitado = false;

  /** Emite el archivo seleccionado cuando supera las validaciones establecidas. */
  @Output() readonly archivoSeleccionado :EventEmitter<File> = new EventEmitter<File>();

  /** Emite un mensaje descriptivo cuando el archivo seleccionado no es válido. */
  @Output() readonly archivoRechazado :EventEmitter<string> = new EventEmitter<string>();

  /** Extensiones de archivo permitidas en el selector. */
  readonly tiposPermitidos = '.pdf,.png,.jpg,.jpeg,docx';

  /**
   * Obtiene el archivo seleccionado y valida que no esté vacío
   * ni supere el tamaño máximo permitido.
   *
   * Limpia el valor del input para permitir seleccionar nuevamente
   * el mismo archivo y emite el evento correspondiente según el resultado.
   *
   * @param input Elemento HTMLInputElement desde el cual se obtiene el archivo.
   */
  seleccionar(input: HTMLInputElement): void {
    const archivo = input.files?.item(0);
    input.value = '';

    if (!archivo) {
      return;
    }

    if (archivo.size === 0) {
      this.archivoRechazado.emit('El archivo está vacío.');
      return;
    }

    if (archivo.size > TAMANO_MAXIMO_BYTES) {
      this.archivoRechazado.emit('El archivo supera el máximo de 5 MB.');
      return;
    }

    this.archivoSeleccionado.emit(archivo);
  }
}
