import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, map, Observable, tap } from 'rxjs';
import type {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  User,
  UserResponseDTO,
} from '../models/usermodel';
import type { PlanId } from '../models/planmodel';
import { StorageService } from './storageservice';

/** Clave bajo la cual se guarda el usuario autenticado en el almacenamiento. */
const USER_KEY = 'user';

/** Clave bajo la cual se guarda el token JWT en el almacenamiento. */
const TOKEN_KEY = 'token';
export const API_URL_BASE = 'http://localhost:8080';
/**
 * Convierte los datos del backend al modelo interno del frontend.
 *
 * Mantiene los identificadores visuales utilizados por las
 * tarjetas de suscripción y el perfil.
 */
function adaptarUsuario(datos: UserResponseDTO): User {
  const planes: Readonly<Record<UserResponseDTO['planId'], PlanId>> = {
    FREE: 'gratuito',
    ORIA: 'oria',
    INFINITIA: 'infinitia',
  };

  return {
    nombre: datos.nombre,
    correo: datos.correo,
    rol: datos.rol,
    plan: planes[datos.planId],
  };
}

/**
 * Servicio de autenticación.
 *
 * Se encarga de iniciar y cerrar sesión, registrar usuarios y mantener
 * el usuario y el token JWT de la sesión actual. La sesión se persiste
 * mediante {@link StorageService}, por lo que se restaura al recargar la aplicación.
 *
 * Está disponible globalmente (`providedIn: 'root'`).
 */
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  /** Cliente HTTP para comunicarse con el backend. */
  private cliente = inject(HttpClient);

  /** Servicio para almacenar en el LocalStorage. */
  private storageService: StorageService = inject(StorageService);

  /** URL base de la API de autenticación. */
  private readonly URL_BASE = 'http://localhost:8080/auth';

  /** Cabeceras HTTP enviadas en las peticiones (JSON de ida y de vuelta). */
  private readonly HEADER: HttpHeaders = new HttpHeaders({
    'Content-Type': 'application/json',
    accept: 'application/json',
  });

  /** Estado reactivo del usuario actual; `null` si no hay sesión. */
  private userSubject: BehaviorSubject<User | null> = new BehaviorSubject<User | null>(null);

  /** Token JWT de la sesión actual; `null` si no hay sesión. */
  private jwt: string | null = null;

  /**
   * Restaura la sesión desde el almacenamiento (usuario y token), si existe.
   */
  constructor() {
    this.userSubject.next(this.storageService.get(USER_KEY) ?? null);
    this.jwt = this.storageService.get(TOKEN_KEY) ?? null;
  }

  /**
   * Obtiene el valor actual del usuario autenticado (lectura puntual).
   *
   * @returns El usuario actual o `null` si no hay sesión.
   */
  getUser(): User | null {
    return this.userSubject.value;
  }

  /**
   * Expone el usuario autenticado como flujo observable, para reaccionar
   * a los cambios de sesión (inicio y cierre de sesión).
   *
   * @returns Observable que emite el usuario actual o `null`.
   */
  getUserAsObservable(): Observable<User | null> {
    return this.userSubject.asObservable();
  }

  /**
   * Obtiene el token JWT de la sesión actual.
   *
   * @returns El token JWT o `null` si no hay sesión.
   */
  getJwt(): string | null {
    return this.jwt;
  }

  /**
   * Autentica al usuario y conserva la sesión recibida del backend.
   */
  iniciarSesion(credenciales: LoginRequest): Observable<User> {
    return this.cliente
      .post<AuthResponse>(`${this.URL_BASE}/login`, credenciales, { headers: this.HEADER })
      .pipe(
        map((respuesta) => ({
          usuario: adaptarUsuario(respuesta.userResponse),
          token: respuesta.jwt,
        })),
        tap(({ usuario, token }) => {
          this.storageService.set(USER_KEY, usuario);
          this.storageService.set(TOKEN_KEY, token);

          this.jwt = token;
          this.userSubject.next(usuario);
        }),
        map(({ usuario }) => usuario),
      );
  }

  /**
   * Sincroniza en memoria y almacenamiento los cambios confirmados del usuario
   * tras actualizar el perfil o cambiar de plan.
   *
   * @param datos Respuesta actualizada del backend.
   * @returns Usuario adaptado al modelo interno del frontend.
   */
  actualizarUsuario(datos: UserResponseDTO): User {
    const usuario = adaptarUsuario(datos);
    this.storageService.set(USER_KEY, usuario);
    this.userSubject.next(usuario);
    return usuario;
  }

  /**
   * Cierra la sesión: limpia el token y el usuario en memoria,
   * notifica a los suscriptores y elimina los datos del almacenamiento.
   */
  cerrarSesion(): void {
    this.jwt = null;
    this.userSubject.next(null);
    this.storageService.remove(USER_KEY);
    this.storageService.remove(TOKEN_KEY);
  }

  /**
   * Registra la cuenta sin iniciar sesión automáticamente.
   *
   * La cuenta debe completar la verificación de correo
   * exigida por el backend.
   */
  registrarUsuario(datos: RegisterRequest): Observable<User> {
    const solicitud: RegisterRequest = {
      nombre: datos.nombre,
      correo: datos.correo,
      password: datos.password,
    };

    return this.cliente
      .post<UserResponseDTO>(`${this.URL_BASE}/register`, solicitud, { headers: this.HEADER })
      .pipe(map((respuesta) => adaptarUsuario(respuesta)));
  }

  /**
   * Indica si hay una sesión activa (existen token y usuario).
   *
   * @returns `true` si hay sesión; `false` en caso contrario.
   */
  haySesion(): boolean {
    return this.jwt !== null && this.userSubject.value !== null;
  }
}
