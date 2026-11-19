import { Component } from '@angular/core';

@Component({
  selector: 'app-noencontradocomponent',
  standalone: false,
  templateUrl: './noencontradocomponent.html',
  styleUrl: './noencontradocomponent.css'
})
/**
 * Componente de pagina no encontrada (404).
 */
export class Noencontradocomponent {
  readonly codigoError = 404;
}
