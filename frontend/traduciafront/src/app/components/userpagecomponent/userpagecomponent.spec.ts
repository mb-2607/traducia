import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { RouterModule } from '@angular/router';
import { Userpagecomponent } from './userpagecomponent';

/** Reemplaza el panel lateral real. */
@Component({ selector: 'app-asideusercomponent', standalone: false, template: '' })
class PanelLateralFalso {}

/**
 * Pruebas unitarias de {@link Userpagecomponent}.
 *
 * Validan la estructura de la página de usuario. El panel lateral se reemplaza por un componente
 * falso para probar solo este componente.
 */
describe('Userpagecomponent', () => {
  /**
   * Verifica que la página incluya el panel lateral y el área de contenido con su `router-outlet`.
   */
  it('arma la página con el panel lateral y el área de traducción', async () => {
    await TestBed.configureTestingModule({
      imports: [RouterModule.forRoot([])],
      declarations: [Userpagecomponent, PanelLateralFalso],
    }).compileComponents();

    const fixture = TestBed.createComponent(Userpagecomponent);
    await fixture.whenStable();
    const html: HTMLElement = fixture.nativeElement;

    expect(html.querySelector('app-asideusercomponent')).not.toBeNull();
    expect(html.querySelector('section.contenido router-outlet')).not.toBeNull();
    expect(html.querySelector('section.contenido')?.getAttribute('aria-label')).toBe(
      'Área de traducción',
    );
  });
});
