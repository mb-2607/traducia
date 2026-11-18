package co.edu.unbosque.traducia.exception;

/**
 * Excepción personalizada utilizada para indicar que se intenta registrar
 * o utilizar una dirección de correo electrónico que ya se encuentra
 * asociada a otro usuario en el sistema.
 * <p>
 * Extiende RuntimeException, por lo que es una excepción no verificada
 * (unchecked exception).
 * </p>
 *
 * @author
 * @version 1.0
 * @see RuntimeException
 */
public class EmailAlreadyExistsException extends RuntimeException {

    /**
     * Construye una excepción indicando que el correo electrónico ya existe.
     *
     * @param message mensaje descriptivo del motivo de la excepción.
     */
    public EmailAlreadyExistsException(String message) {
        super(message);
    }

}
