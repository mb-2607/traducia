import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  ElementRef,
  OnInit,
  ViewChild,
  computed,
  inject,
  signal,
} from '@angular/core';

import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import type {
  RegistroAuditoria,
  ResultadoAuditoria,
  TipoActorAuditoria,
} from '../../models/auditoriamodel';

import type { PlanAdministracion } from '../../models/usuarioadminmodel';
import { AuditoriaService } from '../../services/auditoriaservice';

/** Zona compartida por los filtros y la presentación de fechas. */
const ZONA_HORARIA = 'America/Bogota';

/** Presenta el día del evento con el formato habitual de Colombia. */
const FORMATO_FECHA = new Intl.DateTimeFormat('es-CO', {
  timeZone: ZONA_HORARIA,
  day: '2-digit',
  month: 'short',
  year: 'numeric',
});

/** Presenta la hora del evento con segundos y formato de 24 horas. */
const FORMATO_HORA = new Intl.DateTimeFormat('es-CO', {
  timeZone: ZONA_HORARIA,
  hour: '2-digit',
  minute: '2-digit',
  second: '2-digit',
  hourCycle: 'h23',
});

/** Permite construir una fecha comparable con los campos de tipo date. */
const FORMATO_FILTRO = new Intl.DateTimeFormat('en', {
  timeZone: ZONA_HORARIA,
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
});

/** Nombres comerciales de los planes que se muestran junto al actor. */
const PLANES: Readonly<Record<PlanAdministracion, string>> = {
  FREE: 'Free Pass',
  ORIA: 'orIA',
  INFINITIA: 'InfinitIA',
};

/**
 * Prepara un evento para su presentación sin modificar el registro recibido.
 *
 * Las fechas inválidas se identifican y no se incluyen en filtros por fecha.
 * Los procesos del sistema se presentan sin un plan de usuario.
 *
 * @param registro Evento recibido mediante el servicio de auditoría.
 */
function prepararFila(registro: RegistroAuditoria) {
  const fecha = new Date(registro.fecha);
  const valida = Number.isFinite(fecha.getTime());
  const partes = valida ? FORMATO_FILTRO.formatToParts(fecha) : [];
  const anio = partes.find((parte) => parte.type === 'year')?.value ?? '';
  const mes = partes.find((parte) => parte.type === 'month')?.value ?? '';
  const dia = partes.find((parte) => parte.type === 'day')?.value ?? '';
  const esSistema = registro.actor.tipo === 'SYSTEM';
  const plan = esSistema ? null : registro.actor.planActual;

  return {
    ...registro,

    marcaTiempo: valida ? fecha.getTime() : null,
    fechaFiltro: valida ? `${anio}-${mes}-${dia}` : '',
    fechaTexto: valida ? FORMATO_FECHA.format(fecha) : 'Fecha no disponible',
    horaTexto: valida ? FORMATO_HORA.format(fecha) : 'Hora no disponible',
    fechaIso: valida ? fecha.toISOString() : null,
    nombreActor: esSistema ? 'Sistema' : (registro.actor.nombre ?? 'Usuario no disponible'),
    correoActor: esSistema
      ? 'Proceso automático'
      : (registro.actor.correo ?? 'Correo no disponible'),

    planColor: plan ?? 'SIN_PLAN',
    planTexto: esSistema
      ? 'Sin plan de usuario'
      : plan === null
        ? 'Plan no disponible'
        : PLANES[plan],
  };
}

/**
 * Página de consulta de auditoría de TraducIA.
 *
 * Presenta los eventos más recientes primero y permite buscar
 * por identificador, usuario, correo o acción.
 *
 * Los filtros por fecha, actor y resultado se aplican sobre
 * la lista cargada. La paginación organiza los resultados
 * disponibles en el navegador.
 *
 * Cada actor se identifica con el color de su plan actual.
 * El diálogo permite revisar información adicional sin
 * modificar los eventos registrados.
 *
 * Sin un proveedor de AuditoriaService, la vista permanece vacía.
 */
