package co.edu.unbosque.traducia.entity;

/**
 * Tipos de autenticación soportados al consumir APIs externas de traducción.
 * <p>
 * Cada constante define el esquema que se antepone a la llave de la API en el
 * encabezado HTTP {@code Authorization}.
 * </p>
 *
 * @author TradudIA
 * @version 1.0
 */
public enum AuthenticationType {
	/** Autenticación estándar mediante token, con esquema {@code Bearer}. */
	BEARER("Bearer"),
	/** Autenticación propia de DeepL, con esquema {@code DeepL-Auth-Key}. */
	DEEPL("DeepL-Auth-Key");
	
	/** Esquema que precede a la llave en el encabezado {@code Authorization}. */
	private final String esquema;
	
	/**
	 * Construye un tipo de autenticación con su esquema asociado.
	 *
	 * @param esquema texto del esquema de autenticación (por ejemplo,
	 *                {@code "Bearer"}).
	 */
	private AuthenticationType(String esquema) {
		this.esquema = esquema;
	}	
	
	/**
	 * Crea el valor completo del encabezado {@code Authorization} combinando el
	 * esquema del tipo de autenticación con la llave recibida.
	 * <p>
	 * Ejemplo: para {@link #BEARER} y la llave {@code "abc123"} retorna
	 * {@code "Bearer abc123"}.
	 * </p>
	 *
	 * @param llave llave o token de acceso de la API externa.
	 * @return cadena con el formato {@code "<esquema> <llave>"}.
	 */
	public String crearFormatoLlave(String llave) {
		return esquema + " " + llave;
	}
}
