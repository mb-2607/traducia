package co.edu.unbosque.traducia.exception;

/**
 * Se lanza cuando no existe un idioma con el código indicado.
 */
public class IdiomaNotFoundException extends RuntimeException {

	/**
	 * Crea la excepción con un mensaje descriptivo.
	 *
	 * @param message mensaje que describe el error
	 */
	public IdiomaNotFoundException(String message) {
		super(message);
	}

}