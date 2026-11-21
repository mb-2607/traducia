package co.edu.unbosque.traducia.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO (Data Transfer Object) que representa el cuerpo de una petición enviada
 * a la API de chat de NVIDIA.
 * <p>
 * Esta clase se serializa a JSON para enviarse como payload en la solicitud.
 * Los nombres de los atributos (por ejemplo {@code max_tokens}) coinciden con los
 * campos que espera la API, por lo que se mantienen en <i>snake_case</i>.
 * </p>
 *
 * <pre>
 * Ejemplo de JSON generado:
 * {
 *   "model": "meta/llama-3.1-8b-instruct",
 *   "stream": false,
 *   "messages": [ { ... } ],
 *   "max_tokens": 1024
 * }
 * </pre>
 *
 * @see NvidiaMessageRequestDTO
 */
public class NvidiaRequestDTO {
	private String model;
	private boolean stream;
	private List<NvidiaMessageRequestDTO> messages;
	private int max_tokens;

	public NvidiaRequestDTO() {
		messages = new ArrayList<>();
	}

	public NvidiaRequestDTO(String model, boolean stream, List<NvidiaMessageRequestDTO> messages, int max_tokens) {
		/** Identificador del modelo de lenguaje que procesará la petición. */
		this.model = model;
		this.stream = stream;
		this.messages = messages;
		this.max_tokens = max_tokens;
	}
	
	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public boolean isStream() {
		return stream;
	}

	public void setStream(boolean stream) {
		this.stream = stream;
	}

	public List<NvidiaMessageRequestDTO> getMessages() {
		return messages;
	}

	public void setMessages(List<NvidiaMessageRequestDTO> messages) {
		this.messages = messages;
	}

	public int getMax_tokens() {
		return max_tokens;
	}

	public void setMax_tokens(int max_tokens) {
		this.max_tokens = max_tokens;
	}

}
