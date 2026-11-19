import {
  Component,
  DestroyRef,
  ElementRef,
  OnInit,
  ViewChild,
  computed,
  inject,
  signal,
} from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import type {
  AccionUsuario,
  EstadoUsuario,
  PlanAdministracion,
  ResultadoAdministracion,
  SolicitudAdministracion,
  UsuarioAdministracion,
} from '../../models/usuarioadminmodel';
import { UsuariosAdminService } from '../../services/usuariosadminservice';

/** Texto de la confirmación y del resultado de cada operación. */
const ACCIONES: Readonly<Record<AccionUsuario, { titulo: string; aviso: string; exito: string }>> =
  {
    actualizar: {
      titulo: 'Guardar cambios',
      aviso: 'Se actualizarán el nombre de usuario y el correo electrónico.',
      exito: 'Los datos del usuario se actualizaron correctamente.',
    },
    bloquear: {
      titulo: 'Bloquear usuario',
      aviso: 'El usuario no podrá iniciar sesión mientras permanezca bloqueado.',
      exito: 'El usuario se bloqueó correctamente.',
    },
    desbloquear: {
      titulo: 'Desbloquear usuario',
      aviso: 'Se retirará el bloqueo. La cuenta también debe estar habilitada para acceder.',
      exito: 'El usuario se desbloqueó correctamente.',
    },
    habilitar: {
      titulo: 'Habilitar cuenta',
      aviso: 'Se habilitará la cuenta. Esta acción no retira un bloqueo existente.',
      exito: 'La cuenta se habilitó correctamente.',
    },
    deshabilitar: {
      titulo: 'Deshabilitar cuenta',
      aviso: 'La cuenta dejará de estar habilitada para acceder.',
      exito: 'La cuenta se deshabilitó correctamente.',
    },
    eliminar: {
      titulo: 'Eliminar usuario',
      aviso:
        'Esta operación solicitará la eliminación del usuario. No se puede deshacer desde esta pantalla.',
      exito: 'El usuario se eliminó correctamente.',
    },
  };

/**
 * Obtiene el estado visible de una cuenta.
 * El bloqueo tiene prioridad cuando una cuenta también está deshabilitada.
 */
function obtenerEstado(usuario: UsuarioAdministracion): EstadoUsuario {
  if (!usuario.accountNonLocked) {
    return 'bloqueado';
  }
  return usuario.enabled ? 'activo' : 'deshabilitado';
}

/**
 * Página de administración de usuarios de TraducIA.
 *
 * Permite consultar, buscar y filtrar usuarios, recorrer sus resultados y
 * revisar los datos de una cuenta antes de solicitar una modificación.
 * Las operaciones requieren confirmación y actualizan la tabla únicamente
 * después de recibir una respuesta correcta del servicio.
 *
 * Sin un proveedor de UsuariosAdminService, la pantalla permanece vacía y
 * desactiva la gestión. No genera usuarios ni confirma operaciones locales.
 * La búsqueda y la paginación se aplican a la lista cargada en el navegador.
 */
@Component({
  selector: 'app-listausuarioscomponent',
  standalone: false,
  templateUrl: './listausuarioscomponent.html',
  styleUrl: './listausuarioscomponent.css',
})
export class Listausuarioscomponent implements OnInit {
  private readonly servicio = inject(UsuariosAdminService, { optional: true });
  private readonly destroyRef = inject(DestroyRef);
  private readonly constructorFormularios = inject(FormBuilder);

  /** Diálogo nativo con gestión de foco y navegación mediante teclado. */
  @ViewChild('modal', { static: true })
  private modal?: ElementRef<HTMLDialogElement>;

  /** Indica si está registrada la implementación de acceso al backend. */
  protected readonly gestionDisponible = this.servicio !== null;

  /** Datos originales recibidos del servicio, sin modificar sus objetos. */
  private readonly usuarios = signal<readonly UsuarioAdministracion[]>([]);

  protected readonly cargando = signal(false);
  protected readonly procesando = signal(false);
  protected readonly error = signal('');
  protected readonly exito = signal('');
  protected readonly errorModal = signal('');
  protected readonly busqueda = signal('');
  protected readonly filtroPlan = signal<PlanAdministracion | ''>('');
  protected readonly filtroEstado = signal<EstadoUsuario | ''>('');
  protected readonly tamanoPagina = signal(10);
  private readonly paginaSolicitada = signal(1);

