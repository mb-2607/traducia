import { HttpErrorResponse } from '@angular/common/http';
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
import type { ValidatorFn } from '@angular/forms';
import { finalize } from 'rxjs';
import { EXTENSIONES_DOCUMENTO, MAX_ARCHIVO_BYTES } from '../../api-config';
import type { Idioma } from '../../models/idiomamodel';
import { PLANES } from '../../models/planmodel';
import type { PlanId } from '../../models/planmodel';
import type {
  ProveedorTraduccion,
  SolicitudTraduccion,
  TraduccionResponseDTO,
} from '../../models/traduccionmodel';
import { AuthService } from '../../services/authservice';
import { DocumentoService } from '../../services/documentoservice';
import { GrabacionService } from '../../services/grabacionservice';
import { IdiomaService } from '../../services/idiomaservice';
import { TranscripcionService } from '../../services/transcripcionservice';
import { TraduccionHttpService } from '../../services/traduccionhttpservice';
import { TraduccionService } from '../../services/traduccionservice';

/** Comprueba el contenido sin alterar los espacios ni los saltos de línea. */
const sinSoloEspacios: ValidatorFn = (control) => {
  const valor: unknown = control.value;
  return typeof valor === 'string' && valor.trim().length > 0 ? null : { vacio: true };
};

/** Evita solicitar traducciones entre dos códigos de idioma iguales. */
const idiomasDistintos: ValidatorFn = (grupo) => {
  const origen: unknown = grupo.get('origen')?.value;
  const destino: unknown = grupo.get('destino')?.value;
  return typeof origen === 'string' && origen.length > 0 && origen === destino
    ? { mismosIdiomas: true }
    : null;
};

type Estado = 'reposo' | 'extrayendo' | 'grabando' | 'transcribiendo' | 'traduciendo';

/**
 * Coordina el editor, la extracción documental y el dictado mediante el backend.
 * Conserva el texto completo y exige corregir los excesos antes de traducir.
 * La grabación tiene alcance local; las APIs externas se invocan en el servidor.
 */
