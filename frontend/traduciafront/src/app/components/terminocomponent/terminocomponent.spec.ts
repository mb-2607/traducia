import { signal } from '@angular/core';
import type { WritableSignal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import type { Mock } from 'vitest';
import { TerminosService } from '../../services/terminosservice';
import { Terminocomponent } from './terminocomponent';

/**
 * Pruebas unitarias de {@link Terminocomponent}.
 *
 * Validan la apertura del modal de términos, la aceptación, el rechazo y el cierre con Escape, y lo
 * que se comunica al componente padre. {@link TerminosService} se simula con una señal; el
 * comportamiento de `<dialog>` lo aporta `src/test-setup.ts`.
 */
describe('Terminocomponent', () => {
  let fixture: ComponentFixture<Terminocomponent>;
  let html: HTMLElement;
  let terminos: { aceptados: WritableSignal<boolean>; aceptar: Mock };
  let decisiones: boolean[];

  const dialogo = () => html.querySelector('dialog') as HTMLDialogElement;
  const boton = (clase: string) => html.querySelector(`.${clase}`) as HTMLButtonElement;

  /** Crea el componente y registra las decisiones que emite. */
  async function crearComponente(): Promise<void> {
    fixture = TestBed.createComponent(Terminocomponent);
    html = fixture.nativeElement;
    decisiones = [];
    fixture.componentInstance.terminosAceptadosChange.subscribe((valor) => decisiones.push(valor));
    await fixture.whenStable();
  }

  beforeEach(async () => {
    terminos = { aceptados: signal(false), aceptar: vi.fn(() => terminos.aceptados.set(true)) };

    await TestBed.configureTestingModule({
      declarations: [Terminocomponent],
      providers: [{ provide: TerminosService, useValue: terminos }],
    }).compileComponents();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  /** Verifica que el modal se abra al iniciar cuando no hay una aceptación previa. */
  it('abre los términos si el usuario todavía no los ha aceptado', async () => {
    await crearComponente();

    expect(dialogo().open).toBe(true);
    expect(html.querySelector('#tc-titulo')?.textContent).toContain('Términos y condiciones');
  });

  /** Verifica que el modal no se abra si los términos ya fueron aceptados. */
  it('no vuelve a mostrar los términos a quien ya los aceptó', async () => {
    const abrir = vi.spyOn(HTMLDialogElement.prototype, 'showModal');
    terminos.aceptados.set(true);

    await crearComponente();

    expect(abrir).not.toHaveBeenCalled();
    expect(dialogo().open).toBe(false);
  });

  /** Verifica que aceptar registre la aceptación, cierre el modal y emita `true`. */
  it('registra la aceptación, cierra el diálogo y avisa al padre', async () => {
    await crearComponente();

    boton('tc-aceptar').click();
    await fixture.whenStable();

    expect(terminos.aceptar).toHaveBeenCalledTimes(1);
    expect(dialogo().open).toBe(false);
    expect(decisiones).toEqual([true]);
  });

  /** Verifica que no se guarde de nuevo una aceptación existente, aunque se avise al padre. */
  it('no registra otra vez una aceptación que ya existía', async () => {
    terminos.aceptados.set(true);
    await crearComponente();

    fixture.componentInstance.aceptar();

    expect(terminos.aceptar).not.toHaveBeenCalled();
    expect(decisiones).toEqual([true]);
  });

  /** Verifica que rechazar muestre el error, mantenga el modal abierto y emita `false`. */
  it('muestra un error y mantiene el diálogo abierto al rechazar', async () => {
    await crearComponente();

    boton('tc-rechazar').click();
    await fixture.whenStable();

    expect(html.querySelector('.tc-error')?.textContent).toContain(
      'Debes aceptar los términos para registrarte o iniciar sesión.',
    );
    expect(dialogo().classList).toContain('tc-dialogo-error');
    expect(dialogo().open).toBe(true);
    expect(decisiones).toEqual([false]);
  });

  /** Verifica que Escape no cierre el modal y se trate como un rechazo. */
  it('impide cerrar el diálogo con Escape', async () => {
    await crearComponente();
    const escape = new Event('cancel', { cancelable: true });

    dialogo().dispatchEvent(escape);
    await fixture.whenStable();

    expect(escape.defaultPrevented).toBe(true);
    expect(fixture.componentInstance.error()).toBe(true);
    expect(decisiones).toEqual([false]);
  });

  /** Verifica que el error desaparezca si el usuario acepta después de rechazar. */
  it('quita el mensaje de error cuando finalmente acepta', async () => {
    await crearComponente();
    boton('tc-rechazar').click();
    await fixture.whenStable();

    boton('tc-aceptar').click();
    await fixture.whenStable();

    expect(html.querySelector('.tc-error')).toBeNull();
    expect(decisiones).toEqual([false, true]);
  });
});
