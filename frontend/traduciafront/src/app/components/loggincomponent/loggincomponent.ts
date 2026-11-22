import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Output,
  ViewChild,
  inject,
  signal,
} from '@angular/core';
import { Router } from '@angular/router';
import { NonNullableFormBuilder, Validators } from '@angular/forms';
import type { AbstractControl, ValidatorFn } from '@angular/forms';
import type { LoginRequest, RegisterRequest } from '../../models/usermodel';
import { AuthService } from '../../services/authservice';
import { TerminosService } from '../../services/terminosservice';
import { Terminocomponent } from '../terminocomponent/terminocomponent';

/**
 * Datos adicionales de la interfaz.
 *
 * Ciudad y edad permanecen pendientes de soporte en el backend.
 */
type RegistroCuenta = RegisterRequest & {
  ciudad: string;
  edad: number;
};

/** Valida la coincidencia sin modificar las contraseñas ingresadas. */
const contrasenasIguales: ValidatorFn = (control) => {
  const password: unknown = control.get('password')?.value;
  const confirmacion: unknown = control.get('confirmacion')?.value;

  return password === confirmacion ? null : { contrasenasDistintas: true };
};

/** Comprueba la estructura de los errores de longitud de Angular. */
function esErrorLongitud(valor: unknown): valor is { requiredLength: number } {
  return (
    typeof valor === 'object' &&
    valor !== null &&
    'requiredLength' in valor &&
    typeof valor.requiredLength === 'number'
  );
}

/** Devuelve el mensaje aplicable a un control que ya fue tocado. */
function obtenerError(control: AbstractControl): string {
  if (!control.touched || !control.errors) {
    return '';
  }

  if (control.hasError('required')) {
    return 'Completa este campo.';
  }

  if (control.hasError('email')) {
    return 'Ingresa un correo válido.';
  }

  const minimo: unknown = control.getError('minlength');

  if (esErrorLongitud(minimo)) {
    return `Ingresa al menos ${minimo.requiredLength} caracteres.`;
  }

  const maximo: unknown = control.getError('maxlength');

  if (esErrorLongitud(maximo)) {
    return `Ingresa hasta ${maximo.requiredLength} caracteres.`;
  }

  if (control.hasError('min') || control.hasError('max')) {
    return 'Ingresa una edad entre 1 y 120 años.';
  }

  return 'Revisa el formato de este campo.';
}

/**
 * Gestiona los formularios de acceso y registro.
 *
 * Comprueba la aceptación de términos antes de emitir solicitudes.
 * La integración recibe los eventos y determina el resultado
 * de las operaciones con el backend.
 */
