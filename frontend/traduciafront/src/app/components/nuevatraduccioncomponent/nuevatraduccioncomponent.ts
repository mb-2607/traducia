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
import type { Idioma } from '../../models/idiomamodel';
import { PLANES } from '../../models/planmodel';
import type { PlanId } from '../../models/planmodel';
import type {
  ProveedorTraduccion,
  SolicitudTraduccion,
  TraduccionResponseDTO,
} from '../../models/traduccionmodel';
import { AuthService } from '../../services/authservice';
import { IdiomaService } from '../../services/idiomaservice';
import { TraduccionService } from '../../services/traduccionservice';

/** Rechaza valores vacíos y contenido compuesto únicamente por espacios. */
const sinSoloEspacios: ValidatorFn = (control) => {
  const valor: unknown = control.value;
  return typeof valor === 'string' && valor.trim().length > 0 ? null : { vacio: true };
};

/** Comprueba que los códigos de origen y destino sean diferentes. */
const idiomasDistintos: ValidatorFn = (grupo) => {
  const origen: unknown = grupo.get('origen')?.value;
  const destino: unknown = grupo.get('destino')?.value;
  return typeof origen === 'string' && origen.length > 0 && origen === destino
    ? { mismosIdiomas: true }
    : null;
};

/** Estados de las operaciones actualmente contempladas por el componente. */
type Estado = 'reposo' | 'traduciendo';

/**
 * Prepara y valida solicitudes de traducción.
 *
 * Aplica el límite de caracteres del usuario, gestiona los idiomas
 * disponibles y presenta los resultados individuales por proveedor.
 *
 * Utiliza TraduccionService para enviar las solicitudes al backend.
 */
@Component({
  selector: 'app-nuevatraduccioncomponent',
  standalone: false,
  templateUrl: './nuevatraduccioncomponent.html',
  styleUrl: './nuevatraduccioncomponent.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Nuevatraduccioncomponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly idiomaService = inject(IdiomaService);
  private readonly traductor = inject(TraduccionService);
  private readonly destroyRef = inject(DestroyRef);

  /** Sin una sesión disponible se utiliza el límite del plan gratuito. */
  private readonly planActual = signal<PlanId>('gratuito');
  readonly estado = signal<Estado>('reposo');
  readonly ocupado = computed(() => this.estado() === 'traduciendo');

  /** Indica que el servicio de traducción está disponible para enviar solicitudes. */
  readonly traduccionDisponible = true;
  readonly listaIdioma = signal<Idioma[]>([]);
  readonly cargandoListaIdioma = signal(false);
  readonly errorIdiomas = signal('');
  readonly error = signal('');
  readonly aviso = signal('');
  readonly resultados = signal<readonly TraduccionResponseDTO[]>([]);
  readonly limiteCaracteres = computed(() => PLANES[this.planActual()].caracteresPorSolicitud);

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

  /** Sincroniza el límite con la sesión y carga los idiomas disponibles. */
  ngOnInit(): void {
    this.auth
      .getUserAsObservable()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((usuario) => {
        const plan = usuario?.plan;
        this.planActual.set(plan === 'oria' || plan === 'infinitia' ? plan : 'gratuito');

        const texto = this.form.controls.texto;
        texto.setValidators([sinSoloEspacios, Validators.maxLength(this.limiteCaracteres())]);

        texto.updateValueAndValidity({ emitEvent: false });
      });

    this.cargarIdiomas();
  }

  /** Permite cargar o reintentar la consulta de idiomas. */
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
        finalize(() => this.cargandoListaIdioma.set(false)),
      )
      .subscribe({
        next: (idiomas) => {
          this.listaIdioma.set(idiomas);
          this.elegirIdiomasIniciales();
          const cantidadIdiomas = new Set(idiomas.map((idioma) => idioma.id)).size;

          if (cantidadIdiomas < 2) {
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

  /**
   * Valida el formulario y envía la solicitud mediante TraduccionService.
   *
   * Conserva el texto original, incluidos espacios y saltos de línea.
   * El backend deberá volver a validar el límite del usuario.
   */
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

    const solicitud: SolicitudTraduccion = this.form.getRawValue();
    this.resultados.set([]);
    this.estado.set('traduciendo');
    this.form.disable({ emitEvent: false });
    this.traductor
      .traducir(solicitud)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.estado.set('reposo');
          this.form.enable({ emitEvent: false });
        }),
      )
      .subscribe({
        next: (respuesta) => {
          this.resultados.set(respuesta.listaTraduccion);

          if (respuesta.listaTraduccion.length === 0) {
            this.error.set('No se recibieron resultados de traducción.');
          }
        },
        error: () => {
          this.error.set('No fue posible completar la traducción. Inténtalo nuevamente.');
        },
      });
  }

  /**
   * Informa de la selección sin simular una extracción de contenido.
   *
   * La integración de archivos se incorporará cuando esté disponible.
   */
  alSeleccionarArchivo(archivo: File): void {
    if (this.ocupado()) {
      return;
    }

    this.error.set('');
    this.aviso.set(`La lectura de «${archivo.name}» aún no está disponible.`);
  }

  /**
   * Incorpora texto adicional sin recortarlo ni superar el límite del plan.
   *
   * Puede utilizarse posteriormente para incorporar contenido extraído.
   */
  agregarTexto(nuevo: string): void {
    if (this.ocupado() || nuevo.trim().length === 0) {
      return;
    }

    const control = this.form.controls.texto;
    const combinado = control.value.length > 0 ? `${control.value} ${nuevo}` : nuevo;

    if (combinado.length > this.limiteCaracteres()) {
      this.error.set('El contenido adicional supera el límite de caracteres de tu plan.');
      return;
    }

    this.error.set('');
    control.setValue(combinado);
  }

  /** Elige idiomas iniciales distintos y presentes en la lista disponible. */
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
