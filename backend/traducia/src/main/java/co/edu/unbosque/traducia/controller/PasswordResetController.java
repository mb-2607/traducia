package co.edu.unbosque.traducia.controller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.traducia.dto.ForgotPasswordRequestDTO;
import co.edu.unbosque.traducia.dto.ResetPasswordRequestDTO;
import co.edu.unbosque.traducia.exception.InvalidPasswordException;
import co.edu.unbosque.traducia.exception.InvalidTokenException;
import co.edu.unbosque.traducia.exception.UserNotFoundException;
import co.edu.unbosque.traducia.service.PasswordResetService;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST público encargado de gestionar la recuperación de contraseñas olvidadas.
 * <p>
 * Expone los endpoints bajo la ruta base {@code /auth} para solicitar el envío
 * del correo con el enlace de restablecimiento y para establecer una nueva
 * contraseña validando el token recibido desde el frontend.
 *
 * @author
 * @version 1.0
 * @see PasswordResetService
 * @see ForgotPasswordRequestDTO
 * @see ResetPasswordRequestDTO
 */
@Tag(
	name = "Recuperación de contraseña",
	description = "Endpoints públicos para solicitar y completar el restablecimiento de contraseña por correo electrónico."
)
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "${front.url}",
		"http://localhost:4200" })
@RestController
@RequestMapping("/auth")

public class PasswordResetController {
	
	/**
	 * Servicio encargado de gestionar los tokens y el envío de correos de recuperación.
	 */
	private final PasswordResetService passwordResetService;

	/**
	 * Crea una nueva instancia del controlador inyectando el servicio de recuperación.
	 *
	 * @param passwordResetService servicio utilizado para procesar las solicitudes de recuperación.
	 */
	public PasswordResetController(PasswordResetService passwordResetService) {
		this.passwordResetService = passwordResetService;
	}

	/**
	 * Inicia el proceso de recuperación enviando un correo con el enlace de restablecimiento.
	 *
	 * @param request DTO que contiene el correo electrónico registrado del usuario.
	 * @return {@link ResponseEntity} con estado {@code 200 OK} y un mensaje de confirmación.
	 */
	@PostMapping(path = "forgot-password")
	public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequestDTO request) {
		passwordResetService.solicitarRecuperacion(request);
		return ResponseEntity.ok("Se ha enviado un enlace de recuperación a tu correo electrónico");
	}

	/**
	 * Completa el restablecimiento de contraseña utilizando el token enviado por correo.
	 *
	 * @param request DTO con el token de recuperación y la nueva contraseña elegida.
	 * @return {@link ResponseEntity} con estado {@code 200 OK} y un mensaje de confirmación.
	 */
	@PostMapping(path = "reset-password")
	public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequestDTO request) {
		passwordResetService.restablecerPassword(request);
		return ResponseEntity.ok("Contraseña restablecida correctamente");
	}

	/**
	 * Maneja la excepción lanzada cuando no existe un usuario asociado al correo suministrado.
	 *
	 * @return {@link ResponseEntity} con estado {@code 404 Not Found} y el mensaje descriptivo.
	 */
	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<String> manejarUserNotFoundException() {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body("No existe un usuario registrado con ese correo");
	}

	/**
	 * Maneja la excepción lanzada cuando el token de recuperación es inválido, expiró o ya fue usado.
	 *
	 * @param e excepción capturada con el detalle del error del token.
	 * @return {@link ResponseEntity} con estado {@code 400 Bad Request} y el mensaje de error.
	 */
	@ExceptionHandler(InvalidTokenException.class)
	public ResponseEntity<String> manejarInvalidTokenException(InvalidTokenException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(e.getMessage());
	}

	/**
	 * Maneja la excepción lanzada cuando la nueva contraseña no cumple las reglas mínimas.
	 *
	 * @param e excepción capturada con el motivo de invalidez.
	 * @return {@link ResponseEntity} con estado {@code 400 Bad Request} y el mensaje de error.
	 */
	@ExceptionHandler(InvalidPasswordException.class)
	public ResponseEntity<String> manejarInvalidPasswordException(InvalidPasswordException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(e.getMessage());
	}

	/**
	 * Maneja la excepción lanzada cuando faltan datos obligatorios en la solicitud.
	 *
	 * @param e excepción capturada con el detalle del campo faltante.
	 * @return {@link ResponseEntity} con estado {@code 400 Bad Request} y el mensaje de error.
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<String> manejarIllegalArgumentException(IllegalArgumentException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(e.getMessage());
	}

	/**
	 * Maneja la excepción lanzada cuando la cuenta está deshabilitada o bloqueada.
	 *
	 * @param e excepción capturada con el motivo del rechazo.
	 * @return {@link ResponseEntity} con estado {@code 403 Forbidden} y el mensaje de error.
	 */
	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<String> manejarIllegalStateException(IllegalStateException e) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(e.getMessage());
	}

}
