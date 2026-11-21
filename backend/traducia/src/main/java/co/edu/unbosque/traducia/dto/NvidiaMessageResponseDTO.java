package co.edu.unbosque.traducia.dto;

/**
 * DTO (Data Transfer Object) que representa el mensaje generado por el modelo
 * dentro de una opción de la respuesta de la API de chat de NVIDIA.
 * <p>
 * Corresponde al objeto {@code message} de {@link NvidiaChoiceResponseDTO}. Solo
 * mapea el campo {@code content}, que contiene el texto de la respuesta.
 * </p>
 *
 * @see NvidiaChoiceResponseDTO
 * @see NvidiaResponseDTO
 */
public class NvidiaMessageResponseDTO {

	/** Texto de la respuesta generada por el modelo. */
	private String content;

	/**
	 * Constructor con todos los parámetros.
	 *
	 * @param content texto de la respuesta generada por el modelo
	 */
	public NvidiaMessageResponseDTO(String content) {
		this.content = content;
	}

	/**
	 * Constructor por defecto.
	 * <p>
	 * Necesario para la deserialización de JSON.
	 * </p>
	 */
	public NvidiaMessageResponseDTO() {
	}

	/**
	 * Obtiene el texto de la respuesta generada por el modelo.
	 *
	 * @return el contenido del mensaje
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Establece el texto de la respuesta generada por el modelo.
	 *
	 * @param content el contenido del mensaje
	 */
	public void setContent(String content) {
		this.content = content;
	}

}