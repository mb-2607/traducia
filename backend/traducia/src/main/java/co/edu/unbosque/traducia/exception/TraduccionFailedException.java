package co.edu.unbosque.traducia.exception;

/**
 * Se lanza cuando la solicitud no obtiene ninguna traducción.
 */
public class TraduccionFailedException extends RuntimeException {

	/**
	 * Crea la excepción con un mensaje descriptivo.
	 *
	 * @param message mensaje que describe el error
	 */
	public TraduccionFailedException(String message) {
		super(message);
	}

}
