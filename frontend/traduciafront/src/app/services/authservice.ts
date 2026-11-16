import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {BehaviorSubject, map, Observable, tap} from 'rxjs';
import {AuthResponse, LoginRequest, RegisterRequest, User} from '../models/usermodel';
import {StorageService} from './storageservice';

/** Clave bajo la cual se guarda el usuario autenticado en el almacenamiento. */
const USER_KEY = 'user';

/** Clave bajo la cual se guarda el token JWT en el almacenamiento. */
const TOKEN_KEY = 'token';

/**
 * Servicio de autenticación.
 *
 * Se encarga de iniciar y cerrar sesión, registrar usuarios y mantener
 * el usuario y el token JWT de la sesión actual. La sesión se persiste
 * mediante {@link StorageService}, por lo que se restaura al recargar la aplicación.
 *
 * Está disponible globalmente (`providedIn: 'root'`).
 *
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  /** Cliente HTTP para comunicarse con el backend. */
  private cliente= inject(HttpClient);

  /** Servicio para almacenar en el LocalStorage. */
  private storageService: StorageService = inject(StorageService);

  /** URL base de la API de autenticación. Pendiente de configurar. */
  private readonly URL_BASE = 'TODO';

  /** Cabeceras HTTP enviadas en las peticiones (JSON de ida y de vuelta). */
  private readonly HEADER :HttpHeaders = new HttpHeaders({ 'Content-Type': 'application/json', 'accept': 'application/json' })

  /** Estado reactivo del usuario actual; `null` si no hay sesión. */
  private userSubject :BehaviorSubject<User|null> = new BehaviorSubject<User|null>(null);

  /** Token JWT de la sesión actual; `null` si no hay sesión. */
  private jwt: string|null = null;

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
  getUser():User|null {
    return this.userSubject.value;
  }

  /**
   * Expone el usuario autenticado como flujo observable, para reaccionar
   * a los cambios de sesión (inicio y cierre de sesión).
   *
   * @returns Observable que emite el usuario actual o `null`.
   */
  getUserAsObservable() :Observable<User|null> {
    return this.userSubject.asObservable();
  }

  /**
   * Obtiene el token JWT de la sesión actual.
   *
   * @returns El token JWT o `null` si no hay sesión.
   */
  getJwt() :string|null {
    return this.jwt;
  }

  /**
   * Inicia sesión enviando las credenciales al endpoint `/login`.
   *
   * Si la autenticación es exitosa, guarda el JWT y el usuario en memoria
   * y en el almacenamiento, y notifica a los suscriptores de
   * {@link getUserAsObservable}.
   *
   * @param usuario Credenciales de acceso.
   * @returns Observable que emite el usuario autenticado.
   */
  iniciarSesion(usuario :LoginRequest){
    return this.cliente.post<AuthResponse>(this.URL_BASE + '/login', usuario, {headers: this.HEADER}).pipe(
      tap(response=>{
        this.jwt = response.jwt;
        this.userSubject.next(response.user);
        this.storageService.set(USER_KEY, response.user);
        this.storageService.set(TOKEN_KEY, response.jwt);
      }),
      map(response => response.user)
    );
  }

  /**
   * Cierra la sesión: limpia el token y el usuario en memoria,
   * notifica a los suscriptores y elimina los datos del almacenamiento.
   */
  cerrarSesion(){
    this.jwt = null;
    this.userSubject.next(null);
    this.storageService.remove(USER_KEY);
    this.storageService.remove(TOKEN_KEY)
  }

  /**
   * Registra un nuevo usuario mediante el endpoint `/register`.
   * No inicia sesión automáticamente.
   *
   * @param usuario Datos de registro del nuevo usuario.
   * @returns Observable con la respuesta del servidor.
   */
  registrarUsuario(usuario :RegisterRequest){
    return this.cliente.post(this.URL_BASE + '/register', usuario, {headers: this.HEADER})
  }

  /**
   * Indica si hay una sesión activa (existen token y usuario).
   *
   * @returns `true` si hay sesión; `false` en caso contrario.
   */
  haySesion() :boolean{
    return this.jwt !== null && this.userSubject.value !== null;
  }
}
