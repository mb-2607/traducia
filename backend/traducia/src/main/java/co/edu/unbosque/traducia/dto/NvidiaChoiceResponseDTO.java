package co.edu.unbosque.traducia.dto;

/**
 * DTO (Data Transfer Object) que representa una opción (<i>choice</i>) dentro
 * de la respuesta de la API de chat de NVIDIA.
 * <p>
 * Cada instancia corresponde a un elemento del arreglo {@code choices} de
 * {@link NvidiaResponseDTO}. Solo mapea el campo {@code message}, que contiene
 * el mensaje generado por el modelo.
 * </p>
 *
 * @see NvidiaResponseDTO
 * @see NvidiaMessageResponseDTO
 */
public class NvidiaChoiceResponseDTO {

	/** Mensaje generado por el modelo en esta opción. */
	private NvidiaMessageResponseDTO message;

	/**
	 * Constructor por defecto.
	 * <p>
	 * Necesario para la deserialización de JSON.
	 * </p>
	 */
	public NvidiaChoiceResponseDTO() {
	}

	/**
	 * Obtiene el mensaje generado por el modelo.
	 *
	 * @return el {@link NvidiaMessageResponseDTO} de esta opción
	 */
	public NvidiaMessageResponseDTO getMessage() {
		return message;
	}

	/**
	 * Establece el mensaje generado por el modelo.
	 *
	 * @param message el {@link NvidiaMessageResponseDTO} a asignar a esta opción
	 */
	public void setMessage(NvidiaMessageResponseDTO message) {
		this.message = message;
	}

	/**
	 * Constructor con todos los parámetros.
	 *
	 * @param message mensaje generado por el modelo
	 */
	public NvidiaChoiceResponseDTO(NvidiaMessageResponseDTO message) {
		this.message = message;
	}
}