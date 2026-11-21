import { Component, EventEmitter, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-terminocomponent',
  standalone: false,
  templateUrl: './terminocomponent.html',
  styleUrl: './terminocomponent.css',
})
/**
 * Componente modal de terminos y condiciones de TraducIA.
 */
export class Terminocomponent implements OnInit {
  @Output() terminosAceptadosChange = new EventEmitter<boolean>();

  activo: boolean = false;
  error: boolean = false;
  animandoExit: boolean = false;
  animandoShake: boolean = false;

  /**
   * Consulta en localStorage si ya se aceptaron los terminos y define el estado inicial del modal.
   */
  ngOnInit(): void {
    const yaAcepto = localStorage.getItem('traduciaTerminosAceptados') === 'true';
    this.activo = !yaAcepto;
    this.terminosAceptadosChange.emit(yaAcepto);
  }

  /**
   * Guarda la aceptacion de los terminos, emite el cambio y cierra el modal con animacion de salida.
   */
  aceptar(): void {
    this.error = false;
    this.animandoShake = false;
    localStorage.setItem('traduciaTerminosAceptados', 'true');
    this.animandoExit = true;
    this.terminosAceptadosChange.emit(true);

    setTimeout(() => {
      this.activo = false;
      this.animandoExit = false;
    }, 350);
  }

  /**
   * Muestra el mensaje de error, limpia la aceptacion guardada y activa la animacion de sacudida.
   */
  rechazar(): void {
    this.error = true;
    localStorage.removeItem('traduciaTerminosAceptados');
    this.terminosAceptadosChange.emit(false);

    this.animandoShake = false;
    requestAnimationFrame(() => {
      this.animandoShake = true;
    });
  }

  /**
   * Reinicia el estado de la animacion de sacudida al finalizar.
   */
  onAnimationEnd(): void {
    this.animandoShake = false;
  }
}
