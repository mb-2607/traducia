package co.edu.unbosque.traducia.controller;

import java.io.IOException;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import co.edu.unbosque.traducia.dto.TranscripcionResponseDTO;
import co.edu.unbosque.traducia.exception.ExternalAPIException;
import co.edu.unbosque.traducia.service.TranscripcionService;

/** Recibe grabaciones para convertirlas en texto. */
@RestController
@RequestMapping("/traductor")
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "http://localhost:4200", "${front.url}" })
public class TranscripcionController {

	/** Servicio encargado de procesar las solicitudes de transcripción. */
	private final TranscripcionService transcripcionService;

	/**
	 * Construye el controlador e inyecta el servicio de transcripción.
	 *
	 * @param transcripcionService servicio encargado de validar el audio y obtener
	 *                             su transcripción
	 */
	public TranscripcionController(TranscripcionService transcripcionService) {
		this.transcripcionService = transcripcionService;
	}

	/**
	 * Recibe un archivo de audio y solicita su conversión a texto.
	 *
	 * @param archivo archivo de audio enviado mediante multipart/form-data
	 * @param idioma  código opcional del idioma hablado; puede omitirse
	 * @return respuesta HTTP 200 con el resultado de la transcripción
	 * @throws IOException si ocurre un error durante la lectura del audio
	 */
	@PostMapping(path = "/transcribir", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TranscripcionResponseDTO> transcribir(@RequestPart("archivo") MultipartFile archivo,
			@RequestParam(name = "idioma", required = false) String idioma) throws IOException {

		return ResponseEntity.ok(transcripcionService.transcribir(archivo, idioma));
	}

	/**
	 * Convierte los errores de validación del audio en una respuesta HTTP 400.
	 *
	 * @param exception excepción que describe el motivo del rechazo
	 * @return respuesta con el mensaje del error de validación
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> manejarAudioInvalido(IllegalArgumentException exception) {

		return ResponseEntity.badRequest().body(Map.of("mensaje", exception.getMessage()));
	}

	/**
	 * Gestiona los errores de lectura del archivo de audio.
	 *
	 * @return respuesta HTTP 500 con un mensaje público sobre el fallo de lectura
	 */
	@ExceptionHandler(IOException.class)
	public ResponseEntity<Map<String, String>> manejarLectura() {
		return ResponseEntity.status(500).body(Map.of("mensaje", "No fue posible leer el audio"));
	}

	/**
	 * Distingue errores del proveedor de errores de autenticación del usuario. Una
	 * API key externa inválida no se devuelve como si hubiera fallado el JWT de
	 * TraducIA.
	 *
	 * @param exception excepción con el código del fallo de la petición externa
	 * @return respuesta HTTP con el estado correspondiente y un mensaje público que
	 *         no expone credenciales ni detalles internos del proveedor
	 */
	@ExceptionHandler(ExternalAPIException.class)
	public ResponseEntity<Map<String, String>> manejarProveedor(ExternalAPIException exception) {

		int codigo = exception.getStatusCode();

		if (codigo == 429 || codigo == 503) {
			return ResponseEntity.status(503)
					.body(Map.of("mensaje", "La transcripción no está disponible temporalmente"));
		}

		if (codigo == 504) {
			return ResponseEntity.status(504).body(Map.of("mensaje", "El proveedor tardó demasiado en responder"));
		}

		if (codigo == 400 || codigo == 413 || codigo == 422) {
			return ResponseEntity.badRequest()
					.body(Map.of("mensaje", "El proveedor no pudo procesar el audio o el idioma"));
		}

		return ResponseEntity.status(502).body(Map.of("mensaje", "No fue posible completar la transcripción"));
	}
}