import {PlanId} from './planmodel';

export type Rol = 'ADMIN' | 'USER';

export interface User {
  nombre: string;
  correo: string;
  plan: PlanId;
  rol: Rol;
  jwt: string;
}
export interface LoginRequest {
  correo: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  correo: string;
  password: string;
}

export interface AuthResponse {
  user: User;
  jwt: string;
}