@Component({
  selector: 'app-loggincomponent',
  standalone: false,
  templateUrl: './loggincomponent.html',
  styleUrl: './loggincomponent.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Loggincomponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly terminos = inject(TerminosService);

  @ViewChild(Terminocomponent)
  private modalTerminos?: Terminocomponent;
  /** Credenciales validadas, compatibles con el contrato de acceso. */
  @Output()
  readonly accesoSolicitado = new EventEmitter<LoginRequest>();
  /** Datos de registro conservando los campos de la interfaz actual. */
  @Output()
  readonly registroSolicitado = new EventEmitter<RegistroCuenta>();
  /** Solicita el flujo de acceso con Google a la integración. */
  @Output()
  readonly googleSolicitado = new EventEmitter<void>();
  /** Estado de aceptación compartido con el modal. */
  readonly terminosAceptados = this.terminos.aceptados;
  readonly modoRegistro = signal(false);
  readonly passwordVisible = signal(false);
  readonly mensaje = signal('');
  /** Expone los mensajes de validación a la plantilla. */
  readonly errorCampo = obtenerError;

  /**
   * Formulario de acceso.
   *
   * El nombre de usuario puede incluir números y no puede
   * contener únicamente espacios.
   */
  readonly acceso = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(80), Validators.pattern(/\S/)]],
    password: ['', [Validators.required, Validators.maxLength(128)]],
  });
  /**
   * Formulario de registro.
   *
   * Ciudad y edad siguen siendo campos adicionales del frontend.
   */
  readonly registro = this.fb.group(
    {
      nombre: [
        '',
        [
          Validators.required,
          Validators.minLength(2),
          Validators.maxLength(80),
          Validators.pattern(/\S/),
        ],
      ],
      ciudad: [
        '',
        [
          Validators.required,
          Validators.minLength(2),
          Validators.maxLength(80),
          Validators.pattern(/\S/),
        ],
      ],
      correo: ['', [Validators.required, Validators.email, Validators.maxLength(254)]],
      edad: [
        '',
        [
          Validators.required,
          Validators.pattern(/^\d{1,3}$/),
          Validators.min(1),
          Validators.max(120),
        ],
      ],
      password: [
        '',
        [
          Validators.required,
          Validators.minLength(8),
          Validators.maxLength(128),
          Validators.pattern(/\S/),
        ],
      ],
      confirmacion: ['', [Validators.required, Validators.maxLength(128)]],
    },
    { validators: contrasenasIguales },
  );

  /** Actualiza el mensaje después de una decisión en el modal. */
  actualizarAceptacionTerminos(aceptados: boolean): void {
    if (!aceptados) {
      this.mensaje.set('Debes aceptar los términos para continuar.');
      return;
    }

    this.mensaje.set(
      this.terminos.persistenciaDisponible()
        ? ''
        : 'Aceptación válida para esta sesión. El navegador no permitió recordarla.',
    );
  }

  /** Alterna el formulario y elimina las contraseñas del modo anterior. */
  cambiarModo(): void {
    this.modoRegistro.update((actual) => !actual);
    this.passwordVisible.set(false);
    this.mensaje.set('');
    this.acceso.controls.password.reset();
    this.registro.controls.password.reset();
    this.registro.controls.confirmacion.reset();
  }

  /** Alterna la representación visible u oculta de las contraseñas. */
  alternarPassword(): void {
    this.passwordVisible.update((actual) => !actual);
  }

  /** Emite las credenciales únicamente después de aceptar y validar. */
  iniciarSesion(): void {
    if (!this.comprobarTerminos()) {
      return;
    }

    this.mensaje.set('');
    const nombre = this.acceso.controls.nombre;
    nombre.setValue(nombre.value.trim());
    if (this.acceso.invalid) {
      this.acceso.markAllAsTouched();
      this.mensaje.set('Revisa los campos para continuar.');
      return;
    }

    const datos: LoginRequest = this.acceso.getRawValue();
    this.accesoSolicitado.emit(datos);
    this.mensaje.set('Iniciando sesión...');

    this.auth.iniciarSesion(datos).subscribe({
      next: (usuario) => {
        this.mensaje.set('');
        const destino = usuario.rol === 'ADMIN' ? ['/admin'] : ['/dashboard/new'];
        this.router.navigate(destino).catch(() => undefined);
      },
      error: (err: unknown) => {
        if (
          typeof err === 'object' &&
          err !== null &&
          'error' in err &&
          typeof err.error === 'string' &&
          err.error.trim().length > 0
        ) {
          this.mensaje.set(err.error);
          return;
        }
        this.mensaje.set('No fue posible iniciar sesión. Revisa tus datos.');
      },
    });
  }

  /**
   * Normaliza datos de texto y emite el registro.
   *
   * Conserva la contraseña exactamente como fue ingresada.
   * No inicia sesión automáticamente.
   */
  registrarCuenta(): void {
    if (!this.comprobarTerminos()) {
      return;
    }

    this.mensaje.set('');
    const { nombre, ciudad, correo } = this.registro.controls;
    nombre.setValue(nombre.value.trim());
    ciudad.setValue(ciudad.value.trim());
    correo.setValue(correo.value.trim());
    if (this.registro.invalid) {
      this.registro.markAllAsTouched();
      this.mensaje.set('Revisa los campos para crear tu cuenta.');
      return;
    }

    const datos = this.registro.getRawValue();
    const payloadRegistro: RegisterRequest = {
      nombre: datos.nombre,
      correo: datos.correo,
      password: datos.password,
    };

    this.registroSolicitado.emit({
      ...payloadRegistro,
      ciudad: datos.ciudad,
      edad: Number(datos.edad),
    });
    this.mensaje.set('Creando cuenta...');

    this.auth.registrarUsuario(payloadRegistro).subscribe({
      next: () => {
        this.mensaje.set('Cuenta creada. Revisa tu correo para verificarla antes de iniciar sesión.');
      },
      error: (err: unknown) => {
        if (
          typeof err === 'object' &&
          err !== null &&
          'error' in err &&
          typeof err.error === 'string' &&
          err.error.trim().length > 0
        ) {
          this.mensaje.set(err.error);
          return;
        }
        this.mensaje.set('No fue posible crear la cuenta.');
      },
    });
  }

  /** Comprueba los términos antes de solicitar el flujo de Google. */
  continuarConGoogle(): void {
    if (!this.comprobarTerminos()) {
      return;
    }

    this.mensaje.set('El acceso con Google está pendiente de conexión.');
    this.googleSolicitado.emit();
  }

  /**
   * Consulta el estado real del servicio antes de continuar.
   *
   * El evento del modal comunica la decisión, pero no sustituye
   * esta comprobación.
   */
  private comprobarTerminos(): boolean {
    if (this.terminosAceptados()) {
      return true;
    }

    this.mensaje.set('Debes aceptar los términos para continuar.');
    this.modalTerminos?.abrir();
    return false;
  }
}
