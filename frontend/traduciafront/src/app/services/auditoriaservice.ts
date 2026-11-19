import type { Observable } from 'rxjs';
import type { RegistroAuditoria } from '../models/auditoriamodel';

/**
 * Contrato de consulta de auditoría.
 *
 * La implementación HTTP se registrará cuando estén definidos
 * los endpoints del backend.
 *
 * Cada consulta debe emitir una lista completa, finalizar
 * y propagar los errores que impidan obtener los registros.
 *
 * El backend comprueba los permisos y devuelve el plan actual
 * de cada cuenta.
 *
 * La pantalla no modifica ni elimina eventos del registro.
 */
export abstract class AuditoriaService {
  /**
   * Obtiene los eventos que puede consultar
   * el administrador autenticado.
   *
   * @returns Lista de eventos disponibles para su consulta.
   */
  abstract listar(): Observable<readonly RegistroAuditoria[]>;
}
