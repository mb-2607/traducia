package co.edu.unbosque.traducia.exception;

/**
 * Excepción personalizada utilizada para indicar que la contraseña
 * proporcionada no cumple con las condiciones requeridas para una operación
 * dentro del sistema, como la autenticación de un usuario.
 * <p>
 * Extiende RuntimeException, por lo que es una excepción no verificada
 * (unchecked exception).
 * </p>
 *
 * @author
 * @version 1.0
 * @see RuntimeException
 */
public class InvalidPasswordException extends RuntimeException {

    /**
     * Construye una excepción indicando que la contraseña proporcionada
     * no es válida.
     *
     * @param message mensaje descriptivo del motivo de la excepción.
     */
    public InvalidPasswordException(String message) {
        super(message);
    }

}
