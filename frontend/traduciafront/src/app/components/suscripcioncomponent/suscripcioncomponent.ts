import {
  Component,
  ElementRef,
  Injector,
  ViewChild,
  afterNextRender,
  inject,
  signal,
} from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import type { AbstractControl, ValidatorFn } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PLANES } from '../../models/plan';
import type { Plan } from '../../models/plan';
import { SuscripcionService } from '../../services/suscripcionservice';
const FORMATO_PESOS = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 0,
});
const FORMATO_NUMERO = new Intl.NumberFormat('es-CO');
const PAISES = [
  { codigo: 'CO', nombre: 'Colombia' },
  { codigo: 'MX', nombre: 'México' },
  { codigo: 'PE', nombre: 'Perú' },
  { codigo: 'ES', nombre: 'España' },
  { codigo: 'OTRO', nombre: 'Otro país' },
] as const;
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
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './suscripcioncomponent.html',
  styleUrls: ['./suscripcioncomponent.css'],
})
export class Suscripcioncomponent {
  private readonly suscripcion = inject(SuscripcionService);
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly injector = inject(Injector);
  @ViewChild('destinoFoco')
  private destinoFoco?: ElementRef<HTMLElement>;
  private botonOrigen: HTMLButtonElement | null = null;
  readonly planes = Object.values(PLANES);
  readonly paises = PAISES;
  readonly planActivo = this.suscripcion.planActivo;
  readonly planSeleccionado = signal<Plan | null>(null);
  readonly planConfirmado = signal<Plan | null>(null);
  readonly mensajeFormulario = signal('');
  readonly vencimientoEjemplo = `12/${String(new Date().getFullYear() + 1).slice(-2)}`;

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

  get nombrePlanActivo(): string {
    return PLANES[this.planActivo()].nombre;
  }

  precioFormateado(precio: number): string {
    return FORMATO_PESOS.format(precio);
  }

  limitePalabras(plan: Plan): string {
    if (plan.palabras === null) {
      return 'Palabras ilimitadas por traducción';
    }

    return `${FORMATO_NUMERO.format(plan.palabras)} palabras por traducción`;
  }

  limiteArchivos(plan: Plan): string {
    if (plan.archivosPorDia === null) {
      return 'Traducciones de archivos ilimitadas al día';
    }

    return `${plan.archivosPorDia} traducciones de archivos al día`;
  }

  campoInvalido(control: AbstractControl): boolean {
    return control.touched && control.invalid;
  }

  seleccionarPlan(plan: Plan, boton: HTMLButtonElement): void {
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

  cancelar(): void {
    this.pago.reset();
    this.mensajeFormulario.set('');
    this.planSeleccionado.set(null);
    this.botonOrigen?.focus();
    this.botonOrigen = null;
  }

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

  private enfocarResultado(): void {
    afterNextRender(() => this.destinoFoco?.nativeElement.focus(), { injector: this.injector });
  }
}