  protected readonly seleccionado = signal<UsuarioAdministracion | null>(null);
  protected readonly modo = signal<'detalle' | 'editar' | 'confirmar'>('detalle');
  protected readonly solicitud = signal<SolicitudAdministracion | null>(null);

  /** Etiquetas de presentación independientes de los valores del backend. */
  protected readonly planes: Readonly<Record<PlanAdministracion, string>> = {
    FREE: 'Gratuito',
    ORIA: 'OrIA',
    INFINITIA: 'InfinitIA',
  };
  protected readonly estados: Readonly<Record<EstadoUsuario, string>> = {
    activo: 'Activo',
    deshabilitado: 'Deshabilitado',
    bloqueado: 'Bloqueado',
  };
  protected readonly estadoDe = obtenerEstado;

  /** Formulario limitado a los datos personales editables de la cuenta. */
  protected readonly formulario = this.constructorFormularios.nonNullable.group({
    username: ['', [Validators.required, Validators.maxLength(100), Validators.pattern(/\S/)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(254)]],
  });

  /** Resultados que coinciden con la búsqueda y los filtros seleccionados. */
  protected readonly filtrados = computed(() => {
    const texto = this.busqueda().trim().toLocaleLowerCase('es');
    const plan = this.filtroPlan();
    const estado = this.filtroEstado();
    return this.usuarios().filter((usuario) => {
      const coincideTexto =
        usuario.username.toLocaleLowerCase('es').includes(texto) ||
        usuario.email.toLocaleLowerCase('es').includes(texto);
      return (
        coincideTexto &&
        (plan === '' || usuario.planType === plan) &&
        (estado === '' || obtenerEstado(usuario) === estado)
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
    this.filtrados().slice(this.inicio() === 0 ? 0 : this.inicio() - 1, this.fin()),
  );
  protected readonly detalleAccion = computed(() => {
    const solicitud = this.solicitud();
    return solicitud === null ? null : ACCIONES[solicitud.accion];
  });
  protected readonly tituloModal = computed(() => {
    if (this.modo() === 'editar') {
      return 'Editar usuario';
    }
    return this.modo() === 'detalle'
      ? 'Detalle del usuario'
      : (this.detalleAccion()?.titulo ?? 'Confirmar operación');
  });

  /** Consulta los usuarios cuando Angular termina de preparar la página. */
  ngOnInit(): void {
    this.cargarUsuarios();
  }

  /** Recarga la tabla y comunica los fallos sin sustituir datos por ejemplos. */
  protected cargarUsuarios(): void {
    const servicio = this.servicio;
    if (servicio === null || this.cargando() || this.procesando()) {
      return;
    }
    this.error.set('');
    this.exito.set('');
    this.cargando.set(true);
    servicio
      .listar()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.cargando.set(false)),
      )
      .subscribe({
        next: (usuarios) => {
          this.usuarios.set([...usuarios]);
          this.paginaSolicitada.set(1);
        },
        error: () => this.error.set('No fue posible cargar los usuarios. Inténtalo nuevamente.'),
      });
  }

  /** Actualiza la búsqueda y regresa al primer grupo de resultados. */
  protected buscar(valor: string): void {
    this.busqueda.set(valor);
    this.paginaSolicitada.set(1);
  }

  /** Acepta únicamente planes conocidos por la pantalla. */
  protected cambiarPlan(valor: string): void {
    if (valor === '' || valor === 'FREE' || valor === 'ORIA' || valor === 'INFINITIA') {
      this.filtroPlan.set(valor);
      this.paginaSolicitada.set(1);
    }
  }

  /** Acepta únicamente estados de acceso disponibles en el filtro. */
  protected cambiarEstado(valor: string): void {
    if (valor === '' || valor === 'activo' || valor === 'deshabilitado' || valor === 'bloqueado') {
      this.filtroEstado.set(valor);
      this.paginaSolicitada.set(1);
    }
  }

  /** Ajusta el tamaño de página a una de las cantidades permitidas. */
  protected cambiarTamano(valor: string): void {
    const cantidad = Number(valor);
    if ([10, 25, 50].includes(cantidad)) {
      this.tamanoPagina.set(cantidad);
      this.paginaSolicitada.set(1);
    }
  }

  /** Mantiene la navegación dentro del rango de páginas disponible. */
  protected irAPagina(numero: number): void {
    this.paginaSolicitada.set(Math.max(1, Math.min(numero, this.totalPaginas())));
  }

  /** Abre la información de la cuenta sin modificar sus valores. */
  protected verDetalle(usuario: UsuarioAdministracion): void {
    this.seleccionado.set(usuario);
    this.modo.set('detalle');
    this.mostrarModal();
  }

  /** Carga los datos actuales de la cuenta en el formulario de edición. */
  protected editar(usuario: UsuarioAdministracion): void {
    if (!this.gestionDisponible || this.procesando()) {
      return;
    }
    this.seleccionado.set(usuario);
    this.formulario.reset({ username: usuario.username, email: usuario.email });
    this.modo.set('editar');
    this.mostrarModal();
  }

  /** Valida los datos antes de preparar la confirmación de la actualización. */
  protected prepararEdicion(): void {
    const usuario = this.seleccionado();
    const valores = this.formulario.getRawValue();
    const cambios = {
      username: valores.username.trim(),
      email: valores.email.trim(),
    };
    this.formulario.setValue(cambios);
    this.formulario.markAllAsTouched();
    if (usuario === null || this.formulario.invalid || this.procesando()) {
      return;
    }
    this.solicitud.set({
      accion: 'actualizar',
      usuarioId: usuario.id,
      cambios,
    });
    this.errorModal.set('');
    this.modo.set('confirmar');
  }

  /** Prepara una operación de acceso o eliminación sin ejecutarla todavía. */
  protected prepararAccion(
    usuario: UsuarioAdministracion,
    accion: Exclude<AccionUsuario, 'actualizar'>,
  ): void {
    if (!this.gestionDisponible || this.procesando()) {
      return;
    }
    this.seleccionado.set(usuario);
    this.solicitud.set({ accion, usuarioId: usuario.id });
    this.modo.set('confirmar');
    this.mostrarModal();
  }

  /** Envía la operación confirmada y aplica solamente la respuesta del servicio. */
  protected confirmar(): void {
    const servicio = this.servicio;
    const solicitud = this.solicitud();
    if (servicio === null || solicitud === null || this.procesando()) {
      return;
    }
    this.procesando.set(true);
    this.errorModal.set('');
    this.error.set('');
    this.exito.set('');
    servicio
      .ejecutar(solicitud)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.procesando.set(false)),
      )
      .subscribe({
        next: (resultado) => {
          this.aplicarResultado(resultado);
          this.exito.set(ACCIONES[solicitud.accion].exito);
          this.finalizarModal();
        },
        error: () =>
          this.errorModal.set(
            'No fue posible completar la operación. Comprueba el estado de la cuenta antes de reintentar.',
          ),
      });
  }

