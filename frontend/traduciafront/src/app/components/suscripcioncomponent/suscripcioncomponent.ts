import {
  Component,
  ElementRef,
  Injector,
  ViewChild,
  afterNextRender,
  inject,
  signal,
} from '@angular/core';
import { NonNullableFormBuilder, Validators } from '@angular/forms';
import type { AbstractControl, ValidatorFn } from '@angular/forms';
import { PLANES } from '../../models/planmodel';
import type { Planmodel } from '../../models/planmodel';
import { SuscripcionService } from '../../services/suscripcionservice';

/**
 * Componente encargado de gestionar la visualización, selección y
 * confirmación de planes de suscripción dentro de TraducIA.
 *
 * Proporciona información sobre los planes disponibles, permite
 * seleccionar una suscripción y administra el formulario de pago
 * con sus respectivas validaciones.
 *
 * Utiliza Angular Signals para gestionar los estados reactivos
 * de selección y confirmación de planes, y se comunica con
 * SuscripcionService para consultar y activar las suscripciones.
 *
 * También implementa gestión del foco para mejorar la accesibilidad
 * durante la selección, cancelación y confirmación de los planes.
 */

/**
 * Formateador de valores monetarios según la configuración regional
 * colombiana, utilizando pesos colombianos sin decimales.
 */
const FORMATO_PESOS = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 0,
});

/** Formateador de valores numéricos según la configuración regional colombiana. */
const FORMATO_NUMERO = new Intl.NumberFormat('es-CO');

/**
 * Lista de países disponibles para seleccionar en el formulario
 * de información de facturación.
 */
const PAISES = [
  { codigo: 'CO', nombre: 'Colombia' },
  { codigo: 'MX', nombre: 'México' },
  { codigo: 'PE', nombre: 'Perú' },
  { codigo: 'ES', nombre: 'España' },
  { codigo: 'OTRO', nombre: 'Otro país' },
] as const;

/**
 * Valida el formato y la vigencia de la fecha de vencimiento
 * de la tarjeta de pago.
 *
 * Comprueba que la fecha tenga el formato MM/AA, que el mes
 * sea válido y que no corresponda a una fecha anterior al mes actual.
 *
 * @param control Control cuyo valor contiene la fecha de vencimiento.
 * @returns Un error de validación si la fecha no es válida; null en caso contrario.
 */
const validarVencimiento: ValidatorFn = (control) => {
  const valor = String(control.value ?? '');

  if (!valor) {
    return null;
  }

  const partes = /^(0[1-9]|1[0-2])\/(\d{2})$/.exec(valor);

  if (!partes) {
    return { vencimiento: true };
  }

  const mes = Number(partes[1]);
  const anio = 2000 + Number(partes[2]);
  const hoy = new Date();
  const anioActual = hoy.getFullYear();
  const mesActual = hoy.getMonth() + 1;

  if (anio < anioActual || (anio === anioActual && mes < mesActual)) {
    return { vencimiento: true };
  }

  return null;
};

@Component({
  selector: 'app-suscripcioncomponent',
  standalone: false,
  templateUrl: './suscripcioncomponent.html',
  styleUrls: ['./suscripcioncomponent.css'],
})
export class Suscripcioncomponent {

  /** Servicio encargado de consultar y gestionar las suscripciones. */
  private readonly suscripcion = inject(SuscripcionService);

  /** Constructor utilizado para inicializar el formulario reactivo. */
  private readonly fb = inject(NonNullableFormBuilder);

  /** Inyector utilizado para ejecutar operaciones posteriores al renderizado. */
  private readonly injector = inject(Injector);

  /** Referencia al elemento HTML que recibirá el foco al mostrar un resultado. */
  @ViewChild('destinoFoco')
  private destinoFoco?: ElementRef<HTMLElement>;

  /** Almacena el botón desde el cual se inició la selección de un plan. */
  private botonOrigen: HTMLButtonElement | null = null;

  /** Lista de planes disponibles para mostrar en la interfaz. */
  readonly planes = Object.values(PLANES);

  /** Lista de países disponibles para el formulario de pago. */
  readonly paises = PAISES;

  /** Signal que representa el identificador del plan actualmente activo. */
  readonly planActivo = this.suscripcion.planActivo;

  /** Signal que almacena el plan seleccionado por el usuario. */
  readonly planSeleccionado = signal<Planmodel | null>(null);

  /** Signal que almacena el plan cuya confirmación se ha completado. */
  readonly planConfirmado = signal<Planmodel | null>(null);

  /** Signal que contiene los mensajes informativos o de validación del formulario. */
  readonly mensajeFormulario = signal('');

  /** Ejemplo dinámico de fecha de vencimiento correspondiente al siguiente año. */
  readonly vencimientoEjemplo = `12/${String(new Date().getFullYear() + 1).slice(-2)}`;

