import { HttpErrorResponse } from '@angular/common/http';
import {
  Component,
  DestroyRef,
  ElementRef,
  Injector,
  OnInit,
  ViewChild,
  afterNextRender,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, Validators } from '@angular/forms';
import type { AbstractControl, ValidatorFn } from '@angular/forms';
import { finalize } from 'rxjs';
import { PLANES } from '../../models/planmodel';
import type { Planmodel } from '../../models/planmodel';
import { SuscripcionService } from '../../services/suscripcionservice';

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

/** Lista de países disponibles en el formulario de facturación. */
const PAISES = [
  { codigo: 'CO', nombre: 'Colombia' },
  { codigo: 'MX', nombre: 'México' },
  { codigo: 'PE', nombre: 'Perú' },
  { codigo: 'ES', nombre: 'España' },
  { codigo: 'OTRO', nombre: 'Otro país' },
] as const;

/**
 * Valida el formato y la vigencia de la fecha de vencimiento.
 *
 * Comprueba que la fecha tenga el formato MM/AA, que el mes
 * sea válido y que no sea anterior al mes actual. La obligatoriedad
 * del campo se valida por separado.
 *
 * @param control Control cuyo valor contiene la fecha de vencimiento.
 * @returns Un error de validación si la fecha no es válida, o `null` en caso contrario.
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

/**
 * Formatea un precio utilizando el formato monetario colombiano.
 *
 * @param precio Valor numérico que se desea formatear.
 * @returns Precio representado en pesos colombianos.
 */
function precioFormateado(precio: number): string {
  return FORMATO_PESOS.format(precio);
}

/**
 * Describe el máximo de caracteres de entrada permitido por solicitud.
 *
 * @param plan Plan cuyo límite se desea mostrar.
 * @returns Descripción del límite de caracteres.
 */
function limiteCaracteres(plan: Planmodel): string {
  return `${FORMATO_NUMERO.format(plan.caracteresPorSolicitud)} caracteres por traducción`;
}

/**
 * Obtiene la descripción del límite diario de traducciones de archivos.
 * Un valor `null` en el plan indica que no hay un cupo diario.
 *
 * @param plan Plan del cual se desea consultar el límite de archivos.
 * @returns Descripción del límite diario de traducciones de archivos.
 */
function limiteArchivos(plan: Planmodel): string {
  if (plan.archivosPorDia === null) {
    return 'Traducciones de archivos ilimitadas al día';
  }

  return `${plan.archivosPorDia} traducciones de archivos al día`;
}

/**
 * Indica si se debe mostrar un error de validación para un control.
 *
 * @param control Control del formulario que se desea comprobar.
 * @returns `true` si el control está marcado como tocado y es inválido.
 */
function campoInvalido(control: AbstractControl): boolean {
  return control.touched && control.invalid;
}

/** Obtiene un mensaje legible a partir de un error HTTP al cambiar de plan. */
function extraerMensajeError(error: unknown): string {
  if (error instanceof HttpErrorResponse && typeof error.error === 'string' && error.error.trim()) {
    return error.error;
  }

  return 'No fue posible actualizar tu plan en este momento. Inténtalo nuevamente.';
}

/**
 * Gestiona la visualización, selección y confirmación de planes
 * de suscripción dentro de TraducIA.
 *
 * Consulta los límites y el plan vigente desde el backend, valida el formulario
 * y solicita al servicio el cambio de plan real de la cuenta autenticada.
 */
@Component({
  selector: 'app-suscripcioncomponent',
  standalone: false,
  templateUrl: './suscripcioncomponent.html',
  styleUrls: ['./suscripcioncomponent.css'],
})
export class Suscripcioncomponent implements OnInit {
  /** Servicio encargado de consultar y actualizar el plan del usuario. */
  private readonly suscripcion = inject(SuscripcionService);

  /** Constructor utilizado para inicializar el formulario reactivo. */
  private readonly fb = inject(NonNullableFormBuilder);

  /** Inyector utilizado para registrar operaciones posteriores al renderizado. */
  private readonly injector = inject(Injector);

  /** Referencia de destrucción para cancelar suscripciones activas. */
  private readonly destroyRef = inject(DestroyRef);

  /** Elemento que recibe el foco después de seleccionar o confirmar un plan. */
  @ViewChild('destinoFoco')
  private destinoFoco?: ElementRef<HTMLElement>;

  /** Botón al que se devuelve el foco al cancelar la selección. */
  private botonOrigen: HTMLButtonElement | null = null;

  /** Lista de planes disponibles para mostrar en la interfaz. */
  readonly planes = signal<Planmodel[]>(Object.values(PLANES));