@Component({
  selector: 'app-nuevatraduccioncomponent',
  standalone: false,
  templateUrl: './nuevatraduccioncomponent.html',
  styleUrl: './nuevatraduccioncomponent.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [GrabacionService, { provide: TraduccionService, useClass: TraduccionHttpService }],
})
export class Nuevatraduccioncomponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly idiomaService = inject(IdiomaService);
  private readonly traductor = inject(TraduccionService);
  private readonly documentos = inject(DocumentoService);
  private readonly transcripciones = inject(TranscripcionService);
  private readonly grabacion = inject(GrabacionService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly planActual = signal<PlanId>('gratuito');
  private readonly limiteServidor = signal<number | null>(null);

  readonly estado = signal<Estado>('reposo');
  readonly ocupado = computed(() => this.estado() !== 'reposo');
  readonly escuchando = computed(() => this.grabacion.estado() === 'grabando');
  readonly listaIdioma = signal<Idioma[]>([]);
  readonly cargandoListaIdioma = signal(false);
  readonly errorIdiomas = signal('');
  readonly error = signal('');
  readonly aviso = signal('');
  readonly resultados = signal<readonly TraduccionResponseDTO[]>([]);
  readonly limiteCaracteres = computed(
    () => this.limiteServidor() ?? PLANES[this.planActual()].caracteresPorSolicitud,
  );
  readonly mensajeEstado = computed(() => {
    switch (this.estado()) {
      case 'extrayendo':
        return 'Extrayendo el texto del documento…';
      case 'transcribiendo':
        return 'Transcribiendo el audio…';
      case 'traduciendo':
        return 'Traduciendo el texto…';
      case 'grabando':
        return this.grabacion.estado() === 'solicitando'
          ? 'Permite el acceso al micrófono para comenzar.'
          : this.escuchando()
            ? 'Grabando. Pulsa detener para transcribir. Máximo: 2 minutos.'
            : 'Preparando el audio…';
      default:
        return '';
    }
  });
  readonly nombresProveedores: Readonly<Record<ProveedorTraduccion, string>> = {
    DEEPL: 'DeepL',
    GEMMA: 'Gemma',
    RIVA: 'Riva',
  };
  readonly form = this.fb.group(
    {
      texto: ['', [sinSoloEspacios, Validators.maxLength(PLANES.gratuito.caracteresPorSolicitud)]],
      origen: ['', Validators.required],
      destino: ['', Validators.required],
    },
    { validators: idiomasDistintos },
  );

  /** Sincroniza el plan y borra resultados anteriores al editar el formulario. */
  ngOnInit(): void {
    this.auth
      .getUserAsObservable()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((usuario) => {
        const plan = usuario?.plan;
        this.planActual.set(plan === 'oria' || plan === 'infinitia' ? plan : 'gratuito');
        this.limiteServidor.set(null);
        this.actualizarValidacionTexto();
      });
    this.form.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.resultados.set([]));
    this.cargarIdiomas();
  }

  /** Carga el catálogo existente y permite reintentar errores de conexión. */
  cargarIdiomas(): void {
    if (this.cargandoListaIdioma() || this.ocupado()) {
      return;
    }
    this.errorIdiomas.set('');
    this.cargandoListaIdioma.set(true);
    this.idiomaService
      .getListaIdioma()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          if (!this.destroyRef.destroyed) {
            this.cargandoListaIdioma.set(false);
          }
        }),
      )
      .subscribe({
        next: (idiomas) => {
          this.listaIdioma.set(idiomas);
          this.elegirIdiomasIniciales();
          if (new Set(idiomas.map((idioma) => idioma.id)).size < 2) {
            this.errorIdiomas.set('No hay suficientes idiomas disponibles para traducir.');
          }
        },
        error: () => {
          this.listaIdioma.set([]);
          this.form.patchValue({ origen: '', destino: '' });
          this.errorIdiomas.set('No fue posible cargar los idiomas. Inténtalo nuevamente.');
        },
      });
  }

  /** Envía el texto revisado; el backend vuelve a validar el límite del plan. */
  traducir(): void {
    if (this.ocupado() || this.cargandoListaIdioma()) {
      return;
    }
    this.error.set('');
    this.aviso.set('');
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      this.error.set('Revisa el texto, su límite y los idiomas seleccionados.');
      return;
    }
    if (!this.comprobarSesion()) {
      return;
    }
    const solicitud: SolicitudTraduccion = this.form.getRawValue();
    this.iniciar('traduciendo');
    this.traductor
      .traducir(solicitud)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.terminar()),
      )
      .subscribe({
        next: (respuesta) => {
          this.resultados.set(respuesta.listaTraduccion);
          const exitosas = respuesta.listaTraduccion.filter(
            (resultado) => resultado.estado === 'EXITOSA',
          );
          if (exitosas.length === 0) {
            this.error.set('Ningún proveedor pudo completar la traducción.');
          } else if (exitosas.length !== respuesta.listaTraduccion.length) {
            this.aviso.set(
              'Algunos proveedores no respondieron. Puedes consultar los resultados disponibles.',
            );
          }
        },
        error: (error: unknown) =>
          this.mostrarError(error, 'No fue posible completar la traducción.'),
      });
  }

  /** Valida la carga y añade el texto extraído sin recortarlo ni borrar lo escrito. */
  alSeleccionarArchivo(archivo: File): void {
    if (this.ocupado() || this.cargandoListaIdioma()) {
      return;
    }
    this.error.set('');
    const extension = archivo.name.split('.').pop()?.toLowerCase();
    if (!extension || !EXTENSIONES_DOCUMENTO.some((tipo) => tipo === extension)) {
      this.error.set('Selecciona un documento o imagen de un formato admitido.');
      return;
    }
    if (archivo.size === 0 || archivo.size > MAX_ARCHIVO_BYTES) {
      this.error.set('Selecciona un archivo no vacío de hasta 10 MB.');
      return;
    }
    const origen = this.form.controls.origen.value;
    if (!origen) {
      this.error.set('Selecciona el idioma de origen del documento.');
      return;
    }
    if (!this.comprobarSesion()) {
      return;
    }
    this.iniciar('extrayendo');
    this.documentos
      .extraer(archivo, origen)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.terminar()),
      )
      .subscribe({
        next: (respuesta) => {
          if (Number.isSafeInteger(respuesta.limiteCaracteres) && respuesta.limiteCaracteres > 0) {
            this.limiteServidor.set(respuesta.limiteCaracteres);
            this.actualizarValidacionTexto();
          }
          this.incorporarTexto(respuesta.texto, `Texto de «${archivo.name}» incorporado.`);
        },
        error: (error: unknown) => this.mostrarError(error, 'No fue posible extraer el documento.'),
      });
  }

  /** Mantiene bloqueado el editor durante la captura y envía un único audio final. */
  async iniciarGrabacion(): Promise<void> {
    if (this.ocupado() || this.cargandoListaIdioma() || !this.comprobarSesion()) {
      return;
    }
    this.iniciar('grabando');
    try {
      const archivo = await this.grabacion.iniciar();
      if (this.destroyRef.destroyed) {
        return;
      }
      if (archivo === null) {
        this.aviso.set('Grabación cancelada.');
        this.terminar();
        return;
      }
      this.transcribirAudio(archivo);
    } catch (error: unknown) {
      if (!this.destroyRef.destroyed) {
        this.mostrarError(error, 'No fue posible acceder al micrófono.');
        this.terminar();
      }
    }
  }

  /** El evento stop del grabador entregará el archivo con su último fragmento. */
  detenerGrabacion(): void {
    this.grabacion.detener();
  }

  /** Cancela incluso cuando el navegador todavía está solicitando permiso. */
  cancelarGrabacion(): void {
    this.grabacion.cancelar();
  }

  /** Permite añadir texto desde otros controles cuando el editor está libre. */
  agregarTexto(nuevo: string): void {
    if (!this.ocupado()) {
      this.incorporarTexto(nuevo, 'Texto incorporado.');
    }
  }

  /** El audio usa el idioma de origen o detección automática si no hay selección. */
  private transcribirAudio(archivo: File): void {
    this.estado.set('transcribiendo');
    const idioma = this.form.controls.origen.value || undefined;
    this.transcripciones
      .transcribir(archivo, idioma)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.terminar()),
      )
      .subscribe({
        next: (respuesta) =>
          this.incorporarTexto(
            respuesta.texto,
            'Audio transcrito. Revisa el texto antes de traducir.',
          ),
        error: (error: unknown) => this.mostrarError(error, 'No fue posible transcribir el audio.'),
      });
  }

  /** Conserva el texto íntegro y el contenido anterior, también si exceden el plan. */
  private incorporarTexto(nuevo: string, mensaje: string): void {
    if (nuevo.trim().length === 0) {
      this.error.set('No se encontró texto para incorporar.');
      return;
    }
    const control = this.form.controls.texto;
    const combinado = control.value.length > 0 ? `${control.value}\n\n${nuevo}` : nuevo;
    control.setValue(combinado);
    control.markAsDirty();
    control.markAsTouched();
    this.error.set('');
    this.aviso.set(
      combinado.length > this.limiteCaracteres()
        ? `${mensaje} Se conservó todo el contenido. Reduce el texto a ${this.limiteCaracteres()} caracteres para traducir.`
        : mensaje,
    );
  }

  /** Aplica el límite del servidor cuando está disponible; no modifica el valor. */
  private actualizarValidacionTexto(): void {
    const texto = this.form.controls.texto;
    texto.setValidators([sinSoloEspacios, Validators.maxLength(this.limiteCaracteres())]);
    texto.updateValueAndValidity({ emitEvent: false });
  }

  /** Evita iniciar cargas o permisos cuando no existe una sesión autenticada. */
  private comprobarSesion(): boolean {
    if (!this.auth.getJwt()) {
      this.error.set('Inicia sesión para utilizar documentos, audio y traducciones.');
      return false;
    }
    return true;
  }

  /** Limpia resultados obsoletos y bloquea el formulario durante una operación. */
  private iniciar(estado: Estado): void {
    this.error.set('');
    this.aviso.set('');
    this.resultados.set([]);
    this.estado.set(estado);
    this.form.disable({ emitEvent: false });
  }

  /** Restablece el editor únicamente mientras el componente siga montado. */
  private terminar(): void {
    if (!this.destroyRef.destroyed) {
      this.estado.set('reposo');
      this.form.enable({ emitEvent: false });
    }
  }

  /** Distingue autorización, conexión y errores públicos del backend. */
  private mostrarError(error: unknown, mensaje: string): void {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 401 || error.status === 403) {
        this.error.set('La sesión venció o no tiene permiso. Vuelve a iniciar sesión.');
      } else if (error.status === 0) {
        this.error.set(
          'No se pudo conectar con el backend. Revisa su dirección y la configuración CORS.',
        );
      } else {
        const cuerpo: unknown = error.error;
        const detalle =
          typeof cuerpo === 'object' &&
          cuerpo !== null &&
          'detail' in cuerpo &&
          typeof cuerpo.detail === 'string'
            ? cuerpo.detail
            : mensaje;
        this.error.set(detalle);
      }
    } else if (error instanceof DOMException && error.name === 'NotAllowedError') {
      this.error.set('Permite el acceso al micrófono en la configuración del navegador.');
    } else if (error instanceof DOMException && error.name === 'NotFoundError') {
      this.error.set('No se encontró un micrófono disponible.');
    } else {
      this.error.set(error instanceof Error ? error.message : mensaje);
    }
  }

  /** Mantiene las selecciones si continúan presentes en el catálogo. */
  private elegirIdiomasIniciales(): void {
    const ids = this.listaIdioma().map((idioma) => idioma.id);
    const { origen, destino } = this.form.controls;
    if (!ids.includes(origen.value)) {
      origen.setValue(ids.includes('es') ? 'es' : (ids[0] ?? ''));
    }
    const destinos = ids.filter((id) => id !== origen.value);
    if (!destinos.includes(destino.value)) {
      destino.setValue(destinos.includes('en') ? 'en' : (destinos[0] ?? ''));
    }
  }
}
