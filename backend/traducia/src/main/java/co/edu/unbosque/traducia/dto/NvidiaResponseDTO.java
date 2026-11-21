package co.edu.unbosque.traducia.dto;

import java.util.List;

/**
 * DTO (Data Transfer Object) que representa la respuesta recibida de la API de
 * chat de NVIDIA.
 * <p>
 * Esta clase se deserializa desde el JSON de respuesta. Solo mapea el campo
 * {@code choices}, que contiene las posibles respuestas generadas por el modelo.
 * </p>
 *
 * @see NvidiaChoiceResponseDTO
 * @see NvidiaRequestDTO
 */
public class NvidiaResponseDTO {
	/** Lista de respuestas (opciones) generadas por el modelo. */
	private List<NvidiaChoiceResponseDTO> choices; 
	
	/**
	 * Constructor por defecto.
	 * <p>
	 * Necesario para la deserialización de JSON.
	 * </p>
	 */
	public NvidiaResponseDTO() {
	}
	
	/**
	 * Constructor con todos los parámetros.
	 *
	 * @param choices lista de opciones generadas por el modelo
	 */
	public NvidiaResponseDTO(List<NvidiaChoiceResponseDTO> choices) {
		this.choices = choices; // skipcq: JAVA-E1086
	}
	
	/**
	 * Obtiene la lista de opciones generadas por el modelo.
	 *
	 * @return lista de {@link NvidiaChoiceResponseDTO}
	 */
	public List<NvidiaChoiceResponseDTO> getChoices() {
		return choices;
	}
	
	/**
	 * Establece la lista de opciones generadas por el modelo.
	 *
	 * @param choices lista de {@link NvidiaChoiceResponseDTO}
	 */
	public void setChoices(List<NvidiaChoiceResponseDTO> choices) {
		this.choices = choices;
	}
	
	/**
	 * Método de conveniencia que extrae el texto de la respuesta principal del
	 * modelo, es decir, el contenido del mensaje de la primera opción
	 * ({@code choices[0].message.content}).
	 *
	 * @return el texto generado por el modelo en la primera opción
	 * @throws NullPointerException      si {@code choices} es {@code null}
	 * @throws IndexOutOfBoundsException si la lista {@code choices} está vacía
	 */
	public String obtenerContenido() {
		return choices.get(0).getMessage().getContent();
	}
}
