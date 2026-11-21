import { Component, EventEmitter, Input, Output } from '@angular/core';
import { EXTENSIONES_DOCUMENTO, MAX_ARCHIVO_BYTES } from '../../api-config';

/** Selecciona documentos; el backend vuelve a validar formato real y tamaño. */
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
  readonly tiposPermitidos = EXTENSIONES_DOCUMENTO.map((tipo) => `.${tipo}`).join(',');

  /** Limpia el input para permitir volver a seleccionar el mismo archivo. */
  seleccionar(input: HTMLInputElement): void {
    const archivo = input.files?.item(0);
    input.value = '';
    if (this.deshabilitado || !archivo) {
      return;
    }
    const extension = archivo.name.split('.').pop()?.toLowerCase();
    if (!extension || !EXTENSIONES_DOCUMENTO.some((tipo) => tipo === extension)) {
      this.archivoRechazado.emit('Selecciona TXT, PDF, Word o una imagen admitida.');
    } else if (archivo.size === 0) {
      this.archivoRechazado.emit('El archivo está vacío.');
    } else if (archivo.size > MAX_ARCHIVO_BYTES) {
      this.archivoRechazado.emit('El archivo supera el máximo de 10 MB.');
    } else {
      this.archivoSeleccionado.emit(archivo);
    }
  }
}
