import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {BehaviorSubject, map, Observable, tap} from 'rxjs';
import {AuthResponse, LoginRequest, RegisterRequest, User} from '../models/usermodel';
import {StorageService} from './storageservice';

const USER_KEY = 'user';
const TOKEN_KEY = 'token';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private cliente= inject(HttpClient);
  private readonly URL_BASE = 'TODO';
  private readonly HEADER :HttpHeaders = new HttpHeaders({ 'Content-Type': 'application/json', 'accept': 'application/json' })

  private userSubject :BehaviorSubject<User|null> = new BehaviorSubject<User|null>(null);
  private jwt: string|null = null;

  constructor(private storageService: StorageService) {
    this.userSubject.next(storageService.get(USER_KEY) ?? null);
    this.jwt = this.storageService.get(TOKEN_KEY) ?? null;
  }

  getUser():User|null {
    return this.userSubject.value;
  }

  getUserAsObservable() :Observable<User|null> {
    return this.userSubject.asObservable();
  }

  getJwt(){
    return this.jwt;
  }

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

  cerrarSesion(){
    this.jwt = null;
    this.userSubject.next(null);
    this.storageService.remove(USER_KEY);
    this.storageService.remove(TOKEN_KEY)
  }

  registrarUsuario(usuario :RegisterRequest){
    return this.cliente.post(this.URL_BASE + '/register', usuario, {headers: this.HEADER})
  }

  haySesion(){
    return this.jwt !== null && this.userSubject.value !== null;
  }
}
