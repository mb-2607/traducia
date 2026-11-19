import type { PlanId } from './planmodel';
import type { Rol } from './usermodel';

/** Datos públicos de la cuenta que se muestran en el perfil. No incluye el JWT. */
export interface PerfilUsuario {
  readonly nombre: string;
  readonly correo: string;
  readonly ciudad: string | null;
  readonly edad: number | null;
  readonly fotoUrl: string | null;
  readonly rol: Rol;
  readonly plan: PlanId | null;
}

/** Campos editables. El correo, el rol y el plan no se modifican desde este formulario. */
export interface CambiosPerfil {
  readonly nombre: string;
  readonly ciudad: string | null;
  readonly edad: number | null;
}

/**
 * Solicitud de actualización del perfil.
 * foto=null conserva la imagen existente salvo que quitarFoto sea verdadero.
 * La implementación utiliza FormData cuando existe un archivo nuevo.
 */
export interface GuardarPerfil {
  readonly datos: CambiosPerfil;
  readonly foto: File | null;
  readonly quitarFoto: boolean;
}
