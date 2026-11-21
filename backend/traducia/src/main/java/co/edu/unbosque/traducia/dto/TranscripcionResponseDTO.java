package co.edu.unbosque.traducia.dto;

/**
 * Transcripción que recibe el frontend.
 *
 * No contiene credenciales ni información interna del proveedor.
 */
public class TranscripcionResponseDTO {
	private final String texto;
	private final int caracteres;

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