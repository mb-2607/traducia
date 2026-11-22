import type { PlanId } from './planmodel';

/** Roles reconocidos por el backend. */
export type Rol = 'ADMIN' | 'USER';

/** Identificadores de planes utilizados por la API. */
export type PlanApi = 'FREE' | 'ORIA' | 'INFINITIA';

/**
 * Usuario utilizado internamente por los componentes del frontend.
 *
 * El servicio de autenticación adapta la respuesta de la API
 * a esta representación.
 */
export interface User {
  nombre: string;
  correo: string;
  plan: PlanId;
  rol: Rol;
}

/** Datos del usuario devueltos por el backend. */
export interface UserResponseDTO {
  id?: number;
  nombre: string;
  correo: string;
  planId: PlanApi;
  rol: Rol;
}

/**
 * Credenciales enviadas a POST /auth/login.
 *
 * El backend actual autentica mediante el nombre de usuario.
 */
export interface LoginRequest {
  nombre: string;
  password: string;
}

/** Datos enviados a POST /auth/register. */
export interface RegisterRequest {
  nombre: string;
  correo: string;
  password: string;
}

/** Respuesta de POST /auth/login. */
export interface AuthResponse {
  userResponse: UserResponseDTO;
  jwt: string;
}

/** Datos enviados a PUT /user/me. */
export interface UpdateUserRequestDTO {
  nombre: string;
  correo: string;
  rol: Rol;
  planId: PlanApi;
}
