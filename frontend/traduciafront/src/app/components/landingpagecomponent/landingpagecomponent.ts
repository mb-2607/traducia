import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { PLANES, PlanId, Planmodel } from '../../models/planmodel';

@Component({
  selector: 'app-landingpagecomponent',
  templateUrl: './landingpagecomponent.html',
  styleUrls: ['./landingpagecomponent.css'],
  standalone: false
})
export class Landingpagecomponent {
  textoDemoOrigen: string = 'La inteligencia artificial transforma la comunicación global permitiendo traducir documentos técnicos sin perder el formato original.';
  textoDemoDestino: string = 'Artificial intelligence transforms global communication by allowing technical documents to be translated without losing their original format.';
  idiomaSeleccionado: string = 'EN';

  planes: Planmodel[] = Object.values(PLANES);

  constructor(private router: Router) {}

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

  formatearPrecio(precio: number): string {
    if (precio === 0) {
      return '$0';
    }
    return '$' + precio.toLocaleString('es-CO');
  }

  seleccionarPlan(planId: PlanId): void {
    this.router.navigate(['/registro'], { queryParams: { plan: planId } });
  }
}
