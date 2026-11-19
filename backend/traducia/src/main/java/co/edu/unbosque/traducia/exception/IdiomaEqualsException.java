package co.edu.unbosque.traducia.exception;

/**
 * Se lanza cuando los idiomas de origen y destino son el mismo.
 */
public class IdiomaEqualsException extends RuntimeException {

	/**
	 * Crea la excepción con un mensaje descriptivo.
	 *
	 * @param message mensaje que describe el error
	 */
	public IdiomaEqualsException(String message) {
		super(message);
	}

}
