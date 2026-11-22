package co.edu.unbosque.traducia.exception;

/**
 * Error controlado al validar, leer o extraer un documento. Mantiene el patrón
 * de código HTTP utilizado por ExternalAPIException.
 */
public class DocumentoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/** Código que la capa HTTP transforma en una respuesta de error. */
	private final int statusCode;

	/**
	 * Construye un error con un mensaje seguro para el usuario.
	 *
	 * @param statusCode código HTTP asociado
	 * @param mensaje    explicación del error, sin contenido ni credenciales
	 *                   externas
	 */
	public DocumentoException(int statusCode, String mensaje) {
		super(mensaje);
		this.statusCode = statusCode;
	}

	/**
	 * Conserva la causa técnica sin incorporarla al mensaje público.
	 *
	 * @param statusCode código HTTP asociado
	 * @param mensaje    explicación segura
	 * @param causa      causa original
	 */
	public DocumentoException(int statusCode, String mensaje, Throwable causa) {
		super(mensaje, causa);
		this.statusCode = statusCode;
	}

	/** @return código HTTP asociado al error. */
	public int getStatusCode() {
		return statusCode;
	}
}
