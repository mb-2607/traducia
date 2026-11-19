import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { NonNullableFormBuilder, Validators } from '@angular/forms';
import type { ValidatorFn } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { RecuperacionService } from '../../services/recuperacionservice';
import { AuthService } from '../../services/authservice';

/** Comprueba la confirmación sin modificar ni recortar las contraseñas. */
const contrasenasIguales: ValidatorFn = (grupo) => {
  const password: unknown = grupo.get('password')?.value;
  const confirmacion: unknown = grupo.get('confirmacion')?.value;
  return password === confirmacion ? null : { contrasenasDistintas: true };
};

/**
 * Gestiona la solicitud del enlace y el establecimiento de una nueva contraseña.
 * La ruta define el modo mediante data.modo. El token se conserva únicamente
 * en memoria y se retira de la URL tras su lectura inicial.
 * Solo muestra un resultado correcto después de recibir respuesta del servicio.
 */
@Component({
  selector: 'app-recuperarcontrasenacomponent',
  standalone: false,
  templateUrl: './recuperarcontrasenacomponent.html',
  styleUrl: './recuperarcontrasenacomponent.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Recuperarcontrasenacomponent implements OnInit {
  private readonly servicio = inject(RecuperacionService, { optional: true });
  private readonly auth = inject(AuthService);
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private token: string | null = null;

  protected readonly disponible = this.servicio !== null;
  protected readonly restableciendo = this.route.snapshot.data['modo'] === 'restablecer';
  protected readonly enlaceValido = signal(false);
  protected readonly enviando = signal(false);
  protected readonly completado = signal(false);
  protected readonly visible = signal(false);
  protected readonly error = signal('');
  protected readonly titulo = computed(() =>
    this.restableciendo ? 'Crea una nueva contraseña' : 'Recupera tu acceso',
  );

  protected readonly solicitud = this.fb.group({
    correo: ['', [Validators.required, Validators.email, Validators.maxLength(254)]],
  });
  protected readonly nueva = this.fb.group(
    {
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

  constructor() {
    this.destroyRef.onDestroy(() => {
      this.token = null;
      this.nueva.reset();
    });
  }

  /** Lee el token una vez y evita conservarlo en la dirección visible de la página. */
  ngOnInit(): void {
    if (!this.restableciendo) {
      return;
    }
    const tokens = this.route.snapshot.queryParamMap.getAll('token');
    const token = tokens.length === 1 ? tokens[0] : undefined;
    this.enlaceValido.set(token !== undefined && token.length > 0 && token.length <= 4096);
    this.token = this.enlaceValido() && token !== undefined ? token : null;
    if (tokens.length > 0) {
      void this.router.navigate([], {
        relativeTo: this.route,
        queryParams: { token: null },
        queryParamsHandling: 'merge',
        replaceUrl: true,
      });
    }
  }

  /** Solicita un enlace sin informar si el correo corresponde a una cuenta. */
  protected solicitarEnlace(): void {
    const servicio = this.servicio;
    if (servicio === null || this.enviando() || this.completado()) {
      return;
    }
    this.solicitud.controls.correo.setValue(this.solicitud.controls.correo.value.trim());
    this.solicitud.markAllAsTouched();
    if (this.solicitud.invalid) {
      return;
    }
    this.enviando.set(true);
    this.error.set('');
    servicio
      .solicitarEnlace(this.solicitud.controls.correo.value)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.enviando.set(false)),
      )
      .subscribe({
        next: () => this.completado.set(true),
        error: () =>
          this.error.set('No fue posible procesar la solicitud. Inténtalo nuevamente más tarde.'),
      });
  }

  /** Envía la contraseña después de validar su formato y confirmación. */
  protected restablecer(): void {
    const servicio = this.servicio;
    const token = this.token;
    if (servicio === null || token === null || this.enviando() || this.completado()) {
      return;
    }
    this.nueva.markAllAsTouched();
    if (this.nueva.invalid) {
      return;
    }
    this.enviando.set(true);
    this.error.set('');
    servicio
      .restablecer(token, this.nueva.controls.password.value)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.enviando.set(false)),
      )
      .subscribe({
        next: () => {
          this.token = null;
          this.nueva.reset();
          this.visible.set(false);
          this.completado.set(true);
          this.auth.cerrarSesion();
        },
        error: () =>
          this.error.set(
            'No fue posible cambiar la contraseña. Si el enlace venció o ya fue utilizado, solicita uno nuevo.',
          ),
      });
  }

  /** Alterna la visibilidad sin copiar las contraseñas a otro estado. */
  protected alternarVisibilidad(): void {
    this.visible.update((valor) => !valor);
  }

  /** Permite volver al formulario después de una solicitud procesada. */
  protected volverASolicitar(): void {
    this.completado.set(false);
    this.error.set('');
  }
}