@Component({
  selector: 'app-auditoriacomponent',
  standalone: false,
  templateUrl: './auditoriacomponent.html',
  styleUrl: './auditoriacomponent.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Auditoriacomponent implements OnInit {
  private readonly servicio = inject(AuditoriaService, { optional: true });
  private readonly destroyRef = inject(DestroyRef);

  /** Registros originales, conservados sin modificar sus objetos. */
  private readonly registros = signal<readonly RegistroAuditoria[]>([]);
  private readonly paginaSolicitada = signal(1);

  /**
   * Diálogo nativo que mantiene el foco dentro del detalle
   * mientras permanece abierto.
   */
  @ViewChild('detalle', { static: true })
  private detalle?: ElementRef<HTMLDialogElement>;

  /** Indica si existe una implementación de consulta registrada. */
  protected readonly consultaDisponible = this.servicio !== null;
  protected readonly cargando = signal(false);
  protected readonly error = signal('');
  protected readonly aviso = signal('');

  /** Criterios independientes de los datos originales. */
  protected readonly busqueda = signal('');
  protected readonly desde = signal('');
  protected readonly hasta = signal('');
  protected readonly tipoActor = signal<TipoActorAuditoria | ''>('');
  protected readonly resultado = signal<ResultadoAuditoria | ''>('');
  protected readonly tamanoPagina = signal(10);
  protected readonly seleccionado = signal<ReturnType<typeof prepararFila> | null>(null);
  protected readonly tiposActor: Readonly<Record<TipoActorAuditoria, string>> = {
    USER: 'Usuario',
    ADMIN: 'Administrador',
    SYSTEM: 'Sistema',
  };

  protected readonly resultados: Readonly<Record<ResultadoAuditoria, string>> = {
    SUCCESS: 'Exitoso',
    FAILURE: 'Fallido',
    PENDING: 'Pendiente',
  };

  /** Detecta un rango cuyo inicio sea posterior a su fecha final. */
  protected readonly rangoInvalido = computed(
    () => this.desde() !== '' && this.hasta() !== '' && this.desde() > this.hasta(),
  );

  protected readonly hayFiltros = computed(
    () =>
      this.busqueda() !== '' ||
      this.desde() !== '' ||
      this.hasta() !== '' ||
      this.tipoActor() !== '' ||
      this.resultado() !== '',
  );

  /**
   * Ordena los eventos desde el más reciente.
   * Los registros con fecha no interpretable permanecen al final.
   */
  private readonly filas = computed(() =>
    this.registros()
      .map(prepararFila)
      .sort((a, b) => {
        if (a.marcaTiempo === null) {
          return b.marcaTiempo === null ? 0 : 1;
        }

        return b.marcaTiempo === null ? -1 : b.marcaTiempo - a.marcaTiempo;
      }),
  );

  /** Combina la búsqueda y todos los filtros seleccionados. */
  protected readonly filtrados = computed(() => {
    if (this.rangoInvalido()) {
      return [];
    }

    const texto = this.busqueda().trim().toLocaleLowerCase('es');
    const desde = this.desde();
    const hasta = this.hasta();
    const tipo = this.tipoActor();
    const resultado = this.resultado();

    return this.filas().filter((fila) => {
      const contenido = [fila.id, fila.nombreActor, fila.correoActor, fila.accion]
        .join(' ')
        .toLocaleLowerCase('es');

      const fechaConocida = fila.fechaFiltro !== '';

      return (
        contenido.includes(texto) &&
        (desde === '' || (fechaConocida && fila.fechaFiltro >= desde)) &&
        (hasta === '' || (fechaConocida && fila.fechaFiltro <= hasta)) &&
        (tipo === '' || fila.actor.tipo === tipo) &&
        (resultado === '' || fila.resultado === resultado)
      );
    });
  });

  protected readonly total = computed(() => this.filtrados().length);

  protected readonly totalPaginas = computed(() =>
    Math.max(1, Math.ceil(this.total() / this.tamanoPagina())),
  );

  protected readonly pagina = computed(() =>
    Math.min(this.paginaSolicitada(), this.totalPaginas()),
  );

  protected readonly inicio = computed(() =>
    this.total() === 0 ? 0 : (this.pagina() - 1) * this.tamanoPagina() + 1,
  );

  protected readonly fin = computed(() =>
    Math.min(this.pagina() * this.tamanoPagina(), this.total()),
  );

  protected readonly visibles = computed(() =>
    this.filtrados().slice((this.pagina() - 1) * this.tamanoPagina(), this.fin()),
  );

  /** Inicia la consulta cuando Angular prepara el componente. */
  ngOnInit(): void {
    this.cargarRegistros();
  }

  /**
   * Consulta los eventos disponibles.
   *
   * Si la petición falla, conserva la última lista correcta
   * e informa que no fue posible actualizarla.
   */
  protected cargarRegistros(): void {
    const servicio = this.servicio;

    if (servicio === null || this.cargando()) {
      return;
    }

    this.cargando.set(true);
    this.error.set('');
    this.aviso.set('');

    servicio
      .listar()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.cargando.set(false)),
      )
      .subscribe({
        next: (registros) => {
          this.registros.set([...registros]);
          this.paginaSolicitada.set(1);

          this.aviso.set('El registro de auditoría se actualizó correctamente.');
        },
        error: () => {
          this.error.set(
            this.registros().length > 0
              ? 'No fue posible actualizar la auditoría. Se conservan los datos de la última consulta correcta.'
              : 'No fue posible consultar la auditoría. Inténtalo nuevamente.',
          );
        },
      });
  }

  /** Cambia la búsqueda y vuelve a la primera página. */
  protected buscar(valor: string): void {
    this.busqueda.set(valor);
    this.paginaSolicitada.set(1);
  }

  /** Incluye los eventos ocurridos desde el día seleccionado. */
  protected cambiarDesde(valor: string): void {
    this.desde.set(valor);
    this.paginaSolicitada.set(1);
  }

  /** Incluye los eventos ocurridos hasta el día seleccionado. */
  protected cambiarHasta(valor: string): void {
    this.hasta.set(valor);
    this.paginaSolicitada.set(1);
  }

  /** Acepta únicamente los tipos de actor disponibles. */
  protected cambiarTipoActor(valor: string): void {
    if (valor === '' || valor === 'USER' || valor === 'ADMIN' || valor === 'SYSTEM') {
      this.tipoActor.set(valor);
      this.paginaSolicitada.set(1);
    }
  }

  /** Acepta únicamente resultados definidos por el contrato. */
  protected cambiarResultado(valor: string): void {
    if (valor === '' || valor === 'SUCCESS' || valor === 'FAILURE' || valor === 'PENDING') {
      this.resultado.set(valor);
      this.paginaSolicitada.set(1);
    }
  }

  /** Restablece los criterios sin repetir la consulta al servidor. */
  protected limpiarFiltros(): void {
    this.busqueda.set('');
    this.desde.set('');
    this.hasta.set('');
    this.tipoActor.set('');
    this.resultado.set('');
    this.paginaSolicitada.set(1);
  }

  /** Ajusta la cantidad de filas a una opción permitida. */
  protected cambiarTamano(valor: string): void {
    const cantidad = Number(valor);

    if ([10, 25, 50].includes(cantidad)) {
      this.tamanoPagina.set(cantidad);
      this.paginaSolicitada.set(1);
    }
  }

  /** Mantiene la navegación dentro de las páginas disponibles. */
  protected irAPagina(numero: number): void {
    this.paginaSolicitada.set(Math.max(1, Math.min(numero, this.totalPaginas())));
  }

  /** Muestra el detalle sin cambiar el evento original. */
  protected verDetalle(fila: ReturnType<typeof prepararFila>): void {
    this.seleccionado.set(fila);

    const dialogo = this.detalle?.nativeElement;

    if (dialogo !== undefined && !dialogo.open) {
      dialogo.showModal();
    }
  }

  /** Cierra el detalle y restaura el foco al control de origen. */
  protected cerrarDetalle(): void {
    this.detalle?.nativeElement.close();
    this.seleccionado.set(null);
  }

  /** Sincroniza la selección al cerrar el diálogo mediante Escape. */
  protected cancelarDetalle(evento: Event): void {
    evento.preventDefault();
    this.cerrarDetalle();
  }
}
