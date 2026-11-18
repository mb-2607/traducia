package co.edu.unbosque.traducia.exception;

/**
 * Excepción personalizada utilizada para indicar que no se ha encontrado
 * un usuario solicitado dentro del sistema.
 * <p>
 * Extiende RuntimeException, por lo que es una excepción no verificada
 * (unchecked exception).
 * </p>
 *
 * @author
 * @version 1.0
 * @see RuntimeException
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Construye una excepción sin especificar un mensaje descriptivo.
     */
    public UserNotFoundException() {
    }

    /**
     * Construye una excepción indicando que el usuario solicitado
     * no ha sido encontrado.
     *
     * @param message mensaje descriptivo del motivo de la excepción.
     */
    public UserNotFoundException(String message) {
        super(message);
    }

}
