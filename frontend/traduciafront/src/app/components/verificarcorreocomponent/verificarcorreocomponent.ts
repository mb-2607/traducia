import { Location } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  inject,
  signal,
} from '@angular/core';
import type { OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import {
  EMPTY,
  Subject,
  catchError,
  defer,
  exhaustMap,
  finalize,
  startWith,
  switchMap,
  tap,
} from 'rxjs';
import type { Observable } from 'rxjs';
import { VerificacionCorreoService } from '../../services/verificacioncorreoservice';
/** Estados visuales; la validez real del token la determina el backend. */
type EstadoVerificacion =
  'sin-enlace' | 'enlace-invalido' | 'pendiente' | 'verificando' | 'verificado' | 'error';

/** Contenido presentado para cada estado del proceso. */
interface PresentacionEstado {
  readonly titulo: string;
  readonly descripcion: string;
  readonly icono: string;
}

/** Límite preventivo del enlace, no una comprobación de autenticidad. */
const LONGITUD_MAXIMA_TOKEN = 4096;
const PRESENTACIONES: Readonly<Record<EstadoVerificacion, PresentacionEstado>> = {
  'sin-enlace': {
    titulo: 'Verifica tu correo',
    descripcion:
      'Abre el enlace que enviamos a tu correo. También puedes solicitar uno nuevo aquí.',
    icono: 'fa-envelope-open-text',
  },
  'enlace-invalido': {
    titulo: 'Enlace incompleto',
    descripcion:
      'El enlace no tiene el formato esperado. Abre el enlace completo del correo o solicita otro.',
    icono: 'fa-triangle-exclamation',
  },
  pendiente: {
    titulo: 'Verificación pendiente',
    descripcion: 'Tu correo todavía no se ha verificado.',
    icono: 'fa-envelope-circle-check',
  },
  verificando: {
    titulo: 'Verificando tu correo',
    descripcion: 'Espera un momento mientras comprobamos el enlace.',
    icono: 'fa-spinner fa-spin',
  },
  verificado: {
    titulo: 'Correo verificado',
    descripcion:
      'La verificación se completó. Ya puedes iniciar sesión con tu nombre de usuario y contraseña.',
    icono: 'fa-circle-check',
  },
  error: {
    titulo: 'No pudimos verificar tu correo',
    descripcion: 'Inténtalo otra vez o solicita un nuevo enlace de verificación.',
    icono: 'fa-circle-exclamation',
  },
};

/**
 * Gestiona la verificación de correo y la solicitud de nuevos enlaces.
 *
 * Trabaja con un contrato de servicio sin implementar peticiones HTTP.
 * Nunca muestra éxito basándose únicamente en la presencia de un token.
 */
@Component({
  selector: 'app-verificarcorreocomponent',
  standalone: false,
  templateUrl: './verificarcorreocomponent.html',
  styleUrl: './verificarcorreocomponent.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Verificarcorreocomponent implements OnInit {
  private readonly servicio = inject(VerificacionCorreoService, {
    optional: true,
  });

  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly location = inject(Location);
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly destroyRef = inject(DestroyRef);
  private readonly reintentos = new Subject<void>();
  /** Indica si existe una implementación registrada del servicio. */
  readonly disponible = this.servicio !== null;
  readonly estado = signal<EstadoVerificacion>('sin-enlace');
  readonly reenviando = signal(false);
  readonly reenviado = signal(false);
  readonly errorReenvio = signal('');
  readonly presentacion = computed(() => PRESENTACIONES[this.estado()]);
  readonly verificando = computed(() => this.estado() === 'verificando');
  readonly verificado = computed(() => this.estado() === 'verificado');
  readonly ocupado = computed(() => this.verificando() || this.reenviando());
  readonly puedeReintentar = computed(
    () => this.estado() === 'pendiente' || this.estado() === 'error',
  );

  readonly formulario = this.fb.group({
    correo: ['', [Validators.required, Validators.email, Validators.maxLength(254)]],
  });

  /**
   * Lee el enlace y comienza la verificación cuando existe un servicio.
   *
   * Cambiar de enlace cancela la verificación anterior.
   * Los reintentos no generan solicitudes simultáneas.
   */
  ngOnInit(): void {
    this.route.queryParamMap
      .pipe(
        switchMap((parametros) => {
          const tokens = parametros.getAll('token');
          if (tokens.length === 0) {
            this.estado.set('sin-enlace');
            return EMPTY;
          }

          this.retirarTokenDeDireccion();
          const token = tokens[0];
          if (
            tokens.length !== 1 ||
            token === undefined ||
            token.length === 0 ||
            token.length > LONGITUD_MAXIMA_TOKEN ||
            /\s/.test(token)
          ) {
            this.estado.set('enlace-invalido');
            return EMPTY;
          }

          this.estado.set('pendiente');

          return this.reintentos.pipe(
            startWith(null),
            exhaustMap(() => this.verificarToken(token)),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe();
  }

  /** Evita verificaciones simultáneas y repeticiones tras el éxito. */
  reintentarVerificacion(): void {
    if (!this.disponible || this.ocupado() || !this.puedeReintentar()) {
      return;
    }

    this.reintentos.next();
  }

  /**
   * Solicita otro enlace de verificación.
   *
   * No modifica la sesión ni activa cuentas localmente.
   */
  reenviarEnlace(): void {
    const servicio = this.servicio;

    if (servicio === null || this.ocupado() || this.verificado()) {
      return;
    }

    this.errorReenvio.set('');
    this.reenviado.set(false);

    const correo = this.formulario.controls.correo;
    correo.setValue(correo.value.trim());

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const direccion = correo.value;
    this.reenviando.set(true);

    defer(() => servicio.reenviar(direccion))
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.reenviando.set(false)),
      )
      .subscribe({
        next: () => {
          this.reenviado.set(true);
          this.formulario.reset();
        },
        error: () => {
          this.errorReenvio.set(
            'No pudimos solicitar el enlace. Revisa el correo e inténtalo nuevamente.',
          );
        },
      });
  }

  /**
   * Solicita la validación real del token.
   *
   * Los errores se capturan dentro del flujo para permitir reintentos.
   */
  private verificarToken(token: string): Observable<void> {
    const servicio = this.servicio;

    if (servicio === null || this.reenviando()) {
      return EMPTY;
    }

    this.estado.set('verificando');

    return defer(() => servicio.verificar(token)).pipe(
      tap(() => this.estado.set('verificado')),
      catchError(() => {
        this.estado.set('error');
        return EMPTY;
      }),
      finalize(() => {
        if (this.verificando()) {
          this.estado.set('pendiente');
        }
      }),
    );
  }

  /**
   * Retira el token de la barra de direcciones sin recargar
   * ni emitir una nueva navegación.
   *
   * El flujo conserva el valor únicamente en memoria.
   */
  private retirarTokenDeDireccion(): void {
    const direccion = this.router.createUrlTree([], {
      relativeTo: this.route,
      queryParams: { token: null },
      queryParamsHandling: 'merge',
      preserveFragment: true,
    });

    this.location.replaceState(this.router.serializeUrl(direccion));
  }
}
