package co.edu.unbosque.traducia.exception;

/**
 * Excepción personalizada lanzada cuando un token de recuperación de contraseña
 * no existe, ya fue utilizado o ha expirado.
 *
 * @author
 * @version 1.0
 */


public class InvalidTokenException extends RuntimeException {
	
	/**
	 * Identificador de versión para la serialización de la excepción.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Crea una nueva instancia con un mensaje descriptivo por defecto.
	 */
	public InvalidTokenException() {
		super("El token de recuperación es inválido o ha expirado");
	}

	/**
	 * Crea una nueva instancia con un mensaje personalizado.
	 *
	 * @param message detalle del error relacionado con el token.
	 */
	public InvalidTokenException(String message) {
		super(message);
	}

}
