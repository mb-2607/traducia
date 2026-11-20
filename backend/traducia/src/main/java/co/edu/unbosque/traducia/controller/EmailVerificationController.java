package co.edu.unbosque.traducia.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.traducia.dto.ResendVerificationRequestDTO;
import co.edu.unbosque.traducia.exception.InvalidTokenException;
import co.edu.unbosque.traducia.exception.UserNotFoundException;
import co.edu.unbosque.traducia.service.EmailVerificationService;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST encargado de exponer los endpoints de verificación de correo electrónico.
 */
@Tag(
	name = "Verificación de correo",
	description = "Endpoints para activar cuentas recién registradas y reenviar enlaces de verificación."
)
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "${front.url}",
		"http://localhost:4200" })
@RestController
@RequestMapping("/auth")
public class EmailVerificationController {

	/**
	 * Servicio que gestiona la verificación de correo.
	 */
	private final EmailVerificationService emailVerificationService;

	/**
	 * Construye el controlador con sus dependencias.
	 *
	 * @param emailVerificationService servicio de verificación de correo
	 */
	public EmailVerificationController(EmailVerificationService emailVerificationService) {
		this.emailVerificationService = emailVerificationService;
	}

	/**
	 * Verifica el correo electrónico del usuario a partir del token enviado en el enlace.
	 *
	 * @param token código único de verificación
	 * @return respuesta HTTP 200 confirmando la activación de la cuenta
	 */
	@GetMapping("/verify-email")
	public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
		emailVerificationService.verificarCorreo(token);
		return ResponseEntity.ok("Correo verificado correctamente. Ya puedes iniciar sesión");
	}

	/**
	 * Reenvía un nuevo enlace de verificación al correo electrónico indicado.
	 *
	 * @param request petición con el correo del usuario
	 * @return respuesta HTTP 200 confirmando el envío del nuevo enlace
	 */
	@PostMapping("/resend-verification")
	public ResponseEntity<String> resendVerification(@RequestBody ResendVerificationRequestDTO request) {
		emailVerificationService.reenviarVerificacion(request);
		return ResponseEntity.ok("Se ha enviado un nuevo enlace de verificación a tu correo electrónico");
	}

	/**
	 * Gestiona errores cuando el token de verificación es inválido, usado o expirado.
	 *
	 * @param e excepción con el motivo del rechazo
	 * @return respuesta HTTP 400 con el mensaje de error
	 */
	@ExceptionHandler(InvalidTokenException.class)
	public ResponseEntity<String> manejarInvalidTokenException(InvalidTokenException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
	}

	/**
	 * Gestiona errores por parámetros obligatorios vacíos.
	 *
	 * @param e excepción de argumento inválido
	 * @return respuesta HTTP 400 con el mensaje de error
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<String> manejarIllegalArgumentException(IllegalArgumentException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
	}

	/**
	 * Gestiona el caso en el que el correo no existe en el sistema.
	 *
	 * @return respuesta HTTP 404 indicando que el usuario no existe
	 */
	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<String> manejarUserNotFoundException() {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No existe un usuario registrado con ese correo");
	}

	/**
	 * Gestiona intentos de verificar o reenviar sobre cuentas ya activas o bloqueadas.
	 *
	 * @param e excepción con el estado de la cuenta
	 * @return respuesta HTTP 409 con el mensaje descriptivo
	 */
	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<String> manejarIllegalStateException(IllegalStateException e) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
	}
}