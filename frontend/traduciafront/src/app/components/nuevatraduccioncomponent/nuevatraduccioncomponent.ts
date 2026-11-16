import {Component, inject, OnInit} from '@angular/core';
import {FormBuilder, ValidatorFn, Validators} from '@angular/forms';
import {IdiomaService} from '../../services/idiomaservice';
import {Idioma} from '../../models/idiomamodel';
import {ResultadoTraduccion} from '../../models/traduccionmodel';


/**
 * Componente encargado de gestionar la creación de nuevas traducciones
 * dentro de TraducIA.
 *
 * Administra el formulario reactivo de traducción, la selección de idiomas,
 * la validación del texto ingresado y la recepción de archivos para
 * su posterior extracción de contenido.
 *
 * Controla los estados del proceso de traducción y extracción de archivos,
 * habilitando o deshabilitando el formulario según la operación en curso.
 *
 * Obtiene los idiomas disponibles mediante IdiomaService y proporciona
 * métodos para actualizar el contenido del formulario y establecer
 * los idiomas iniciales.
 */

/**
 * Valida que el contenido del control no esté vacío ni contenga
 * únicamente espacios en blanco.
 *
 * @param control Control cuyo valor se desea validar.
 * @returns Un error de validación si el contenido está vacío; null en caso contrario.
 */
const sinSoloEspacios: ValidatorFn = (control) =>
  (control.value ?? '').trim().length === 0 ? { vacio: true } : null;

/**
 * Valida que los idiomas de origen y destino sean diferentes.
 *
 * @param grupo Grupo de controles que contiene los idiomas de origen y destino.
 * @returns Un error de validación si ambos idiomas son iguales; null en caso contrario.
 */
const idiomasDistintos: ValidatorFn = (grupo) => {
  const origen = grupo.get('origen')?.value;
  const destino = grupo.get('destino')?.value;
  return origen && destino && origen === destino ? { mismosIdiomas: true } : null;
};

/**
 * Estados posibles durante el proceso de creación de una traducción.
 * - reposo: no hay operaciones en ejecución.
 * - extrayendo: se está procesando un archivo.
 * - traduciendo: se está realizando una traducción.
 */
type Estado = 'reposo' | 'extrayendo' | 'traduciendo';

@Component({
  selector: 'app-nuevatraduccioncomponent',
  standalone: false,
  styleUrl: './nuevatraduccioncomponent.css',
  templateUrl: './nuevatraduccioncomponent.html',
})
export class Nuevatraduccioncomponent implements OnInit {

  /** Constructor de formularios reactivos de Angular. */
  private fb = inject(FormBuilder);

  /** Servicio encargado de proporcionar los idiomas disponibles. */
  private idiomaService = inject(IdiomaService);

  /** Estado actual del proceso de traducción o extracción de archivos. */
  estado: Estado = 'reposo';

  /** Indica si se encuentra activo el reconocimiento de voz. */
  escuchando = false;

  /** Indica si existe alguna operación en curso. */
  ocupado = false;

  /** Resultados obtenidos durante el proceso de traducción. */
  resultadoLista: ResultadoTraduccion[] = [];

  /** Mensaje de error asociado a las operaciones del componente. */
  error = '';

  /** Formulario reactivo que contiene el texto y los idiomas de traducción. */
  readonly form = this.fb.nonNullable.group(
    {
      texto: ['', [sinSoloEspacios]],
      origen: ['', Validators.required],
      destino: ['', Validators.required],
    },
    { validators: idiomasDistintos },
  );

  /** Lista de idiomas disponibles para realizar traducciones. */
  listaIdioma: Idioma[] = [];

  /** Indica si la lista de idiomas todavía se encuentra en proceso de carga. */
  cargandoListaIdioma = true;

  /**
   * Inicializa la carga de los idiomas disponibles mediante IdiomaService.
   * Actualiza la lista de idiomas y finaliza el estado de carga
   * cuando se recibe la respuesta.
   */
  ngOnInit() {
    this.idiomaService.getListaIdioma().subscribe(
      listaIdioma => {
        this.listaIdioma = listaIdioma;
        this.cargandoListaIdioma = false;
      }
    );
  }

  /**
   * Inicia el proceso de traducción del texto introducido en el formulario.
   * Actualmente se encuentra pendiente de implementar la integración
   * con el servicio encargado de realizar las traducciones.
   */
  traducir() {
    this.agregarTexto("Traduciendo")
    // TODO: Implementar la integración con el servicio de traducción.
  }

  /**
   * Recibe un archivo seleccionado desde el componente selector
   * e inicia el proceso de extracción de su contenido.
   *
   * @param archivo Archivo seleccionado por el usuario.
   */
  alSeleccionarArchivo(archivo: File): void {
    this.iniciar('extrayendo');
    // TODO: Implementar la integración con el servicio de archivos.
  }

  /**
   * Incorpora nuevo contenido al texto existente en el formulario.
   * Conserva el texto anterior y separa el contenido agregado
   * mediante un espacio.
   *
   * @param nuevo Texto que se desea incorporar al formulario.
   */
  agregarTexto(nuevo: string): void {
    const control = this.form.controls.texto;
    control.setValue([control.value.trimEnd(), nuevo.trim()].filter(Boolean).join(' '));
  }

  /**
   * Establece el estado de una operación e indica que el formulario
   * debe permanecer deshabilitado mientras esta se ejecuta.
   *
   * También limpia cualquier mensaje de error anterior.
   *
   * @param estado Estado al que debe cambiar el componente.
   */
  private iniciar(estado: Estado): void {
    this.estado = estado;
    this.error = '';
    this.form.disable();
  }

  /**
   * Finaliza la operación actual, restablece el estado de reposo
   * y habilita nuevamente el formulario.
   */
  private terminar(): void {
    this.estado = 'reposo';
    this.form.enable();
  }

  /**
   * Establece los idiomas iniciales del formulario cuando todavía
   * no se han seleccionado manualmente.
   *
   * Prioriza español como idioma de origen e inglés como destino.
   * Si no están disponibles, utiliza los primeros idiomas de la lista.
   */
  private elegirIdiomasIniciales(): void {
    const ids = this.listaIdioma.map((i) => i.id);
    const { origen, destino } = this.form.controls;

    if (!origen.value) origen.setValue(ids.includes('es') ? 'es' : (ids[0] ?? ''));
    if (!destino.value) destino.setValue(ids.includes('en') ? 'en' : (ids[1] ?? ''));
  }
}
