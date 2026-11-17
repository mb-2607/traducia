import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Output,
  inject,
  signal,
} from '@angular/core';
import { NonNullableFormBuilder, Validators } from '@angular/forms';
import type { AbstractControl, ValidatorFn } from '@angular/forms';
import type { LoginRequest, RegisterRequest } from '../../models/usermodel';

/**
 * Datos adicionales solicitados por el formulario de registro.
 * Amplía el contrato existente mientras se acuerda su integración con el backend.
 */
type RegistroCuenta = RegisterRequest & {
  ciudad: string;
  edad: number;
};

/**
 * Verifica que la contraseña y su confirmación coincidan.
 *
 * La obligatoriedad de ambos campos se valida por separado.
 *
 * @param control Grupo que contiene la contraseña y su confirmación.
 * @returns El error de coincidencia o `null` cuando ambos valores coinciden.
 */
const contrasenasIguales: ValidatorFn = (control) => {
  const password: unknown = control.get('password')?.value;
  const confirmacion: unknown = control.get('confirmacion')?.value;

  if (password === confirmacion) {
    return null;
  }

  return { contrasenasDistintas: true };
};

/**
 * Obtiene un mensaje de validación para un campo que ya fue tocado.
 *
 * @param control Campo que se desea comprobar.
 * @returns El primer mensaje aplicable o una cadena vacía.
 */
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

  if (control.hasError('minlength')) {
    return `Ingresa al menos ${control.getError('minlength').requiredLength} caracteres.`;
  }

  if (control.hasError('maxlength')) {
    return `Ingresa hasta ${control.getError('maxlength').requiredLength} caracteres.`;
  }

  if (control.hasError('min') || control.hasError('max')) {
    return 'Ingresa una edad entre 1 y 120 años.';
  }

  return 'Revisa el formato de este campo.';
}

/**
 * Gestiona los formularios de acceso y registro de TraducIA.
 *
 * Valida los datos antes de emitir las solicitudes a la integración
 * responsable de autenticar o registrar al usuario.
 *
 * Mantiene las contraseñas en los controles del formulario y permite
 * alternar su visibilidad. El acceso con Google se solicita mediante
 * un evento independiente.
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

  /** Solicitud de acceso validada con el contrato existente del proyecto. */
  @Output() readonly accesoSolicitado = new EventEmitter<LoginRequest>();

  /** Solicitud de registro con los datos adicionales del formulario. */
  @Output() readonly registroSolicitado = new EventEmitter<RegistroCuenta>();

  /** Solicita a la integración iniciar el acceso mediante Google. */
  @Output() readonly googleSolicitado = new EventEmitter<void>();

  /** Indica si se está mostrando el formulario de registro. */
  readonly modoRegistro = signal(false);

  /** Controla la visibilidad de las contraseñas de la tarjeta. */
  readonly passwordVisible = signal(false);

  /** Mensaje de validación o estado de la solicitud. */
  readonly mensaje = signal('');

  /** Expone los mensajes de validación a la plantilla. */
  readonly errorCampo = obtenerError;

  /** Formulario de inicio de sesión. */
  readonly acceso = this.fb.group({
    correo: ['', [Validators.required, Validators.email, Validators.maxLength(254)]],
    password: ['', [Validators.required, Validators.maxLength(128)]],
  });

  /** Formulario de registro y confirmación de contraseña. */
  readonly registro = this.fb.group(
    {
      nombre: [
        '',
        [
          Validators.required,
          Validators.minLength(2),
          Validators.maxLength(80),
          Validators.pattern(/^[\p{L}\p{M}]+(?:[ .’'-][\p{L}\p{M}]+)*$/u),
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

  /**
   * Alterna el formulario y limpia contraseñas y mensajes anteriores.
   * Conserva los demás campos para facilitar la navegación entre modos.
   */
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

  /**
   * Valida las credenciales y emite la solicitud de acceso.
   * La integración de autenticación determina el resultado de la operación.
   */
  iniciarSesion(): void {
    this.mensaje.set('');

    if (this.acceso.invalid) {
      this.acceso.markAllAsTouched();
      this.mensaje.set('Revisa los campos para continuar.');
      return;
    }

    const datos = this.acceso.getRawValue();

    this.mensaje.set('Datos validados. El inicio de sesión está pendiente de conexión.');
    this.accesoSolicitado.emit({
      correo: datos.correo.trim(),
      password: datos.password,
    });
  }

  /**
   * Valida el formulario y emite los datos necesarios para crear la cuenta.
   *
   * Mapea el nombre al campo `username` del contrato actual del proyecto.
   * Convierte la edad a número después de superar las validaciones.
   */
  registrarCuenta(): void {
    this.mensaje.set('');

    if (this.registro.invalid) {
      this.registro.markAllAsTouched();
      this.mensaje.set('Revisa los campos para crear tu cuenta.');
      return;
    }

    const datos = this.registro.getRawValue();

    this.mensaje.set('Datos validados. La creación de la cuenta está pendiente de conexión.');
    this.registroSolicitado.emit({
      username: datos.nombre.trim(),
      ciudad: datos.ciudad.trim(),
      correo: datos.correo.trim(),
      edad: Number(datos.edad),
      password: datos.password,
    });
  }

  /** Solicita el acceso mediante Google a la integración de autenticación. */
  continuarConGoogle(): void {
    this.mensaje.set('El acceso con Google está pendiente de conexión.');
    this.googleSolicitado.emit();
  }
}
