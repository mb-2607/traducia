package co.edu.unbosque.traducia.dto;

/**
 * Transcripción que recibe el frontend.
 *
 * No contiene credenciales ni información interna del proveedor.
 */
public class TranscripcionResponseDTO {
	private final String texto;
	private final int caracteres;

	/**
	 * Construye la respuesta con el texto transcrito y calcula su longitud.
	 *
	 * @param texto contenido obtenido mediante la transcripción del audio
	 * @throws NullPointerException si el texto recibido es null
	 */
	public TranscripcionResponseDTO(String texto) {
		this.texto = texto;
		this.caracteres = texto.length();
	}

	public String getTexto() {
		return texto;
	}

	/**
	 * Usa el mismo criterio que String.length() del backend y texto.length del
	 * frontend.
	 */
	public int getCaracteres() {
		return caracteres;
	}
}