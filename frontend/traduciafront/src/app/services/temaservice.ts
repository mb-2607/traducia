import {inject, Injectable} from '@angular/core';
import {StorageService} from './storageservice';
import {BehaviorSubject} from 'rxjs';
import {DOCUMENT} from '@angular/common';

/**
 * Clave utilizada para almacenar la preferencia del tema
 * visual en el almacenamiento local.
 */
const TEMA_KEY = 'tema';

/**
 * Servicio encargado de gestionar el tema visual de la aplicación.
 *
 * Permite consultar, modificar y aplicar el tema claro u oscuro.
 * Mantiene el estado reactivo mediante un BehaviorSubject y
 * persiste la preferencia del usuario mediante StorageService.
 */
@Injectable({
  providedIn: 'root'
})
export class TemaService {

  /**
   * Referencia al documento actual, utilizada para modificar los atributos del elemento raíz del documento HTML.
   */
  private readonly documento: Document = inject(DOCUMENT);

  /**
   * Estado reactivo que indica si el tema oscuro está activo.
   *
   * Su valor inicial se obtiene del almacenamiento local.
   */
  private temaOscuro :BehaviorSubject<boolean>;

  /**
   * Inicializa el servicio recuperando la preferencia de tema almacenada y aplicándola al documento.
   *
   * Si no existe una preferencia guardada, utiliza el tema oscuro como valor predeterminado.
   *
   * @param storageService Servicio encargado de gestionar el almacenamiento de las preferencias del usuario.
   */
  constructor(private storageService: StorageService) {
    this.temaOscuro = new BehaviorSubject<boolean>(storageService.get(TEMA_KEY) ?? true);
    this.aplicarTema(this.temaOscuro.value);
  }

  /**
   * Proporciona acceso de solo lectura al estado del tema.
   *
   * @returns Observable que emite el estado actual del tema y sus posteriores modificaciones.
   */
  getTemaOscuroAsObservable(){
    return this.temaOscuro.asObservable();
  }

  /**
   * Actualiza el estado del tema visual, guarda la preferencia del usuario y aplica el cambio al documento.
   *
   * @param valor Indica si se debe activar el tema oscuro.
   * True para oscuro y false para claro.
   */
  setTemaOscuro(valor: boolean) {
    this.temaOscuro.next(valor);
    this.storageService.set(TEMA_KEY, valor);
    this.aplicarTema(this.temaOscuro.value);
  }

  /**
   * Aplica el tema visual modificando el atributo 'data-tema' del elemento raíz del documento HTML.
   *
   * @param oscuro Indica si se debe aplicar el tema oscuro.
   * True para oscuro y false para claro.
   */
  aplicarTema(oscuro :boolean) : void {
    this.documento.documentElement.dataset['tema'] = oscuro ? 'oscuro' : 'claro';
  }
}
