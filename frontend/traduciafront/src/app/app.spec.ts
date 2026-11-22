import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router, RouterModule } from '@angular/router';
import { App } from './app';

/** Página mínima usada como destino de las rutas de prueba. */
@Component({ template: '<p>login</p>' })
class PaginaFalsa {}

/** Reemplaza la barra de navegación real para no depender de sus enlaces ni estilos. */
@Component({ selector: 'app-navbarcomponent', standalone: false, template: '' })
class NavbarFalso {}

/**
 * Pruebas unitarias del componente raíz {@link App}.
 *
 * Comprueban que la estructura principal (barra de navegación y área de contenido) se renderice y
 * que el componente siga la ruta activa para saber si la vista actual es pública. La barra de
 * navegación se reemplaza por un componente falso para aislar la prueba.
 */
describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        RouterModule.forRoot([
          { path: '', component: PaginaFalsa },
          { path: 'login', component: PaginaFalsa },
        ]),
      ],
      declarations: [App, NavbarFalso],
    }).compileComponents();
  });

  /**
   * Verifica que la plantilla incluya la barra de navegación dentro de la cabecera y el
   * `router-outlet` dentro del contenido principal.
   */
  it('muestra la barra de navegación y el contenido de la ruta', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const html: HTMLElement = fixture.nativeElement;

    expect(html.querySelector('header app-navbarcomponent')).not.toBeNull();
    expect(html.querySelector('main router-outlet')).not.toBeNull();
  });

  /** Verifica que la ruta inicial (`/`) se considere pública antes de cualquier navegación. */
  it('reconoce la raíz como ruta pública', () => {
    const app = TestBed.createComponent(App).componentInstance;

    expect(app.esRutaPublica()).toBe(true);
  });

  /**
   * Verifica que `rutaActual` se actualice con cada `NavigationEnd` y que `esRutaPublica` distinga
   * `/login` de la raíz con parámetros de consulta.
   */
  it('actualiza la ruta actual cuando termina una navegación', async () => {
    const app = TestBed.createComponent(App).componentInstance;
    const router = TestBed.inject(Router);

    await router.navigateByUrl('/login');
    expect(app.rutaActual).toBe('/login');
    expect(app.esRutaPublica()).toBe(false);

    await router.navigateByUrl('/?origen=correo');
    expect(app.esRutaPublica()).toBe(true);
  });
});
