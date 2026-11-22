package co.edu.unbosque.traducia.dto;

import java.util.List;

/**
 * DTO (Data Transfer Object) que representa la respuesta recibida de la API de
 * traducción de DeepL.
 * <p>
 * Esta clase se deserializa desde el JSON de respuesta. Solo mapea el campo
 * {@code translations}, que contiene una traducción por cada texto enviado en
 * {@link DeepLRequestDTO}.
 * </p>
 *
 * @see DeepLRequestDTO
 * @see DeepLTraduccionDTO
 */
public class DeepLResponseDTO {

	/**
	 * Lista de traducciones devueltas por DeepL. Conserva el mismo orden de los
	 * textos enviados en la petición.
	 */
	private List<DeepLTraduccionDTO> translations;

	/**
	 * Constructor por defecto.
	 * <p>
	 * Necesario para la deserialización de JSON.
	 * </p>
	 */
	public DeepLResponseDTO() {
	}

	/**
	 * Obtiene la lista de traducciones.
	 *
	 * @return lista de {@link DeepLTraduccionDTO}
	 */
	public List<DeepLTraduccionDTO> getTranslations() {
		return translations;
	}

	/**
	 * Establece la lista de traducciones.
	 *
	 * @param translations lista de {@link DeepLTraduccionDTO}
	 */
	public void setTranslations(List<DeepLTraduccionDTO> translations) {
		this.translations = translations; // skipcq: JAVA-E1086
	}

	
}