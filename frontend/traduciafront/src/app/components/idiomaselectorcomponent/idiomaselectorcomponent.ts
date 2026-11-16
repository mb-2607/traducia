import {Component, Input} from '@angular/core';
import {FormControl} from '@angular/forms';
import {Idioma} from '../../models/idiomamodel';

/**
 * Componente reutilizable encargado de mostrar un selector de idiomas.
 *
 * Recibe mediante propiedades de entrada el título del selector,
 * el FormControl asociado, la lista de idiomas disponibles y el
 * estado de carga de los datos.
 *
 * Permite integrar el selector en diferentes formularios manteniendo
 * centralizada su estructura y facilitando la reutilización de la interfaz.
 */
@Component({
  selector: 'app-idiomaselectorcomponent',
  standalone: false,
  styleUrl: './idiomaselectorcomponent.css',
  templateUrl: './idiomaselectorcomponent.html',
})
export class Idiomaselectorcomponent {
  /** Título que identifica el selector de idiomas. */
  @Input() titulo?: string;

  /** Control reactivo asociado al selector para gestionar su valor y validaciones. */
  @Input({required: true}) control?: FormControl<String>;

  /** Lista de idiomas disponibles para seleccionar. */
  @Input({required: true}) listaIdioma?: Idioma[];

  /** Indica si los idiomas se encuentran en proceso de carga. */
  @Input({required: true}) cargando = false;
}
