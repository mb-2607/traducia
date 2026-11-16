import {Component, EventEmitter, Input, Output} from '@angular/core';

const TAMANO_MAXIMO_BYTES = 5 * 1024 * 1024;

@Component({
  selector: 'app-archivoselectorcomponent',
  standalone: false,
  styleUrl: './archivoselectorcomponent.css',
  templateUrl: './archivoselectorcomponent.html',
})
export class Archivoselectorcomponent {
  @Input() deshabilitado = false;
  @Output() readonly archivoSeleccionado = new EventEmitter<File>();
  @Output() readonly archivoRechazado = new EventEmitter<string>();

  readonly tiposPermitidos = '.pdf,.png,.jpg,.jpeg,docx';

  seleccionar(input: HTMLInputElement): void {
    const archivo = input.files?.item(0);
    input.value = '';

    if (!archivo) {
      return;
    }

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
