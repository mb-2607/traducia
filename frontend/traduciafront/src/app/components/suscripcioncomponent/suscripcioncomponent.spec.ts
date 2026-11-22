import { HttpErrorResponse } from '@angular/common/http';
import { signal } from '@angular/core';
import type { WritableSignal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Subject, of, throwError } from 'rxjs';
import type { Mock } from 'vitest';
import { PLANES } from '../../models/planmodel';
import type { PlanId } from '../../models/planmodel';
import { SuscripcionService } from '../../services/suscripcionservice';
import { Suscripcioncomponent } from './suscripcioncomponent';

/** Últimos dos dígitos del año siguiente, para fechas de vencimiento vigentes. */
const anioSiguiente = String(new Date().getFullYear() + 1).slice(-2);

/**
 * Pruebas unitarias de {@link Suscripcioncomponent}.
 *
 * Validan el catálogo de planes y sus límites, la selección de un plan, el formulario de pago y sus
 * validaciones, la confirmación del cambio de plan contra {@link SuscripcionService} (simulado) y
 * el manejo del foco para la navegación con teclado.
 */
describe('Suscripcioncomponent', () => {
  let fixture: ComponentFixture<Suscripcioncomponent>;
  let component: Suscripcioncomponent;
  let html: HTMLElement;
  let servicio: {
    planActivo: WritableSignal<PlanId>;
    listarPlanes: Mock;
    obtenerPlanActual: Mock;
    cambiarPlan: Mock;
  };

  const boton = (texto: string): HTMLButtonElement => {
    const encontrado = Array.from(html.querySelectorAll<HTMLButtonElement>('button')).find(
      (b) => b.textContent?.trim() === texto,
    );
    if (!encontrado) {
      throw new Error(`No hay un botón «${texto}» en pantalla`);
    }
    return encontrado;
  };

  /** Ejecuta la detección de cambios y espera a que termine. */
  async function actualizar(): Promise<void> {
    fixture.detectChanges();
    await fixture.whenStable();
  }

  /** Pulsa el botón «Elegir» del plan indicado. */
  async function elegir(nombrePlan: string): Promise<void> {
    boton(`Elegir ${nombrePlan}`).click();
    await actualizar();
  }

  /** Llena el formulario de pago con datos válidos; `cambios` permite alterar algún campo. */
  function llenarPago(cambios: Partial<Suscripcioncomponent['pago']['value']> = {}): void {
    component.pago.setValue({
      titular: 'María Fernanda Ruiz',
      pais: 'CO',
      direccion: 'Calle 45 # 12-30, Bogotá',
      empresa: '',
      tarjeta: '4111 1111 1111 1111',
      vencimiento: `12/${anioSiguiente}`,
      cvc: '123',
      confirmar: true,
      ...cambios,
    });
  }

  beforeEach(async () => {
    servicio = {
      planActivo: signal<PlanId>('gratuito'),
      listarPlanes: vi.fn().mockReturnValue(of(Object.values(PLANES))),
      obtenerPlanActual: vi.fn().mockReturnValue(of({})),
      cambiarPlan: vi.fn((plan: PlanId) => {
        servicio.planActivo.set(plan);
        return of({ nombre: 'laura', correo: 'laura@correo.com', rol: 'USER', plan });
      }),
    };

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, RouterModule.forRoot([])],
      declarations: [Suscripcioncomponent],
      providers: [{ provide: SuscripcionService, useValue: servicio }],
    }).compileComponents();

    fixture = TestBed.createComponent(Suscripcioncomponent);
    component = fixture.componentInstance;
    html = fixture.nativeElement;
    await actualizar();
  });

  /** Planes disponibles y plan activo. */
  describe('catálogo', () => {
    /** Verifica que al iniciar se consulten el catálogo y el plan actual. */
    it('consulta los planes y el plan actual al entrar', () => {
      expect(servicio.listarPlanes).toHaveBeenCalled();
      expect(servicio.obtenerPlanActual).toHaveBeenCalled();
    });

    /** Verifica que se indique el plan activo y que su botón esté desactivado. */
    it('muestra el plan activo y no deja elegirlo otra vez', () => {
      expect(html.querySelector('.plan-actual strong')?.textContent).toBe('Free Pass');
      expect(boton('Tu plan actual').disabled).toBe(true);
      expect(html.querySelectorAll('.plan-card')).toHaveLength(3);
    });

    /** Verifica la descripción de IAs, caracteres y archivos de cada plan. */
    it('describe los límites de cada plan', () => {
      const tarjetas = Array.from(html.querySelectorAll('.plan-card')).map((t) => t.textContent);

      expect(tarjetas[0]).toContain('1 IA disponible');
      expect(tarjetas[1]).toContain('2.000 caracteres por traducción');
      expect(tarjetas[1]).toContain('10 traducciones de archivos al día');
      expect(tarjetas[2]).toContain('Traducciones de archivos ilimitadas al día');
    });

    /** Verifica el formato de precio en pesos colombianos. */
    it('formatea los precios en pesos colombianos', () => {
      expect(component.precioFormateado(19900)).toMatch(/\$\s?19\.900/);
    });

    /** Verifica que el catálogo del backend reemplace al catálogo local. */
    it('usa los límites que envía el backend', async () => {
      servicio.listarPlanes.mockReturnValue(of([{ ...PLANES.oria, caracteresPorSolicitud: 2500 }]));

      component.ngOnInit();
      await actualizar();

      expect(html.querySelectorAll('.plan-card')).toHaveLength(1);
      expect(html.textContent).toContain('2.500 caracteres por traducción');
    });

    /** Verifica que, si fallan las consultas, se mantenga el catálogo local. */
    it('conserva el catálogo local si el backend no responde', async () => {
      servicio.listarPlanes.mockReturnValue(throwError(() => new Error('503')));
      servicio.obtenerPlanActual.mockReturnValue(throwError(() => new Error('503')));

      component.ngOnInit();
      await actualizar();

      expect(component.planes()).toEqual(Object.values(PLANES));
    });

    /** Verifica que una lista vacía del backend no deje la página sin planes. */
    it('conserva el catálogo local si el backend devuelve una lista vacía', async () => {
      servicio.listarPlanes.mockReturnValue(of([]));

      component.ngOnInit();
      await actualizar();

      expect(component.planes()).toHaveLength(3);
    });
  });

  /** Cambio a un plan de pago (orIA). */
  describe('plan de pago', () => {
    beforeEach(async () => {
      await elegir('orIA');
    });

    /**
     * Verifica que elegir orIA abra el formulario de membresía y el resumen, y marque la tarjeta
     * seleccionada.
     */
    it('abre el formulario de membresía', () => {
      expect(html.querySelector('#titulo-checkout')?.textContent?.trim()).toBe(
        'Configura tu membresía',
      );
      expect(html.querySelector('.resumen-pago')?.textContent).toContain('orIA');
      expect(html.querySelector('.plan-card.plan-seleccionado h2')?.textContent).toBe('orIA');
    });

    /** Verifica que el foco pase al título del formulario después de elegir un plan. */
    it('lleva el foco al formulario para quien usa teclado', () => {
      expect(document.activeElement).toBe(html.querySelector('#titulo-checkout'));
    });

    /** Verifica que con el formulario incompleto no se cambie el plan y se marquen los errores. */
    it('no confirma con el formulario incompleto', async () => {
      boton('Iniciar membresía').click();
      await actualizar();

      expect(servicio.cambiarPlan).not.toHaveBeenCalled();
      expect(html.querySelector('[role="alert"]')?.textContent).toBe(
        'Revisa los campos marcados para continuar.',
      );
      expect(html.querySelector('#error-tarjeta')?.textContent?.trim()).toBe(
        'El número de tarjeta debe tener entre 13 y 19 dígitos.',
      );
    });

    /** Verifica que con el formulario válido se cambie el plan y se muestre la confirmación. */
    it('activa el plan cuando el formulario es válido', async () => {
      llenarPago();

      boton('Iniciar membresía').click();
      await actualizar();

      expect(servicio.cambiarPlan).toHaveBeenCalledWith('oria');
      expect(html.querySelector('#titulo-confirmacion')?.textContent?.trim()).toBe(
        'Suscripción realizada',
      );
      expect(html.querySelector('.checkout')).toBeNull();
      expect(html.querySelector('.plan-actual strong')?.textContent).toBe('orIA');
    });

    /** Verifica que se muestre el mensaje del backend y que el formulario siga abierto. */
    it('muestra el mensaje del backend si rechaza el cambio', async () => {
      servicio.cambiarPlan.mockReturnValue(
        throwError(() => new HttpErrorResponse({ status: 400, error: 'Tarjeta rechazada' })),
      );
      llenarPago();

      component.confirmarSuscripcion();
      await actualizar();

      expect(html.querySelector('[role="alert"]')?.textContent).toBe('Tarjeta rechazada');
      expect(html.querySelector('.checkout')).not.toBeNull();
    });

    /** Verifica el mensaje genérico cuando el error no trae detalle. */
    it('muestra un mensaje genérico si el error no trae detalle', async () => {
      servicio.cambiarPlan.mockReturnValue(throwError(() => new Error('sin red')));
      llenarPago();

      component.confirmarSuscripcion();
      await actualizar();

      expect(component.mensajeFormulario()).toBe(
        'No fue posible actualizar tu plan en este momento. Inténtalo nuevamente.',
      );
    });

    /**
     * Verifica que mientras se guarda los botones estén desactivados y no se envíe otra solicitud.
     */
    it('bloquea los botones mientras se guarda el cambio', async () => {
      servicio.cambiarPlan.mockReturnValue(new Subject());
      llenarPago();

      component.confirmarSuscripcion();
      await actualizar();

      expect(boton('Actualizando plan…').disabled).toBe(true);
      expect(boton('Cancelar').disabled).toBe(true);
      component.confirmarSuscripcion();
      expect(servicio.cambiarPlan).toHaveBeenCalledTimes(1);
    });

    /**
     * Verifica que cancelar cierre y limpie el formulario y devuelva el foco al botón que lo abrió.
     */
    it('al cancelar cierra el formulario y devuelve el foco al botón del plan', async () => {
      llenarPago();

      boton('Cancelar').click();
      await actualizar();

      expect(html.querySelector('.checkout')).toBeNull();
      expect(component.pago.controls.titular.value).toBe('');
      expect(document.activeElement).toBe(boton('Elegir orIA'));
    });
  });

  /** Verifica que el cambio a Free Pass no pida datos de tarjeta y se confirme directamente. */
  it('cambia al plan gratuito sin pedir datos de pago', async () => {
    servicio.planActivo.set('infinitia');
    await actualizar();

    await elegir('Free Pass');

    expect(html.querySelector('#titulo-checkout')?.textContent?.trim()).toBe('Cambiar a Free Pass');
    expect(html.querySelector('input[formControlName="tarjeta"]')).toBeNull();

    boton('Activar Free Pass').click();
    await actualizar();

    expect(servicio.cambiarPlan).toHaveBeenCalledWith('gratuito');
    expect(html.querySelector('#titulo-confirmacion')?.textContent?.trim()).toBe(
      'Free Pass activado',
    );
  });

  /** Verifica que no se pueda seleccionar el plan que ya está activo. */
  it('ignora la selección del plan que ya está activo', () => {
    component.seleccionarPlan(PLANES.gratuito, document.createElement('button'));

    expect(component.planSeleccionado()).toBeNull();
  });

  /** Validadores de los campos del formulario de pago. */
  describe('validación del pago', () => {
    const errores = (campo: keyof Suscripcioncomponent['pago']['controls'], valor: string) => {
      const control = component.pago.controls[campo];
      control.setValue(valor as never);
      return control.errors;
    };

    /** Verifica que el número de tarjeta acepte espacios o guiones y rechace números cortos. */
    it('acepta la tarjeta con o sin separadores', () => {
      expect(errores('tarjeta', '4111111111111111')).toBeNull();
      expect(errores('tarjeta', '4111-1111-1111-1111')).toBeNull();
      expect(errores('tarjeta', '4111 1111')).not.toBeNull();
    });

    /** Verifica que el titular acepte tildes y apóstrofes y rechace números. */
    it('acepta nombres con tildes y apóstrofes', () => {
      expect(errores('titular', 'José Ángel O’Neill')).toBeNull();
      expect(errores('titular', 'Ana 3')).not.toBeNull();
    });

    /**
     * Verifica que se acepten el mes actual y las fechas futuras, y se rechacen las fechas pasadas,
     * los meses inválidos y los formatos incorrectos.
     */
    it('rechaza fechas de vencimiento pasadas o mal escritas', () => {
      const hoy = new Date();
      const mesActual = String(hoy.getMonth() + 1).padStart(2, '0');
      const anioActual = String(hoy.getFullYear()).slice(-2);

      expect(errores('vencimiento', `${mesActual}/${anioActual}`)).toBeNull();
      expect(errores('vencimiento', `01/${anioSiguiente}`)).toBeNull();
      expect(errores('vencimiento', '12/20')).toEqual({ vencimiento: true });
      expect(errores('vencimiento', '13/30')).toEqual({ vencimiento: true });
      expect(errores('vencimiento', '1230')).toEqual({ vencimiento: true });
    });

    /** Verifica que el código de seguridad tenga 3 o 4 dígitos. */
    it('exige un código de seguridad de 3 o 4 dígitos', () => {
      expect(errores('cvc', '123')).toBeNull();
      expect(errores('cvc', '1234')).toBeNull();
      expect(errores('cvc', '12')).not.toBeNull();
    });
  });
});
