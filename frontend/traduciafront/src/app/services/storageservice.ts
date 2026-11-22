import { DOCUMENT } from '@angular/common';
import { Injectable, inject } from '@angular/core';

/**
 * Servicio de almacenamiento local.
 *
 * Envuelve `localStorage` para guardar y leer valores como JSON, de modo que
 * el resto de la aplicación trabaje con objetos tipados en lugar de cadenas.
 * Lo usa, por ejemplo, {@link AuthService} para persistir el usuario y el token
 * de la sesión.
 *
 * Está disponible globalmente (`providedIn: 'root'`).
 */
@Injectable({ providedIn: 'root' })
export class StorageService {
  private readonly documento = inject(DOCUMENT);

  /**
   * Obtiene el almacenamiento local de la ventana asociada al documento.
   *
   * @returns El almacenamiento local o `undefined` si no hay una ventana.
   */
  private get almacenamiento(): Storage | undefined {
    return this.documento.defaultView?.localStorage;
  }

  /**
   * Lee un valor del almacenamiento y lo deserializa desde JSON.
   *
   * Si la clave no existe, o el contenido guardado no es un JSON válido,
   * devuelve `null` en lugar de lanzar un error.
   *
   * @typeParam T Tipo esperado del valor almacenado (no se valida en tiempo de ejecución).
   * @param key Clave bajo la cual se guardó el valor.
   * @returns El valor deserializado o `null` si no existe o no se pudo leer.
   */
  get<T>(key: string): T | null {
    try {
      const value = this.almacenamiento?.getItem(key);
      return value ? (JSON.parse(value) as T) : null;
    } catch {
      return null;
    }
  }

  /**
   * Guarda un valor en el almacenamiento serializado como JSON.
   * Si la clave ya existe, se sobrescribe.
   *
   * @typeParam T Tipo del valor a guardar.
   * @param key Clave con la que se almacenará el valor.
   * @param value Valor a guardar; debe ser serializable con `JSON.stringify`.
   */
  set<T>(key: string, value: T): void {
    this.almacenamiento?.setItem(key, JSON.stringify(value));
  }

  /**
   * Elimina del almacenamiento el valor asociado a una clave.
   * Si la clave no existe, no hace nada.
   *
   * @param key Clave del valor a eliminar.
   */
  remove(key: string): void {
    this.almacenamiento?.removeItem(key);
  }

  /**
   * Elimina **todas** las claves del `localStorage` del dominio,
   * incluidas las que no haya creado este servicio.
   */
  clear(): void {
    this.almacenamiento?.clear();
  }
}
