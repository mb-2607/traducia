package co.edu.unbosque.traducia.exception;

/**
 * Se lanza cuando el texto supera el límite de caracteres del plan del usuario.
 */
public class PlanCharacterLimitExceededException extends RuntimeException {

	/**
	 * Crea la excepción sin mensaje.
	 */
	public PlanCharacterLimitExceededException() {
	}

	/**
	 * Crea la excepción con un mensaje descriptivo.
	 *
	 * @param message mensaje que describe el error
	 */
	public PlanCharacterLimitExceededException(String message) {
		super(message);
	}

}