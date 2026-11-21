package co.edu.unbosque.traducia.dto;

/**
 * DTO (Data Transfer Object) que representa un mensaje individual dentro de la
 * conversación enviada a la API de chat de NVIDIA.
 * <p>
 * Cada instancia se serializa a JSON como un elemento del arreglo
 * {@code messages} de {@link NvidiaRequestDTO}.
 * </p>
 *
 * @see NvidiaRequestDTO
 */
public class NvidiaMessageRequestDTO {
	/**
	 * Rol de quien emite el mensaje. Los valores habituales son {@code "system"}
	 * (instrucciones al modelo), {@code "user"} (usuario) y {@code "assistant"}
	 * (respuestas previas del modelo).
	 */
	private String role;

	/** Texto del mensaje que se envía al modelo. */
	private String content;

	/**
	 * Constructor con todos los parámetros.
	 *
	 * @param role    rol del emisor del mensaje (por ejemplo, {@code "user"})
	 * @param content contenido textual del mensaje
	 */
	public NvidiaMessageRequestDTO(String role, String content) {
		this.role = role;
		this.content = content;
	}
	
	/**
	 * Constructor por defecto.
	 * <p>
	 * Necesario para la deserialización de JSON y para crear el objeto
	 * y asignar sus valores posteriormente mediante los setters.
	 * </p>
	 */
	public NvidiaMessageRequestDTO() {
	}
	
	/**
	 * Obtiene el rol del emisor del mensaje.
	 *
	 * @return el rol del mensaje (por ejemplo, {@code "system"}, {@code "user"} o {@code "assistant"})
	 */
	public String getRole() {
		return role;
	}
	
	/**
	 * Establece el rol del emisor del mensaje.
	 *
	 * @param role el rol del mensaje (por ejemplo, {@code "user"})
	 */
	public void setRole(String role) {
		this.role = role;
	}
	
	/**
	 * Obtiene el contenido textual del mensaje.
	 *
	 * @return el texto del mensaje
	 */
	public String getContent() {
		return content;
	}
	
	/**
	 * Establece el contenido textual del mensaje.
	 *
	 * @param content el texto del mensaje a enviar
	 */
	public void setContent(String content) {
		this.content = content;
	}

}