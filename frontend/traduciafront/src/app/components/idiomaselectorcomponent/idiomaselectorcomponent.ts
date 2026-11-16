import {Component, Input} from '@angular/core';
import {FormControl} from '@angular/forms';
import {Idioma} from '../../models/idiomamodel';

@Component({
  selector: 'app-idiomaselectorcomponent',
  standalone: false,
  styleUrl: './idiomaselectorcomponent.css',
  templateUrl: './idiomaselectorcomponent.html',
})
export class Idiomaselectorcomponent {
  @Input() titulo?: string;
  @Input({required: true}) control?: FormControl<String>;
  @Input({required: true}) listaIdioma?: Idioma[];
  @Input({required: true}) cargando = false;
}