  /** Lista de países disponibles para el formulario de pago. */
  readonly paises = PAISES;

  /** Señal que contiene el identificador del plan activo. */
  readonly planActivo = this.suscripcion.planActivo;

  /** Señal que contiene el plan seleccionado por el usuario. */
  readonly planSeleccionado = signal<Planmodel | null>(null);

  /** Señal que contiene el plan cuya confirmación se ha completado. */
  readonly planConfirmado = signal<Planmodel | null>(null);

  /** Señal que indica si hay una solicitud de cambio de plan en curso. */
  readonly guardando = signal(false);

  /** Señal que contiene el mensaje general de validación o error del formulario. */
  readonly mensajeFormulario = signal('');

  /** Ejemplo dinámico de fecha de vencimiento correspondiente al siguiente año. */
  readonly vencimientoEjemplo = `12/${String(new Date().getFullYear() + 1).slice(-2)}`;

  /** Formatea los precios que se muestran en la plantilla. */
  readonly precioFormateado = precioFormateado;

  /** Describe el límite de caracteres de un plan en la plantilla. */
  readonly limiteCaracteres = limiteCaracteres;

  /** Describe el cupo diario de archivos de un plan en la plantilla. */
  readonly limiteArchivos = limiteArchivos;

  /** Determina cuándo debe mostrarse el error de un campo en la plantilla. */
  readonly campoInvalido = campoInvalido;

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

  /** Sincroniza el catálogo de planes y el plan vigente desde el backend. */
  ngOnInit(): void {
    this.suscripcion
      .listarPlanes()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (planes) => {
          if (planes.length > 0) {
            this.planes.set(planes);
          }
        },
        error: () => {
          // Conserva el catálogo local de respaldo si falla la consulta inicial.
        },
      });

    this.suscripcion
      .obtenerPlanActual()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        error: () => {
          // Conserva el plan cargado desde la sesión actual si falla la consulta.
        },
      });
  }

  /**
   * Obtiene el nombre del plan que se encuentra actualmente activo.
   *
   * @returns Nombre correspondiente al plan activo.
   */
  get nombrePlanActivo(): string {
    return PLANES[this.planActivo()].nombre;
  }

  /**
   * Inicia la selección de un plan para su posterior confirmación.
   *
   * Evita seleccionar el plan que ya está activo, guarda el botón
   * de origen y restablece el formulario y la confirmación anterior.
   *
   * @param plan Plan seleccionado por el usuario.
   * @param boton Botón desde el cual se inició la selección.
   */
  seleccionarPlan(plan: Planmodel, boton: HTMLButtonElement): void {
    if (this.guardando() || plan.id === this.planActivo()) {
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
   * Cancela la selección actual y restablece el formulario.
   *
   * Limpia los mensajes y el plan seleccionado, devuelve el foco
   * al botón que inició la operación y libera su referencia.
   */
  cancelar(): void {
    if (this.guardando()) {
      return;
    }

    this.pago.reset();
    this.mensajeFormulario.set('');
    this.planSeleccionado.set(null);
    this.botonOrigen?.focus();
    this.botonOrigen = null;
  }

  /**
   * Valida y confirma la selección de un plan de suscripción contra el backend.
   *
   * Los planes de pago requieren un formulario válido. Cuando se cumplen las
   * validaciones, solicita al servicio actualizar el plan del usuario autenticado.
   */
  confirmarSuscripcion(): void {
    const plan = this.planSeleccionado();

    if (this.guardando() || !plan || plan.id === this.planActivo()) {
      return;
    }

    if (plan.id !== 'gratuito' && this.pago.invalid) {
      this.pago.markAllAsTouched();
      this.mensajeFormulario.set('Revisa los campos marcados para continuar.');
      return;
    }

    this.guardando.set(true);
    this.mensajeFormulario.set('');

    this.suscripcion
      .cambiarPlan(plan.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.guardando.set(false)),
      )
      .subscribe({
        next: () => {
          this.pago.reset();
          this.mensajeFormulario.set('');
          this.planSeleccionado.set(null);
          this.planConfirmado.set(plan);
          this.botonOrigen = null;
          this.enfocarResultado();
        },
        error: (error: unknown) => {
          this.mensajeFormulario.set(extraerMensajeError(error));
        },
      });
  }

  /**
   * Mueve el foco al elemento de destino después del siguiente renderizado.
   * Usa el inyector del componente para registrar la operación.
   */
  private enfocarResultado(): void {
    afterNextRender(() => this.destinoFoco?.nativeElement.focus(), {
      injector: this.injector,
    });
  }
}
