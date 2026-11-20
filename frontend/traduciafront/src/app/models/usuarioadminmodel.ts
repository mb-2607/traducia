import type { Rol } from './usermodel';

/** Planes identificados por el backend para las cuentas de usuario. */
export type PlanAdministracion = "FREE" | "ORIA" | "INFINITIA";

/** Estados que presenta la interfaz a partir de las condiciones de acceso. */
export type EstadoUsuario = "activo" | "deshabilitado" | "bloqueado";

/**
 * Datos de un usuario que puede consultar el administrador.
 * Conserva los nombres de la entidad del backend y excluye las credenciales.
 * Un administrador puede no tener un plan asignado.
 */
export interface UsuarioAdministracion {
  readonly id: number;
  readonly username: string;
  readonly email: string;
  readonly role: Rol;
  readonly planType: PlanAdministracion | null;
  readonly enabled: boolean;
  readonly accountNonLocked: boolean;
}

/** Datos personales que admite el formulario de edición. */
export interface CambiosUsuario {
  readonly username: string;
  readonly email: string;
}

/** Operaciones que requieren una confirmación expresa del administrador. */
export type AccionUsuario =
  | "actualizar"
  | "bloquear"
  | "desbloquear"
  | "habilitar"
  | "deshabilitar"
  | "eliminar";

/** Solicitud de administración. La actualización exige sus nuevos valores. */
export type SolicitudAdministracion =
  | {
      readonly accion: "actualizar";
      readonly usuarioId: number;
      readonly cambios: CambiosUsuario;
    }
  | {
      readonly accion: Exclude<AccionUsuario, "actualizar">;
      readonly usuarioId: number;
    };

/** Resultado confirmado por el servidor después de una operación. */
export type ResultadoAdministracion =
  | { readonly tipo: "actualizado"; readonly usuario: UsuarioAdministracion }
  | { readonly tipo: "eliminado"; readonly usuarioId: number };
