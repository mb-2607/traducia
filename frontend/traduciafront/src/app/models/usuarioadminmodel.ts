import type { PlanApi, Rol, UserResponseDTO } from './usermodel';

/** Planes reconocidos por el backend. */
export type PlanAdministracion = PlanApi;

/** Estados utilizados por los filtros de la interfaz. */
export type EstadoUsuario = 'activo' | 'deshabilitado' | 'bloqueado';

/**
 * Respuesta actual del backend al consultar usuarios.
 *
 * Reutiliza UserResponseDTO:
 * nombre, correo, planId y rol.
 *
 * Actualmente no incluye identificador ni estados de cuenta.
 */
export type UsuarioAdministracionResponseDTO = UserResponseDTO;

/**
 * Usuario utilizado por la tabla y los formularios administrativos.
 *
 * Es un modelo de presentación, diferente del DTO del backend.
 *
 * Para construirlo con datos reales, el backend deberá proporcionar
 * también id, enabled y accountNonLocked.
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

/**
 * Datos enviados para crear un usuario desde la administración.
 *
 * Coincide con AdminCreateUserRequestDTO del backend.
 */
export interface CrearUsuarioRequestDTO {
  readonly nombre: string;
  readonly correo: string;
  readonly password: string;
  readonly rol: Rol;
  readonly planId: PlanAdministracion;
}

/**
 * Datos enviados para actualizar un usuario.
 *
 * Coincide con UpdateUserRequestDTO del backend.
 * Los cuatro campos son obligatorios.
 *
 * El identificador del usuario se envía en la ruta,
 * no dentro de este objeto.
 */
export interface ActualizarUsuarioRequestDTO {
  readonly nombre: string;
  readonly correo: string;
  readonly rol: Rol;
  readonly planId: PlanAdministracion;
}

/**
 * Datos enviados por el administrador para cambiar
 * la contraseña de un usuario.
 *
 * Coincide con AdminChangePasswordRequestDTO del backend.
 */
export interface CambiarPasswordUsuarioRequestDTO {
  readonly nuevaPassword: string;
}

/**
 * Campos editables del formulario administrativo actual.
 *
 * Al construir ActualizarUsuarioRequestDTO, el servicio deberá
 * incorporar también el rol y el plan del usuario seleccionado.
 */
export interface CambiosUsuario {
  readonly username: string;
  readonly email: string;
}

/**
 * Acciones contempladas por la interfaz actual.
 *
 * El backend revisado implementa actualizar y eliminar.
 * Bloquear, desbloquear, habilitar y deshabilitar requieren
 * endpoints adicionales antes de poder ejecutarse.
 */
export type AccionUsuario =
  'actualizar' | 'bloquear' | 'desbloquear' | 'habilitar' | 'deshabilitar' | 'eliminar';

/**
 * Solicitud interna generada por el componente.
 *
 * No representa directamente el cuerpo de una petición HTTP.
 * El servicio deberá convertirla a la operación correspondiente.
 */
export type SolicitudAdministracion =
  | {
      readonly accion: 'actualizar';
      readonly usuarioId: number;
      readonly cambios: CambiosUsuario;
    }
  | {
      readonly accion: Exclude<AccionUsuario, 'actualizar'>;
      readonly usuarioId: number;
    };

/**
 * Resultado interno que el servicio entregará al componente
 * después de confirmar una operación con el backend.
 *
 * No coincide directamente con las respuestas HTTP del servidor.
 */
export type ResultadoAdministracion =
  | {
      readonly tipo: 'actualizado';
      readonly usuario: UsuarioAdministracion;
    }
  | {
      readonly tipo: 'eliminado';
      readonly usuarioId: number;
    };
