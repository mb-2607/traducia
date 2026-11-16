import { DOCUMENT } from '@angular/common';
import { Injectable, inject, signal } from '@angular/core';
import { PLANES } from '../models/planmodel';
import type { PlanId } from '../models/planmodel';
/** Clave de `localStorage` donde se guarda el plan de demostración. */
const CLAVE_PLAN_DEMO = 'traducia.plan-demo';

/**
 * Servicio de suscripción (versión provisional).
 *
 * Mantiene el plan activo del usuario como una señal (`signal`) de solo lectura
 * y lo persiste en `localStorage` para que se conserve al recargar la página.
 *
 * **Provisional:** no hay pasarela de pago ni validación en el backend.
 * El plan se "activa" solo en el navegador, con fines de demostración, y debe
 * reemplazarse cuando exista una integración real de suscripciones.
 *
 * Si no hay un plan guardado, o el valor guardado no es un plan válido,
 * se usa el plan `'gratuito'`.
 *
 * Está disponible globalmente (`providedIn: 'root'`).
 */
@Injectable({ providedIn: 'root' })
export class SuscripcionService {

  /** Acceso al `document`, usado para llegar a `localStorage` de forma segura. */
  private readonly documento = inject(DOCUMENT);

  /** Estado interno y modificable del plan; se inicializa con el plan guardado. */
  private readonly estadoPlan = signal<PlanId>(this.leerPlanDemo());

  /** Plan actualmente activo, expuesto como señal de solo lectura. */
  readonly planActivo = this.estadoPlan.asReadonly();

  /**
   * Activa un plan de demostración.
   *
   * Actualiza la señal `planActivo` y trata de guardar el plan en `localStorage`.
   * Si el almacenamiento no está disponible (modo privado, cuota llena, SSR),
   * el error se ignora y el plan solo vive en memoria durante la sesión.
   *
   * @param plan Identificador del plan a activar.
   */
  activarPlanDemo(plan: PlanId): void {
    this.estadoPlan.set(plan);

    try {
      this.documento.defaultView?.localStorage.setItem(CLAVE_PLAN_DEMO, plan);
    } catch {}
  }

  /**
   * Lee el plan guardado en `localStorage`.
   *
   * Solo acepta el valor si corresponde a una clave existente en `PLANES`;
   * así se descartan valores corruptos o de planes que ya no existen.
   *
   * @returns El plan guardado si es válido; `'gratuito'` en cualquier otro caso.
   */
  private leerPlanDemo(): PlanId {
    try {
      const planGuardado = this.documento.defaultView?.localStorage.getItem(CLAVE_PLAN_DEMO);

      if (planGuardado && Object.prototype.hasOwnProperty.call(PLANES, planGuardado)) {
        return planGuardado as PlanId;
      }
    } catch {
      return "oria";
    }

    return 'gratuito';
  }
}
