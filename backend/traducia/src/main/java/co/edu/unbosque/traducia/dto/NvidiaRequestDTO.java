package co.edu.unbosque.traducia.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO (Data Transfer Object) que representa el cuerpo de una petición enviada a
 * la API de chat de NVIDIA.
 * <p>
 * Esta clase se serializa a JSON para enviarse como payload en la solicitud.
 * Los nombres de los atributos (por ejemplo {@code max_tokens}) coinciden con
 * los campos que espera la API, por lo que se mantienen en <i>snake_case</i>.
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
	/** Identificador del modelo de lenguaje que procesará la petición. */
	private String model;
	/**
	 * Indica si la respuesta se entrega en modo streaming (por fragmentos)
	 * ({@code true}) o completa en una sola respuesta ({@code false}).
	 */
	private boolean stream;
	/**
	 * Lista de mensajes que conforman la conversación enviada al modelo (por
	 * ejemplo, mensajes de sistema, usuario y asistente).
	 */
	private List<NvidiaMessageRequestDTO> messages;
	/** Número máximo de tokens que el modelo puede generar en la respuesta. */
	private int max_tokens;

	/**
	 * Constructor por defecto.
	 * <p>
	 * Inicializa la lista de mensajes vacía para evitar
	 * {@code NullPointerException} al agregar mensajes posteriormente. Necesario
	 * también para la deserialización de JSON.
	 * </p>
	 */
	public NvidiaRequestDTO() {
		messages = new ArrayList<>();
	}

	/**
	 * Constructor con todos los parámetros.
	 *
	 * @param model      identificador del modelo a utilizar
	 * @param stream     {@code true} para recibir la respuesta en streaming
	 * @param messages   lista de mensajes de la conversación
	 * @param max_tokens número máximo de tokens a generar en la respuesta
	 */
	public NvidiaRequestDTO(String model, boolean stream, List<NvidiaMessageRequestDTO> messages, int max_tokens) {
		this.model = model;
		this.stream = stream;
		this.messages = messages;
		this.max_tokens = max_tokens;
	}

	/**
	 * Obtiene el identificador del modelo.
	 *
	 * @return el nombre del modelo configurado
	 */
	public String getModel() {
		return model;
	}

	/**
	 * Establece el identificador del modelo.
	 *
	 * @param model el nombre del modelo a utilizar
	 */
	public void setModel(String model) {
		this.model = model;
	}

	/**
	 * Indica si la respuesta se solicitará en modo streaming.
	 *
	 * @return {@code true} si el streaming está activado; {@code false} en caso
	 *         contrario
	 */
	public boolean isStream() {
		return stream;
	}

	/**
	 * Establece si la respuesta se solicitará en modo streaming.
	 *
	 * @param stream {@code true} para activar el streaming
	 */
	public void setStream(boolean stream) {
		this.stream = stream;
	}

	/**
	 * Obtiene la lista de mensajes de la conversación.
	 *
	 * @return lista de {@link NvidiaMessageRequestDTO}
	 */
	public List<NvidiaMessageRequestDTO> getMessages() {
		return messages;
	}

	/**
	 * Establece la lista de mensajes de la conversación.
	 *
	 * @param messages lista de {@link NvidiaMessageRequestDTO} a enviar al modelo
	 */
	public void setMessages(List<NvidiaMessageRequestDTO> messages) {
		this.messages = messages;
	}

	/**
	 * Obtiene el límite máximo de tokens de la respuesta.
	 *
	 * @return número máximo de tokens a generar
	 */
	public int getMax_tokens() {
		return max_tokens;
	}

	/**
	 * Establece el límite máximo de tokens de la respuesta.
	 *
	 * @param max_tokens número máximo de tokens a generar
	 */
	public void setMax_tokens(int max_tokens) {
		this.max_tokens = max_tokens;
	}

}
