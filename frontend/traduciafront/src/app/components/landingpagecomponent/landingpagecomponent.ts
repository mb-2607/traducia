import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { PLANES, PlanId, Planmodel } from '../../models/planmodel';

@Component({
  selector: 'app-landingpagecomponent',
  templateUrl: './landingpagecomponent.html',
  styleUrls: ['./landingpagecomponent.css'],
  standalone: false
})
/**
 * Componente de la pagina principal de TraducIA.
 */
export class Landingpagecomponent {
  textoDemoOrigen = 'La inteligencia artificial transforma la comunicación global permitiendo traducir documentos técnicos sin perder el formato original.';
  textoDemoDestino = 'Artificial intelligence transforms global communication by allowing technical documents to be translated without losing their original format.';
  idiomaSeleccionado = 'EN';
  readonly moneda = 'es-CO';

  planes: Planmodel[] = Object.values(PLANES);

  constructor(private router: Router) {}

  /**
   * Cambia el idioma de demostracion y actualiza el texto de ejemplo.
   */
  cambiarIdiomaDemo(idioma: string): void {
    this.idiomaSeleccionado = idioma;
    if (idioma === 'EN') {
      this.textoDemoDestino = 'Artificial intelligence transforms global communication by allowing technical documents to be translated without losing their original format.';
    } else if (idioma === 'FR') {
      this.textoDemoDestino = "L'intelligence artificielle transforme la communication mondiale en permettant de traduire des documents techniques sans perdre leur format d'origine.";
    } else if (idioma === 'DE') {
      this.textoDemoDestino = 'Künstliche Intelligenz verändert die globale Kommunikation, indem sie die Übersetzung technischer Dokumente ohne Verlust des Originalformats ermöglicht.';
    }
  }

  /**
   * Formatea un valor numerico como precio en pesos.
   */
  formatearPrecio(precio: number): string {
    if (precio === 0) {
      return '$0';
    }
    return `$${precio.toLocaleString(this.moneda)}`;
  }

  /**
   * Navega a la vista de registro llevando el plan seleccionado.
   */
  seleccionarPlan(planId: PlanId): void {
    this.router.navigate(['/registro'], { queryParams: { plan: planId } });
  }
}
