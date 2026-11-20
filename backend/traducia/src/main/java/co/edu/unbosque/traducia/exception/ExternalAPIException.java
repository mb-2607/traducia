package co.edu.unbosque.traducia.exception;

/**
 * Excepción no verificada que se lanza cuando falla el consumo de una API
 * externa, ya sea por una respuesta HTTP fuera del rango 2xx o por un problema
 * de comunicación.
 * <p>
 * Conserva el código de estado HTTP para que las capas superiores puedan
 * decidir cómo responder al cliente.
 * </p>
 *
 * @author TradudIA
 * @version 1.0
 */
public class ExternalAPIException extends RuntimeException {

	/** Identificador de versión para la serialización. */
	private static final long serialVersionUID = 1L;

	/** Código de estado HTTP asociado al error. */
	private final int statusCode;

	/**
	 * Crea la excepción con el código de estado HTTP indicado.
	 * <p>
	 * El mensaje generado tiene el formato
	 * {@code "Error al consumir API externa. HTTP: <statusCode>"}.
	 * </p>
	 *
	 * @param statusCode código de estado HTTP del error (por ejemplo, 401, 502 o
	 *                   504).
	 */
	public ExternalAPIException(int statusCode) {
		super("Error al consumir API externa. HTTP: " + statusCode);
		this.statusCode = statusCode;
	}
	
	/**
	 * Obtiene el código de estado HTTP asociado al error.
	 *
	 * @return código de estado HTTP.
	 */
	public int getStatusCode() {
		return statusCode;
	}
}
