package co.edu.unbosque.traducia.exception;

/**
 * Excepción personalizada lanzada cuando un administrador intenta realizar una
 * acción restringida sobre su propia cuenta, como actualizarla o eliminarla.
 * <p>
 * Permite interceptar estas operaciones desde los controladores para responder
 * con el código de estado HTTP correspondiente y evitar modificaciones o
 * eliminaciones accidentales de la propia sesión administrativa.
 *
 * @author
 * @version 1.0
 */


public class SelfActionNotAllowedException extends RuntimeException{
	
	/**
	 * Identificador de versión para la serialización de la excepción.
	 */
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Crea una nueva instancia de la excepción con un mensaje descriptivo por defecto.
	 */
	

	public SelfActionNotAllowedException() {
		super("No puedes realizar esta acción sobre tu propia cuenta");
	}
	
	/**
	 * Crea una nueva instancia de la excepción con un mensaje personalizado.
	 *
	 * @param message mensaje que describe el motivo por el cual la acción no está permitida.
	 */
	

	public SelfActionNotAllowedException(String message) {
		super(message);
	}

}