  /** Formulario reactivo que contiene los datos de facturación y pago. */
  readonly pago = this.fb.group({
    titular: [
      '',
      [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(60),
        Validators.pattern(/^[\p{L}\p{M}]+(?:[ .’'-][\p{L}\p{M}]+)*$/u),
      ],
    ],
    pais: ['CO', [Validators.required, Validators.pattern(/^(CO|MX|PE|ES|OTRO)$/)]],
    direccion: [
      '',
      [
        Validators.required,
        Validators.minLength(5),
        Validators.maxLength(120),
        Validators.pattern(/\S/),
      ],
    ],
    empresa: ['', Validators.maxLength(80)],
    tarjeta: ['', [Validators.required, Validators.pattern(/^\d(?:[ -]?\d){12,18}$/)]],
    vencimiento: ['', [Validators.required, validarVencimiento]],
    cvc: ['', [Validators.required, Validators.pattern(/^\d{3,4}$/)]],
    confirmar: [false, Validators.requiredTrue],
  });

  /**
   * Obtiene el nombre del plan que se encuentra actualmente activo.
   *
   * @returns Nombre correspondiente al plan activo.
   */
  get nombrePlanActivo(): string {
    return PLANES[this.planActivo()].nombre;
  }

  /**
   * Formatea un precio utilizando el formato monetario colombiano.
   *
   * @param precio Valor numérico que se desea formatear.
   * @returns Precio representado en pesos colombianos.
   */
  precioFormateado(precio: number): string {
    return FORMATO_PESOS.format(precio);
  }

  /**
   * Obtiene la descripción del límite de palabras permitido por un plan.
   * Contempla los planes que ofrecen traducciones ilimitadas.
   *
   * @param plan Plan del cual se desea consultar el límite de palabras.
   * @returns Descripción del límite de palabras por traducción.
   */
  limitePalabras(plan: Planmodel): string {
    if (plan.palabras === null) {
      return 'Palabras ilimitadas por traducción';
    }

    return `${FORMATO_NUMERO.format(plan.palabras)} palabras por traducción`;
  }

  /**
   * Obtiene la descripción del límite diario de traducciones de archivos.
   * Contempla los planes que ofrecen traducciones ilimitadas.
   *
   * @param plan Plan del cual se desea consultar el límite de archivos.
   * @returns Descripción del límite diario de traducciones de archivos.
   */
  limiteArchivos(plan: Planmodel): string {
    if (plan.archivosPorDia === null) {
      return 'Traducciones de archivos ilimitadas al día';
    }

    return `${plan.archivosPorDia} traducciones de archivos al día`;
  }

  /**
   * Comprueba si un control debe mostrar su estado de validación como inválido.
   * Solo considera los controles que ya han sido marcados como modificados.
   *
   * @param control Control del formulario que se desea comprobar.
   * @returns true si el control fue tocado y contiene errores de validación.
   */
  campoInvalido(control: AbstractControl): boolean {
    return control.touched && control.invalid;
  }

  /**
   * Inicia la selección de un plan para su posterior confirmación.
   *
   * Evita seleccionar nuevamente el plan que ya se encuentra activo,
   * almacena el botón de origen, restablece el formulario y limpia
   * los estados de confirmación anteriores.
   *
   * @param plan Plan seleccionado por el usuario.
   * @param boton Botón HTML desde el cual se inició la selección.
   */
  seleccionarPlan(plan: Planmodel, boton: HTMLButtonElement): void {
    if (plan.id === this.planActivo()) {
      return;
    }

    this.botonOrigen = boton;
    this.pago.reset();
    this.mensajeFormulario.set('');
    this.planConfirmado.set(null);
    this.planSeleccionado.set(plan);
    this.enfocarResultado();
  }

  /**
   * Cancela la selección actual de un plan y restablece el formulario.
   *
   * Limpia los mensajes y el plan seleccionado, devuelve el foco
   * al botón que inició la operación y libera su referencia.
   */
  cancelar(): void {
    this.pago.reset();
    this.mensajeFormulario.set('');
    this.planSeleccionado.set(null);
    this.botonOrigen?.focus();
    this.botonOrigen = null;
  }

  /**
   * Valida y confirma la selección de un plan de suscripción.
   *
   * Comprueba que exista un plan seleccionado y que el formulario
   * sea válido cuando se trata de un plan de pago.
   *
   * Si las validaciones se cumplen, solicita la activación del plan
   * mediante SuscripcionService y actualiza los estados de la interfaz.
   */
  confirmarSuscripcion(): void {
    const plan = this.planSeleccionado();

    if (!plan || plan.id === this.planActivo()) {
      return;
    }

    if (plan.id !== 'gratuito' && this.pago.invalid) {
      this.pago.markAllAsTouched();
      this.mensajeFormulario.set('Revisa los campos marcados para continuar.');
      return;
    }

    this.suscripcion.activarPlanDemo(plan.id);

    this.pago.reset();
    this.mensajeFormulario.set('');
    this.planSeleccionado.set(null);
    this.planConfirmado.set(plan);
    this.botonOrigen = null;
    this.enfocarResultado();
  }

  /**
   * Programa el desplazamiento del foco hacia el elemento de destino
   * una vez Angular haya completado el siguiente renderizado.
   *
   * Utiliza el inyector del componente para ejecutar afterNextRender
   * dentro del contexto de inyección correspondiente.
   */
  private enfocarResultado(): void {
    afterNextRender(() => this.destinoFoco?.nativeElement.focus(), {
      injector: this.injector,
    });
  }
}