  /** Permite cancelar mientras no exista una operación en curso. */
  protected cerrarModal(): void {
    if (!this.procesando()) {
      this.finalizarModal();
    }
  }

  /** Impide que Escape cierre la confirmación durante una petición pendiente. */
  protected cancelarModal(evento: Event): void {
    evento.preventDefault();
    this.cerrarModal();
  }

  /** Abre el diálogo sin duplicar la apertura de una ventana ya visible. */
  private mostrarModal(): void {
    this.errorModal.set('');
    const dialogo = this.modal?.nativeElement;
    if (dialogo !== undefined && !dialogo.open) {
      dialogo.showModal();
    }
  }

  /** Cierra la ventana y descarta la selección y la solicitud pendientes. */
  private finalizarModal(): void {
    this.modal?.nativeElement.close();
    this.seleccionado.set(null);
    this.solicitud.set(null);
    this.errorModal.set('');
  }

  /** Sustituye o retira la fila que el servidor confirmó como modificada. */
  private aplicarResultado(resultado: ResultadoAdministracion): void {
    this.usuarios.update((usuarios) =>
      resultado.tipo === 'eliminado'
        ? usuarios.filter((usuario) => usuario.id !== resultado.usuarioId)
        : usuarios.map((usuario) =>
            usuario.id === resultado.usuario.id ? resultado.usuario : usuario,
          ),
    );
  }
}
