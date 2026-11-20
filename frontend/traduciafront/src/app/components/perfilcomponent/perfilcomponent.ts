import { DOCUMENT } from '@angular/common';
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
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { EMPTY, Subject, catchError, finalize, startWith, switchMap, takeUntil } from 'rxjs';
import { PLANES } from '../../models/planmodel';
import type { GuardarPerfil, PerfilUsuario } from '../../models/perfilmodel';
import type { User } from '../../models/usermodel';
import { AuthService } from '../../services/authservice';
import { PerfilService } from '../../services/perfilservice';

/** Tamaño máximo admitido para una imagen de perfil, expresado en bytes. */
const MAXIMO_FOTO = 10 * 1024 * 1024;
const TIPOS_FOTO = new Set(['image/png', 'image/jpeg', 'image/webp']);

/** Construye el perfil inicial a partir de la sesión, sin copiar sus credenciales. */
function perfilDesdeSesion(usuario: User): PerfilUsuario {
  return {
    nombre: usuario.nombre,
    correo: usuario.correo,
    rol: usuario.rol,
    plan: usuario.plan,
    ciudad: null,
    edad: null,
    fotoUrl: null,
  };
}

/**
 * Permite revisar y editar los datos personales de la cuenta autenticada.
 * Usa la sesión como información inicial y consulta los datos ampliados cuando
 * existe una implementación de PerfilService. Nunca genera un usuario ficticio.
 * La imagen seleccionada es una vista previa hasta que el servidor confirma
 * el guardado. Sus URL temporales se liberan al sustituirla o abandonar la vista.
 */
