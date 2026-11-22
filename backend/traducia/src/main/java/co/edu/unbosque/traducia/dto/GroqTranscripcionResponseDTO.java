package co.edu.unbosque.traducia.dto;

/**
 * Respuesta JSON del endpoint de transcripción de Groq.
 *
 * Solo se representa el campo utilizado por la aplicación.
 */
public class GroqTranscripcionResponseDTO {
	private String text;

	
	/**
	 * Crea una respuesta vacía para representar los datos
	 * deserializados desde el JSON del proveedor Groq.
	 */
	public GroqTranscripcionResponseDTO() {
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}