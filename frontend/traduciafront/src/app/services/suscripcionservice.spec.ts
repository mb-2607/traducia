import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { BehaviorSubject } from 'rxjs';
import type { Planmodel } from '../models/planmodel';
import type { User, UserResponseDTO } from '../models/usermodel';
import { AuthService } from './authservice';
import { SuscripcionService } from './suscripcionservice';

const URL_PLAN = 'http://localhost:8080/plan';

/**
 * Pruebas unitarias de {@link SuscripcionService}.
 *
 * Validan el plan activo y su sincronización con la sesión, el catálogo de planes combinado con los
 * límites del backend, la consulta del plan actual y el cambio de plan. {@link AuthService} se
 * simula con un `BehaviorSubject`.
 */
describe('SuscripcionService', () => {
  let httpMock: HttpTestingController;
  let sesion$: BehaviorSubject<User | null>;
  let auth: Partial<AuthService>;

  /** Crea un usuario de sesión con el plan indicado. */
  const usuario = (plan: User['plan']): User => ({
    nombre: 'laura',
    correo: 'laura@correo.com',
    rol: 'USER',
    plan,
  });

  beforeEach(() => {
    sesion$ = new BehaviorSubject<User | null>(usuario('oria'));
    auth = {
      getUser: () => sesion$.value,
      getUserAsObservable: () => sesion$.asObservable(),
      actualizarUsuario: vi.fn((dto: UserResponseDTO) => {
        const actualizado = usuario(dto.planId === 'INFINITIA' ? 'infinitia' : 'gratuito');
        sesion$.next(actualizado);
        return actualizado;
      }),
    };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: auth },
      ],
    });

    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  /** Verifica que el plan activo inicial sea el de la sesión. */
  it('toma el plan activo de la sesión', () => {
    expect(TestBed.inject(SuscripcionService).planActivo()).toBe('oria');
  });

  /** Verifica que sin sesión el plan activo sea el gratuito. */
  it('usa el plan gratuito cuando no hay sesión', () => {
    sesion$.next(null);

    expect(TestBed.inject(SuscripcionService).planActivo()).toBe('gratuito');
  });

  /** Verifica que el plan activo cambie con cada emisión de la sesión. */
  it('sigue los cambios de sesión', () => {
    const servicio = TestBed.inject(SuscripcionService);

    sesion$.next(usuario('infinitia'));
    expect(servicio.planActivo()).toBe('infinitia');

    sesion$.next(null);
    expect(servicio.planActivo()).toBe('gratuito');
  });

  /** Catálogo de planes desde `GET /plan`. */
  describe('listarPlanes', () => {
    /** Verifica que los límites del backend se combinen con el nombre y el precio locales. */
    it('combina los límites del backend con los datos visuales de cada plan', () => {
      let planes: Planmodel[] = [];

      TestBed.inject(SuscripcionService)
        .listarPlanes()
        .subscribe((lista) => (planes = lista));
      httpMock.expectOne(URL_PLAN).flush([
        { plan: 'FREE', numeroCaracterPorSolicitud: 800, proveedores: ['DEEPL'] },
        { plan: 'ORIA', numeroCaracterPorSolicitud: 2500, proveedores: ['DEEPL', 'GEMMA'] },
      ]);

      expect(planes).toHaveLength(2);
      expect(planes[1]).toMatchObject({
        id: 'oria',
        nombre: 'orIA',
        precio: 19900,
        caracteresPorSolicitud: 2500,
        ias: 2,
      });
    });

    /** Verifica que sin proveedores se conserve la cantidad de IAs del catálogo local. */
    it('conserva la cantidad de IAs local si el backend no envía proveedores', () => {
      let planes: Planmodel[] = [];

      TestBed.inject(SuscripcionService)
        .listarPlanes()
        .subscribe((lista) => (planes = lista));
      httpMock
        .expectOne(URL_PLAN)
        .flush([{ plan: 'INFINITIA', numeroCaracterPorSolicitud: 5000, proveedores: [] }]);

      expect(planes[0].ias).toBe(3);
    });

    /** Verifica que los planes desconocidos se descarten. */
    it('ignora planes que el frontend no conoce', () => {
      let planes: Planmodel[] = [];

      TestBed.inject(SuscripcionService)
        .listarPlanes()
        .subscribe((lista) => (planes = lista));
      httpMock.expectOne(URL_PLAN).flush([
        { plan: 'PREMIUM', numeroCaracterPorSolicitud: 99999, proveedores: [] },
        { plan: 'FREE', numeroCaracterPorSolicitud: 500, proveedores: ['DEEPL'] },
      ]);

      expect(planes.map((plan) => plan.id)).toEqual(['gratuito']);
    });
  });

  /** Plan vigente desde `GET /plan/actual`. */
  describe('obtenerPlanActual', () => {
    /** Verifica que el plan actual del backend actualice la señal. */
    it('actualiza el plan activo con la respuesta del backend', () => {
      const servicio = TestBed.inject(SuscripcionService);

      servicio.obtenerPlanActual().subscribe();
      httpMock
        .expectOne(`${URL_PLAN}/actual`)
        .flush({ plan: 'INFINITIA', numeroCaracterPorSolicitud: 5000, proveedores: [] });

      expect(servicio.planActivo()).toBe('infinitia');
    });

    /** Verifica que un plan desconocido no modifique la señal. */
    it('no cambia el plan activo si la respuesta trae un plan desconocido', () => {
      const servicio = TestBed.inject(SuscripcionService);

      servicio.obtenerPlanActual().subscribe();
      httpMock
        .expectOne(`${URL_PLAN}/actual`)
        .flush({ plan: 'VIP', numeroCaracterPorSolicitud: 1, proveedores: [] });

      expect(servicio.planActivo()).toBe('oria');
    });
  });

  /** Cambio de plan con `PUT /plan/cambiar`. */
  describe('cambiarPlan', () => {
    /** Verifica el cuerpo de la petición, la actualización de la sesión y el nuevo plan activo. */
    it('envía el plan en el formato del backend y actualiza la sesión', () => {
      const servicio = TestBed.inject(SuscripcionService);
      let resultado: User | undefined;
      const respuesta: UserResponseDTO = {
        nombre: 'laura',
        correo: 'laura@correo.com',
        rol: 'USER',
        planId: 'INFINITIA',
      };

      servicio.cambiarPlan('infinitia').subscribe((u) => (resultado = u));

      const peticion = httpMock.expectOne(`${URL_PLAN}/cambiar`);
      expect(peticion.request.method).toBe('PUT');
      expect(peticion.request.body).toEqual({ plan: 'INFINITIA' });
      peticion.flush(respuesta);

      expect(auth.actualizarUsuario).toHaveBeenCalledWith(respuesta);
      expect(resultado?.plan).toBe('infinitia');
      expect(servicio.planActivo()).toBe('infinitia');
    });

    /** Verifica que un error al cambiar de plan no modifique la sesión ni el plan activo. */
    it('mantiene el plan anterior si el cambio falla', () => {
      const servicio = TestBed.inject(SuscripcionService);

      servicio.cambiarPlan('gratuito').subscribe({ error: () => undefined });
      httpMock
        .expectOne(`${URL_PLAN}/cambiar`)
        .flush('No permitido', { status: 403, statusText: 'Forbidden' });

      expect(servicio.planActivo()).toBe('oria');
      expect(auth.actualizarUsuario).not.toHaveBeenCalled();
    });
  });
});
