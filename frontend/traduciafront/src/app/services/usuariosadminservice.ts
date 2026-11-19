import type { Observable } from "rxjs";
import type {
  ResultadoAdministracion,
  SolicitudAdministracion,
  UsuarioAdministracion,
} from "../models/usuarioadminmodel";

/**
 * Contrato de acceso a la administración de usuarios.
 *
 * La implementación HTTP se registrará cuando estén definidos los endpoints.
 * Cada operación debe emitir su respuesta una vez y completar el observable.
 * Los errores del servidor se propagan para que la pantalla informe el fallo.
 * El servidor debe comprobar los permisos y las reglas de cada operación.
 */
export abstract class UsuariosAdminService {
  /** Obtiene los usuarios visibles para el administrador autenticado. */
  abstract listar(): Observable<readonly UsuarioAdministracion[]>;

  /**
   * Ejecuta una solicitud y devuelve el resultado confirmado por el servidor.
   * Una actualización debe devolver el usuario con sus valores definitivos.
   *
   * @param solicitud Operación autorizada y sus datos.
   */
  abstract ejecutar(
    solicitud: SolicitudAdministracion,
  ): Observable<ResultadoAdministracion>;
}
