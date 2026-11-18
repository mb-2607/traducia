package co.edu.unbosque.traducia.exception;

/**
 * Excepción personalizada utilizada para indicar que se intenta registrar
 * o utilizar un nombre de usuario que ya se encuentra asociado a otra
 * cuenta dentro del sistema.
 * <p>
 * Extiende RuntimeException, por lo que es una excepción no verificada
 * (unchecked exception).
 * </p>
 *
 * @author
 * @version 1.0
 * @see RuntimeException
 */
public class UsernameAlreadyExistsException extends RuntimeException {

    /**
     * Construye una excepción indicando que el nombre de usuario ya existe.
     *
     * @param message mensaje descriptivo del motivo de la excepción.
     */
    public UsernameAlreadyExistsException(String message) {
        super(message);
    }

}