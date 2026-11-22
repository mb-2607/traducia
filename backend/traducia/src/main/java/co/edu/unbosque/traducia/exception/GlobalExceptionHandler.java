package co.edu.unbosque.traducia.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador global de excepciones encargado de interceptar y estructurar los
 * errores de validación de los DTO de entrada en toda la aplicación.
 *
 * @author
 * @version 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * Gestiona los errores producidos cuando falla la validación de un DTO anotado
	 * con {@code @Valid} en cualquier controlador.
	 *
	 * @param ex excepción lanzada por Spring al detectar restricciones incumplidas.
	 * @return mapa con el detalle de los campos inválidos bajo estado HTTP 400 (Bad
	 *         Request).
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Map<String, Object> manejarValidaciones(MethodArgumentNotValidException ex) {
		Map<String, String> erroresCampos = new LinkedHashMap<>();

		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			String mensaje = error.getDefaultMessage() != null ? error.getDefaultMessage() : "Valor inválido";
			erroresCampos.putIfAbsent(error.getField(), mensaje);
		}

		Map<String, Object> cuerpo = new LinkedHashMap<>();
		cuerpo.put("timestamp", LocalDateTime.now());
		cuerpo.put("status", HttpStatus.BAD_REQUEST.value());
		cuerpo.put("error", "Error de validación");
		cuerpo.put("errores", erroresCampos);

		return cuerpo;
	}

	/**
	 * Atiende archivos demasiado grandes, incluso cuando Spring los rechaza antes
	 * de ejecutar el controlador.
	 */
	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<Map<String, String>> manejarArchivoGrande(MaxUploadSizeExceededException exception) {
		return ResponseEntity.status(413).body(Map.of("mensaje", "El archivo supera el tamaño máximo permitido"));
	}
}