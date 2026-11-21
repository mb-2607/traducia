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
	private final TranscripcionService transcripcionService;

	public TranscripcionController(TranscripcionService transcripcionService) {
		this.transcripcionService = transcripcionService;
	}

	@PostMapping(path = "/transcribir", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TranscripcionResponseDTO> transcribir(@RequestPart("archivo") MultipartFile archivo,
			@RequestParam(name = "idioma", required = false) String idioma) throws IOException {
		return ResponseEntity.ok(transcripcionService.transcribir(archivo, idioma));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> manejarAudioInvalido(IllegalArgumentException exception) {

		return ResponseEntity.badRequest().body(Map.of("mensaje", exception.getMessage()));
	}

	@ExceptionHandler(IOException.class)
	public ResponseEntity<Map<String, String>> manejarLectura() {
		return ResponseEntity.status(500).body(Map.of("mensaje", "No fue posible leer el audio"));
	}

	/**
	 * Distingue errores del proveedor de errores de autenticación del usuario. Una
	 * API key externa inválida no se devuelve como si hubiera fallado el JWT de
	 * TraducIA.
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