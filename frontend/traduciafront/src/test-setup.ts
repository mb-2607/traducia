/**
 * ⚠️ NO BORRAR. Sin este archivo fallan todas las pruebas unitarias (`npx ng test`).
 *
 * Configuración común de las pruebas unitarias (se registra en angular.json → test.setupFiles).
 * La aplicación no lo usa; solo lo carga el runner de pruebas antes de los .spec.ts.
 *
 * Los componentes del proyecto no son standalone. El runner de pruebas solo compila
 * los archivos .spec.ts y lo que estos importan, así que se importa AppModule para
 * que el compilador conozca el módulo que declara cada componente. Si se borra,
 * aparecen errores como "Can't bind to 'formGroup'" o "'router-outlet' is not a known element".
 */
import './app/app-module';

// jsdom no implementa showModal() ni close() de <dialog>. Se agrega el comportamiento
// mínimo que usan los modales de la aplicación: abrir y cerrar.
if (typeof HTMLDialogElement !== 'undefined' && !HTMLDialogElement.prototype.showModal) {
  HTMLDialogElement.prototype.showModal = function (this: HTMLDialogElement) {
    this.open = true;
  };
  HTMLDialogElement.prototype.close = function (this: HTMLDialogElement) {
    this.open = false;
  };
}
