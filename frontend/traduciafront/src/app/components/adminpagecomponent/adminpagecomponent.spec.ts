import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterModule } from '@angular/router';
import { Adminpagecomponent } from './adminpagecomponent';

/**
 * Pruebas unitarias de {@link Adminpagecomponent}.
 *
 * Validan el saludo del panel de administración, la forma de presentar el total de usuarios
 * (incluido el caso en que todavía no se conoce) y los accesos a usuarios y auditoría.
 */
describe('Adminpagecomponent', () => {
  let fixture: ComponentFixture<Adminpagecomponent>;
  let html: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RouterModule.forRoot([])],
      declarations: [Adminpagecomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Adminpagecomponent);
    html = fixture.nativeElement;
    await fixture.whenStable();
  });

  /** Verifica que el encabezado principal salude al administrador. */
  it('saluda al administrador', () => {
    expect(html.querySelector('h1')?.textContent).toContain('Hola, administrador');
  });

  /**
   * Verifica que, mientras `totalUsuarios` sea `null`, no se muestre ningún número y se anuncie que
   * el total no está disponible.
   */
  it('indica que el total de usuarios aún no está disponible', () => {
    const total = html.querySelector('.admin-total');

    expect(total?.querySelector('strong')).toBeNull();
    expect(total?.textContent).toContain('Total no disponible');
  });

  /**
   * Verifica que el total recibido por la entrada `totalUsuarios` aparezca resaltado en la
   * cabecera.
   */
  it('muestra el total de usuarios cuando se lo pasan', async () => {
    fixture.componentRef.setInput('totalUsuarios', 42);
    await fixture.whenStable();

    expect(html.querySelector('.admin-total strong')?.textContent).toBe('42');
  });

  /** Verifica que un total de `0` se muestre como número y no se confunda con un dato pendiente. */
  it('muestra cero como un total válido', async () => {
    fixture.componentRef.setInput('totalUsuarios', 0);
    await fixture.whenStable();

    expect(html.querySelector('.admin-total strong')?.textContent).toBe('0');
  });

  /** Verifica que las tarjetas de acceso apunten a `/admin/usuarios` y `/admin/auditoria`. */
  it('enlaza a la gestión de usuarios y a la auditoría', () => {
    const rutas = Array.from(html.querySelectorAll('.admin-acceso')).map((a) =>
      a.getAttribute('href'),
    );

    expect(rutas).toEqual(['/admin/usuarios', '/admin/auditoria']);
  });
});
