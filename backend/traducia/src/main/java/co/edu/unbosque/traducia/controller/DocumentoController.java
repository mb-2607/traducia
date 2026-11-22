package co.edu.unbosque.traducia.controller;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import co.edu.unbosque.traducia.dto.DocumentoExtraidoResponseDTO;
import co.edu.unbosque.traducia.dto.SolicitudTraduccionResponseDTO;
import co.edu.unbosque.traducia.dto.TraduccionRequestDTO;
import co.edu.unbosque.traducia.exception.DocumentoException;
import co.edu.unbosque.traducia.exception.ExternalAPIException;
import co.edu.unbosque.traducia.exception.PlanCharacterLimitExceededException;
import co.edu.unbosque.traducia.service.DocumentoService;
import co.edu.unbosque.traducia.service.TraduccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Recibe archivos y delega extracción y traducción en sus servicios.
 * /traductor/** conserva la autorización USER o ADMIN de SecurityConfig.
 */
@RestController
@RequestMapping("/traductor")
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "http://localhost:4200", "${front.url}" })
@Tag(name = "Documentos", description = "Extracción de texto y traducción de archivos.")
@SecurityRequirement(name = "bearerAuth")
public class DocumentoController {

	private final DocumentoService documentoService;
	private final TraduccionService traduccionService;

	/**
	 * Inyecta servicios sin introducir peticiones externas en el controlador.
	 *
	 * @param documentoService  extracción documental
	 * @param traduccionService traducción existente y validación del plan
	 */
	public DocumentoController(DocumentoService documentoService, TraduccionService traduccionService) {
		this.documentoService = documentoService;
		this.traduccionService = traduccionService;
	}

	/**
	 * Permite revisar y editar el texto antes de traducirlo.
	 *
	 * @param archivo        archivo que se desea leer
	 * @param idiomaOcr      idioma del documento
	 * @param authentication sesión validada por Spring Security
	 * @return texto extraído y límite del plan; estado 200
	 */
	@Operation(summary = "Extraer texto de un documento o imagen")
	@PostMapping(path = "/extraer-documento", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DocumentoExtraidoResponseDTO> extraer(@RequestPart("archivo") MultipartFile archivo,
			@RequestParam(defaultValue = "es") String idiomaOcr, Authentication authentication) {
		return ResponseEntity.ok(documentoService.extraer(archivo, idiomaOcr, authentication.getName()));
	}

	/**
	 * Extrae y traduce con los proveedores del plan. Una extracción excesiva se
	 * rechaza antes de invocar el servicio de traducción.
	 *
	 * @param archivo        documento que se desea traducir
	 * @param origen         código del idioma de origen del catálogo de TraducIA
	 * @param destino        código del idioma de destino del catálogo de TraducIA
	 * @param authentication sesión validada
	 * @return solicitud persistida por TraduccionService; estado 201
	 */
	@Operation(summary = "Traducir directamente el texto de un archivo")
	@ApiResponse(responseCode = "201", description = "Solicitud de traducción creada")
	@PostMapping(path = "/traducir-documento", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SolicitudTraduccionResponseDTO> traducir(@RequestPart("archivo") MultipartFile archivo,
			@RequestParam String origen, @RequestParam String destino, Authentication authentication) {
		if (origen.isBlank() || destino.isBlank()) {
			throw new DocumentoException(400, "Selecciona los idiomas de origen y destino.");
		}
		String nombre = authentication.getName();
		DocumentoExtraidoResponseDTO extraido = documentoService.extraer(archivo, origen, nombre);
		if (extraido.excedeLimite()) {
			throw new DocumentoException(400,
					"El documento contiene " + extraido.caracteres() + " caracteres y tu plan permite "
							+ extraido.limiteCaracteres() + ". Extrae y edita el texto antes de traducir.");
		}
		TraduccionRequestDTO solicitud = new TraduccionRequestDTO(origen, destino, extraido.texto());
		return ResponseEntity.status(201).body(traduccionService.traducir(solicitud, nombre));
	}

	/**
	 * Convierte un error del documento en una respuesta pública estructurada.
	 *
	 * @param error error controlado
	 * @return respuesta sin credenciales ni contenido externo
	 */
	@ExceptionHandler(DocumentoException.class)
	public ProblemDetail manejarDocumento(DocumentoException error) {
		return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(error.getStatusCode()), error.getMessage());
	}

	/**
	 * No confunde un 401 de LlamaParse con una sesión JWT inválida de TraducIA.
	 *
	 * @param error fallo de comunicación o respuesta HTTP externa
	 * @return error del proveedor traducido a un código público apropiado
	 */
	@ExceptionHandler(ExternalAPIException.class)
	public ProblemDetail manejarProveedor(ExternalAPIException error) {
		int codigo = error.getStatusCode();
		int publico = switch (codigo) {
		case 400, 413, 415, 422 -> 422;
		case 429, 503 -> 503;
		case 504 -> 504;
		default -> 502;
		};
		String mensaje = switch (publico) {
		case 422 -> "LlamaParse rechazó el archivo o sus opciones. Revisa formato, idioma y límites.";
		case 503 -> "El servicio de documentos está ocupado o no disponible. Inténtalo después.";
		case 504 -> "Se agotó el tiempo de extracción. Prueba con un documento más pequeño.";
		default -> "No se pudo obtener una respuesta válida de LlamaParse. Revisa conexión y configuración.";
		};
		if (codigo == 401 || codigo == 403) {
			mensaje = "LlamaParse rechazó la clave de servicio. Revisa llamaparse.api.key y la cuenta de LlamaCloud.";
		}
		return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(publico), mensaje);
	}

	/** @return estado 413 si Spring rechaza el tamaño del archivo o petición. */
	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ProblemDetail manejarTamanio() {
		return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(413),
				"El archivo o la petición supera el tamaño máximo de subida.");
	}

	/** @return error si el plan cambió entre la extracción y la traducción. */
	@ExceptionHandler(PlanCharacterLimitExceededException.class)
	public ProblemDetail manejarLimitePlan() {
		return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(400),
				"El texto supera el límite del plan actual. Revisa el texto y vuelve a intentarlo.");
	}
}