@Component({
  selector: 'app-perfilcomponent',
  standalone: false,
  templateUrl: './perfilcomponent.html',
  styleUrl: './perfilcomponent.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Perfilcomponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly servicio = inject(PerfilService, { optional: true });
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly router = inject(Router);
  private readonly documento = inject(DOCUMENT);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cambioSesion = new Subject<void>();
  private readonly recarga = new Subject<void>();
  private readonly fotoNueva = signal<File | null>(null);
  private readonly quitarFoto = signal(false);
  private readonly vistaFoto = signal<string | null>(null);
  private versionFoto = 0;

  protected readonly perfil = signal<PerfilUsuario | null>(null);
  protected readonly disponible = this.servicio !== null;
  protected readonly cargando = signal(false);
  protected readonly guardando = signal(false);
  protected readonly preparandoFoto = signal(false);
  protected readonly perfilCargado = signal(false);
  protected readonly fotoFallida = signal(false);
  protected readonly error = signal('');
  protected readonly exito = signal('');
  protected readonly errorFoto = signal('');
  protected readonly planes = PLANES;

  /** Ciudad y edad son opcionales hasta que se acuerde su contrato definitivo. */
  protected readonly formulario = this.fb.group({
    nombre: [
      '',
      [
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(80),
        Validators.pattern(/\S/),
      ],
    ],
    ciudad: ['', [Validators.maxLength(80), Validators.pattern(/\S/)]],
    edad: ['', [Validators.pattern(/^\d{1,3}$/), Validators.min(1), Validators.max(120)]],
  });
  private readonly valores = toSignal(this.formulario.valueChanges, {
    initialValue: this.formulario.getRawValue(),
  });

  protected readonly rutaVolver = computed(() =>
    this.perfil()?.rol === 'ADMIN' ? '/admin' : '/dashboard',
  );
  protected readonly iniciales = computed(() => {
    const partes = this.perfil()?.nombre.trim().split(/\s+/).filter(Boolean) ?? [];
    return partes.length === 0
      ? '?'
      : partes
          .slice(0, 2)
          .map((parte) => parte.charAt(0))
          .join('')
          .toLocaleUpperCase('es');
  });
  protected readonly imagen = computed(() => {
    const vista = this.vistaFoto();
    if (vista !== null) {
      return this.fotoFallida() ? null : vista;
    }
    const ruta = this.perfil()?.fotoUrl;
    if (this.quitarFoto() || this.fotoFallida() || !ruta) {
      return null;
    }
    try {
      const url = new URL(ruta, this.documento.baseURI);
      return url.protocol === 'https:' || url.protocol === 'http:' ? url.href : null;
    } catch {
      return null;
    }
  });
  protected readonly hayFoto = computed(
    () => this.vistaFoto() !== null || (this.perfil()?.fotoUrl != null && !this.quitarFoto()),
  );
  protected readonly fotoPendiente = computed(() => this.fotoNueva() !== null || this.quitarFoto());
  protected readonly hayCambios = computed(() => {
    const actual = this.perfil();
    const valores = this.valores();
    return (
      actual !== null &&
      ((valores.nombre ?? '').trim() !== actual.nombre ||
        (valores.ciudad ?? '').trim() !== (actual.ciudad ?? '') ||
        (valores.edad ?? '').trim() !== (actual.edad?.toString() ?? '') ||
        this.fotoNueva() !== null ||
        this.quitarFoto())
    );
  });

  constructor() {
    this.destroyRef.onDestroy(() => {
      this.versionFoto += 1;
      this.liberarVista();
      this.cambioSesion.complete();
      this.recarga.complete();
    });
  }

  /** Cambia de cuenta sin conservar datos personales de una sesión anterior. */
  ngOnInit(): void {
    this.auth
      .getUserAsObservable()
      .pipe(
        switchMap((usuario) => {
          this.cambioSesion.next();
          this.error.set('');
          this.exito.set('');
          this.perfilCargado.set(false);
          this.aplicarPerfil(usuario === null ? null : perfilDesdeSesion(usuario));
          const servicio = this.servicio;
          if (usuario === null || servicio === null) {
            return EMPTY;
          }
          return this.recarga.pipe(
            startWith(null),
            switchMap(() => {
              this.cargando.set(true);
              this.error.set('');
              return servicio.obtener().pipe(
                catchError(() => {
                  this.error.set(
                    'No fue posible cargar el perfil completo. Se muestran los datos de tu sesión.',
                  );
                  return EMPTY;
                }),
                finalize(() => this.cargando.set(false)),
              );
            }),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((perfil) => {
        this.perfilCargado.set(true);
        this.aplicarPerfil(perfil);
      });
  }

  /** Repite una consulta fallida antes de permitir la edición de datos completos. */
  protected reintentarCarga(): void {
    if (!this.cargando() && !this.guardando()) {
      this.recarga.next();
    }
  }

  /** Valida el archivo y su decodificación antes de mostrar la vista previa. */
  protected async seleccionarFoto(evento: Event): Promise<void> {
    const entrada = evento.target;
    if (!(entrada instanceof HTMLInputElement)) {
      return;
    }
    const archivo = entrada.files?.item(0);
    entrada.value = '';
    if (!archivo || this.perfil() === null || this.guardando() || this.cargando()) {
      return;
    }
    this.errorFoto.set('');
    this.exito.set('');
    if (!TIPOS_FOTO.has(archivo.type) || archivo.size === 0 || archivo.size > MAXIMO_FOTO) {
      this.errorFoto.set(
        'Selecciona una imagen PNG, JPEG o WebP de hasta 10 MB que no esté vacía.',
      );
      return;
    }
    const version = ++this.versionFoto;
    const vista = URL.createObjectURL(archivo);
    let conservada = false;
    this.preparandoFoto.set(true);
    try {
      const imagen = this.documento.createElement('img');
      imagen.src = vista;
      await imagen.decode();
      if (version !== this.versionFoto) {
        return;
      }
      this.liberarVista();
      this.vistaFoto.set(vista);
      this.fotoNueva.set(archivo);
      this.quitarFoto.set(false);
      this.fotoFallida.set(false);
      conservada = true;
    } catch {
      if (version === this.versionFoto) {
        this.errorFoto.set('No fue posible abrir la imagen. Selecciona otro archivo.');
      }
    } finally {
      if (!conservada) {
        URL.revokeObjectURL(vista);
      }
      if (version === this.versionFoto) {
        this.preparandoFoto.set(false);
      }
    }
  }

  /** Prepara la retirada de la imagen; la eliminación se confirma al guardar. */
  protected retirarFoto(): void {
    this.versionFoto += 1;
    this.liberarVista();
    this.fotoNueva.set(null);
    this.quitarFoto.set(this.perfil()?.fotoUrl != null);
    this.fotoFallida.set(false);
    this.errorFoto.set('');
    this.exito.set('');
    this.preparandoFoto.set(false);
  }

  /** Recupera los valores del último perfil confirmado y descarta la vista previa. */
  protected descartar(): void {
    this.restaurarFormulario();
    this.error.set('');
    this.exito.set('');
  }

  /** Envía los cambios sin alterar el correo, el rol ni el plan de la cuenta. */
  protected guardar(): void {
    const servicio = this.servicio;
    if (
      servicio === null ||
      !this.perfilCargado() ||
      this.perfil() === null ||
      this.guardando() ||
      this.cargando() ||
      this.preparandoFoto()
    ) {
      return;
    }
    const valores = this.formulario.getRawValue();
    this.formulario.setValue({
      nombre: valores.nombre.trim(),
      ciudad: valores.ciudad.trim(),
      edad: valores.edad.trim(),
    });
    this.formulario.markAllAsTouched();
    if (this.formulario.invalid || !this.hayCambios()) {
      return;
    }
    const datos = this.formulario.getRawValue();
    const solicitud: GuardarPerfil = {
      datos: {
        nombre: datos.nombre,
        ciudad: datos.ciudad || null,
        edad: datos.edad === '' ? null : Number(datos.edad),
      },
      foto: this.fotoNueva(),
      quitarFoto: this.quitarFoto(),
    };
    this.guardando.set(true);
    this.error.set('');
    this.exito.set('');
    servicio
      .guardar(solicitud)
      .pipe(
        takeUntil(this.cambioSesion),
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.guardando.set(false)),
      )
      .subscribe({
        next: (perfil) => {
          this.aplicarPerfil(perfil);
          this.exito.set('Tu perfil se actualizó correctamente.');
        },
        error: () =>
          this.error.set(
            'No fue posible guardar los cambios. Se conservan tus datos para que puedas revisarlos.',
          ),
      });
  }

  /**
   * Cierra únicamente la sesión que mantiene este navegador
   * y redirige al inicio de sesión.
   */
  protected cerrarSesion(): void {
    if (this.guardando()) {
      return;
    }

    this.auth.cerrarSesion();
    this.router.navigate(['/login']).catch(() => {
      this.error.set(
        'La sesión se cerró, pero no fue posible abrir el inicio de sesión.',
      );
    });
  }

  /** Sustituye el perfil visible y toma sus valores como base de edición. */
  private aplicarPerfil(perfil: PerfilUsuario | null): void {
    this.perfil.set(perfil);
    this.restaurarFormulario();
  }

  /** Descarta cambios de campos y fotografía sin escribir en el servidor. */
  private restaurarFormulario(): void {
    this.versionFoto += 1;
    this.liberarVista();
    this.fotoNueva.set(null);
    this.quitarFoto.set(false);
    this.preparandoFoto.set(false);
    this.fotoFallida.set(false);
    this.errorFoto.set('');
    const actual = this.perfil();
    this.formulario.reset({
      nombre: actual?.nombre ?? '',
      ciudad: actual?.ciudad ?? '',
      edad: actual?.edad?.toString() ?? '',
    });
  }

  /** Evita conservar en memoria una URL de imagen que ya no se utiliza. */
  private liberarVista(): void {
    const vista = this.vistaFoto();
    if (vista !== null) {
      URL.revokeObjectURL(vista);
      this.vistaFoto.set(null);
    }
  }
}
