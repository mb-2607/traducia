package co.edu.unbosque.traducia.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO (Data Transfer Object) que representa el cuerpo de una petición de
 * traducción enviada a la API de DeepL.
 * <p>
 * Esta clase se serializa a JSON para enviarse como payload en la solicitud.
 * Los nombres de los atributos (por ejemplo {@code target_lang}) coinciden con
 * los campos que espera la API, por lo que se mantienen en <i>snake_case</i>.
 * </p>
 */
public class DeepLRequestDTO {

	/** Lista de textos que se desean traducir. */
	private List<String> text; // skipcq: JAVA-E1086

	/**
	 * Código del idioma de destino de la traducción (por ejemplo, {@code "ES"},
	 * {@code "EN-US"} o {@code "DE"}).
	 */
	private String target_lang;

	/**
	 * Constructor con todos los parámetros.
	 *
	 * @param text        lista de textos a traducir
	 * @param target_lang código del idioma de destino
	 */
	public DeepLRequestDTO(List<String> text, String target_lang) {
		this.text = text;
		this.target_lang = target_lang;
	}

	/**
	 * Constructor que recibe solo el idioma de destino.
	 * <p>
	 * Inicializa la lista de textos vacía para poder agregar los textos a traducir
	 * posteriormente sin riesgo de {@code NullPointerException}.
	 * </p>
	 *
	 * @param target_lang código del idioma de destino
	 */
	public DeepLRequestDTO(String target_lang) {
		this.target_lang = target_lang;
		text = new ArrayList<>();
	}

	/**
	 * Constructor por defecto.
	 * <p>
	 * Inicializa la lista de textos vacía. Necesario también para la
	 * deserialización de JSON.
	 * </p>
	 */
	public DeepLRequestDTO() {
		text = new ArrayList<>();
	}

	/**
	 * Obtiene la lista de textos a traducir.
	 *
	 * @return lista de textos
	 */
	public List<String> getText() {
		return text;
	}

	/**
	 * Establece la lista de textos a traducir.
	 *
	 * @param text lista de textos
	 */
	public void setText(List<String> text) {
		this.text = text; // skipcq: JAVA-E1086
	}

	/**
	 * Obtiene el código del idioma de destino.
	 *
	 * @return el código del idioma de destino (por ejemplo, {@code "ES"})
	 */
	public String getTarget_lang() {
		return target_lang;
	}

	/**
	 * Establece el código del idioma de destino.
	 *
	 * @param target_lang el código del idioma de destino (por ejemplo,
	 *                    {@code "ES"})
	 */
	public void setTarget_lang(String target_lang) {
		this.target_lang = target_lang;
	}

}
