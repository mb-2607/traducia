import { Component, EventEmitter, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-terminocomponent',
  standalone: false,
  templateUrl: './terminocomponent.html',
  styleUrl: './terminocomponent.css',
})
export class Terminocomponent implements OnInit {
  @Output() terminosAceptadosChange = new EventEmitter<boolean>();

  activo: boolean = false;
  error: boolean = false;
  animandoExit: boolean = false;
  animandoShake: boolean = false;

  ngOnInit(): void {
    const yaAcepto = localStorage.getItem('traduciaTerminosAceptados') === 'true';
    this.activo = !yaAcepto;
    this.terminosAceptadosChange.emit(yaAcepto);
  }

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

  rechazar(): void {
    this.error = true;
    localStorage.removeItem('traduciaTerminosAceptados');
    this.terminosAceptadosChange.emit(false);

    this.animandoShake = false;
    requestAnimationFrame(() => {
      this.animandoShake = true;
    });
  }

  onAnimationEnd(): void {
    this.animandoShake = false;
  }
}
