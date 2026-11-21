/**
 * Credenciales enviadas al endpoint de inicio de sesión para autenticar
 * al usuario en el backend.
 */
export interface LoginRequest {
  /** Nombre de usuario registrado en el sistema. */
  nombre: string;
  /** Contraseña en texto plano enviada para su validación. */
  password: string;
}

/**
 * Información básica del usuario devuelta junto con el token tras
 * iniciar sesión.
 */
export interface UserResponse {
  /** Identificador único del usuario, si está disponible. */
  id?: number;
  /** Nombre de usuario. */
  nombre?: string;
  /** Correo electrónico asociado a la cuenta. */
  correo?: string;
  /** Rol asignado al usuario dentro de la aplicación. */
  rol?: string;
}

/**
 * Respuesta recibida al autenticarse correctamente mediante el endpoint
 * de inicio de sesión.
 */
export interface LoginResponse {
  /** Datos del usuario autenticado, o `null` si no se incluyeron en la respuesta. */
  userResponse: UserResponse | null;
  /** Token JWT emitido por el backend para autorizar solicitudes posteriores. */
  jwt: string;
}
