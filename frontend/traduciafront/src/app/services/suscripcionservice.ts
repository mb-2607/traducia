import { DOCUMENT } from '@angular/common';
import { Injectable, inject, signal } from '@angular/core';
import { PLANES } from '../models/plan';
import type { PlanId } from '../models/plan';
const CLAVE_PLAN_DEMO = 'traducia.plan-demo';

@Injectable({ providedIn: 'root' })
export class SuscripcionService {
  private readonly documento = inject(DOCUMENT);
  private readonly estadoPlan = signal<PlanId>(this.leerPlanDemo());
  readonly planActivo = this.estadoPlan.asReadonly();

  activarPlanDemo(plan: PlanId): void {
    this.estadoPlan.set(plan);

    try {
      this.documento.defaultView?.localStorage.setItem(CLAVE_PLAN_DEMO, plan);
    } catch {}
  }

  private leerPlanDemo(): PlanId {
    try {
      const planGuardado = this.documento.defaultView?.localStorage.getItem(CLAVE_PLAN_DEMO);

      if (planGuardado && Object.prototype.hasOwnProperty.call(PLANES, planGuardado)) {
        return planGuardado as PlanId;
      }
    } catch {}

    return 'gratuito';
  }
}
