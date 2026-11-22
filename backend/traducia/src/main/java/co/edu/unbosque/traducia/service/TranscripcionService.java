package co.edu.unbosque.traducia.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import co.edu.unbosque.traducia.dto.GroqTranscripcionResponseDTO;
import co.edu.unbosque.traducia.dto.TranscripcionResponseDTO;
import co.edu.unbosque.traducia.entity.AuthenticationType;
import co.edu.unbosque.traducia.exception.ExternalAPIException;

/**
 * Valida audios y solicita su transcripción.
 *
 * La comunicación HTTP se realiza exclusivamente mediante
 * ExternalHTTPRequestHandler.
 */
@Service
public class TranscripcionService {
	private static final String URL_TRANSCRIPCION = "https://api.groq.com/openai/v1/audio/transcriptions";
	private static final int MAXIMO_BYTES = 10 * 1024 * 1024;
	private static final Set<String> EXTENSIONES = Set.of("flac", "mp3", "mp4", "mpeg", "mpga", "m4a", "ogg", "wav",
			"webm");
	private final String apiKey;
	private final String modelo;

	/**
	 * Construye el servicio con la configuración del proveedor de transcripción.
	 *
	 * @param apiKey clave de acceso a Groq, suministrada por la configuración del
	 *               backend
	 * @param modelo identificador del modelo utilizado para transcribir el audio
	 */
	public TranscripcionService(@Value("${groq.api.key:}") String apiKey,
			@Value("${groq.transcripcion.model:whisper-large-v3-turbo}") String modelo) {

		this.apiKey = apiKey;
		this.modelo = modelo;
	}

	/**
	 * Transcribe un archivo sin traducirlo ni recortar su contenido.
	 *
	 * @param archivo audio enviado por el frontend
	 * @param idioma  idioma opcional; por ejemplo es, en o es-CO
	 * @return texto reconocido y cantidad de caracteres
	 */
	public TranscripcionResponseDTO transcribir(MultipartFile archivo, String idioma) throws IOException {
		String extension = validarArchivo(archivo);
		String idiomaNormalizado = normalizarIdioma(idioma);
		if (apiKey.isBlank() || modelo.isBlank()) {
			throw new ExternalAPIException(503);
		}

		byte[] contenido;
		try (InputStream entrada = archivo.getInputStream()) {
			contenido = entrada.readNBytes(MAXIMO_BYTES + 1);
		}

		if (contenido.length > MAXIMO_BYTES) {
			throw new MaxUploadSizeExceededException(MAXIMO_BYTES);
		}

		if (contenido.length == 0) {
			throw new IllegalArgumentException("El archivo de audio está vacío");
		}

		Map<String, String> campos = new LinkedHashMap<>();
		campos.put("model", modelo);
		campos.put("response_format", "json");
		if (idiomaNormalizado != null) {
			campos.put("language", idiomaNormalizado);
		}

		GroqTranscripcionResponseDTO respuesta = ExternalHTTPRequestHandler.doPostMultipartAndConvertToDTO(
				URL_TRANSCRIPCION, apiKey, AuthenticationType.BEARER, campos, contenido, "audio." + extension,
				GroqTranscripcionResponseDTO.class);

		String texto = respuesta.getText();
		if (texto == null || texto.isBlank()) {
			throw new IllegalArgumentException("No se obtuvo texto del audio enviado");
		}

		return new TranscripcionResponseDTO(texto);
	}

	/**
	 * Valida la presencia, el tamaño, el nombre y la extensión del archivo de
	 * audio.
	 *
	 * @param archivo archivo recibido para su posterior transcripción
	 * @return extensión admitida del archivo, normalizada a minúsculas
	 * @throws IllegalArgumentException       si el archivo es null, está vacío, no
	 *                                        tiene nombre o su extensión no es
	 *                                        válida o no está admitida
	 * @throws MaxUploadSizeExceededException si el tamaño supera el máximo
	 *                                        permitido
	 */
	private String validarArchivo(MultipartFile archivo) {
		if (archivo == null || archivo.isEmpty()) {
			throw new IllegalArgumentException("Debes enviar un archivo de audio");
		}

		if (archivo.getSize() > MAXIMO_BYTES) {
			throw new MaxUploadSizeExceededException(MAXIMO_BYTES);
		}

		String nombre = archivo.getOriginalFilename();
		if (nombre == null) {
			throw new IllegalArgumentException("El archivo debe incluir nombre y extensión");
		}

		int punto = nombre.lastIndexOf('.');
		if (punto < 0 || punto == nombre.length() - 1) {
			throw new IllegalArgumentException("El archivo debe incluir una extensión válida");
		}

		String extension = nombre.substring(punto + 1).toLowerCase(Locale.ROOT);
		if (!EXTENSIONES.contains(extension)) {
			throw new IllegalArgumentException("Formato de audio no admitido");
		}

		return extension;
	}

	/**
	 * Groq recibe códigos ISO-639-1. Convierte, por ejemplo, es-CO a es. Si no se
	 * indica idioma, el proveedor intenta detectarlo.
	 */
	private String normalizarIdioma(String idioma) {
		if (idioma == null || idioma.isBlank()) {
			return null;
		}

		String codigo = idioma.trim().toLowerCase(Locale.ROOT).split("-", 2)[0];
		if (!codigo.matches("[a-z]{2}")) {
			throw new IllegalArgumentException("El idioma debe utilizar un código como es o en");
		}

		return codigo;
	}
}