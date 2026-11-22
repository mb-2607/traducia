package co.edu.unbosque.traducia.dto;

/**
 * Clase que representa una traducción individual dentro de la respuesta
 * de DeepL.
 * <p>
 * Corresponde a cada elemento del arreglo {@code translations}. Solo mapea el
 * campo {@code text}, que contiene el texto traducido.
 * </p>
 */
public class DeepLTraduccionDTO {

	/** Texto traducido al idioma de destino. */
	private String text;

	/**
	 * Constructor por defecto.
	 * <p>
	 * Necesario para la deserialización de JSON.
	 * </p>
	 */
	public DeepLTraduccionDTO() {
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
