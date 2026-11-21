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
 * @see DeepLTraduccion
 */
public class DeepLResponseDTO {

	/**
	 * Lista de traducciones devueltas por DeepL. Conserva el mismo orden de los
	 * textos enviados en la petición.
	 */
	private List<DeepLTraduccion> translations;

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
	 * @return lista de {@link DeepLTraduccion}
	 */
	public List<DeepLTraduccion> getTranslations() {
		return translations;
	}

	/**
	 * Establece la lista de traducciones.
	 *
	 * @param translations lista de {@link DeepLTraduccion}
	 */
	public void setTranslations(List<DeepLTraduccion> translations) {
		this.translations = translations; // skipcq: JAVA-E1086
	}

	/**
	 * Clase interna que representa una traducción individual dentro de la respuesta
	 * de DeepL.
	 * <p>
	 * Corresponde a cada elemento del arreglo {@code translations}. Solo mapea el
	 * campo {@code text}, que contiene el texto traducido.
	 * </p>
	 */
	public static class DeepLTraduccion {

		/** Texto traducido al idioma de destino. */
		private String text;

		/**
		 * Constructor por defecto.
		 * <p>
		 * Necesario para la deserialización de JSON.
		 * </p>
		 */
		public DeepLTraduccion() {
		}

		/**
		 * Obtiene el texto traducido.
		 *
		 * @return el texto traducido
		 */
		public String getText() {
			return text;
		}

		/**
		 * Establece el texto traducido.
		 *
		 * @param text el texto traducido
		 */
		public void setText(String text) {
			this.text = text;
		}
	}
}