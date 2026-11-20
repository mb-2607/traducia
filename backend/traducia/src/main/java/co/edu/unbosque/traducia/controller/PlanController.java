package co.edu.unbosque.traducia.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.traducia.dto.ChangePlanRequestDTO;
import co.edu.unbosque.traducia.dto.UserResponseDTO;
import co.edu.unbosque.traducia.entity.User;
import co.edu.unbosque.traducia.exception.UserNotFoundException;
import co.edu.unbosque.traducia.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST encargado de gestionar el cambio de plan del usuario autenticado.
 * <p>
 * Expone los endpoints bajo la ruta base {@code /plan} para que cualquier usuario
 * autenticado pueda actualizar su propio tipo de plan sin requerir permisos
 * administrativos. Delega la lógica de negocio en {@link UserService} y maneja
 * las excepciones relacionadas con la solicitud y la existencia del usuario.
 *
 * @author
 * @version 1.0
 * @see UserService
 * @see ChangePlanRequestDTO
 * @see UserResponseDTO
 */


@Tag(
	    name = "Planes",
	    description = "Endpoints para que el usuario autenticado gestione su propio tipo de plan."
	)
	@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "${front.url}",
			"http://localhost:4200" })
	@RestController
	@RequestMapping("/plan")
public class PlanController {
	
	/**
	 * Servicio encargado de ejecutar las operaciones de negocio relacionadas con los usuarios.
	 */
	
	private final UserService userService;
	
	/**
	 * Crea una nueva instancia del controlador inyectando el servicio de usuarios.
	 *
	 * @param userService servicio utilizado para actualizar el plan del usuario.
	 */
	

	public PlanController(UserService userService) {
		this.userService = userService;
	}
	
	/**
	 * Actualiza el tipo de plan del usuario autenticado actualmente.
	 * <p>
	 * Valida que el cuerpo de la solicitud incluya un tipo de plan no nulo,
	 * obtiene el identificador del usuario a partir del contexto de autenticación
	 * y delega la actualización en {@link UserService}.
	 *
	 * @param request objeto DTO que contiene el nuevo tipo de plan seleccionado.
	 * @param authentication información de autenticación del usuario que realiza la solicitud.
	 * @return {@link ResponseEntity} con estado {@code 200 OK} y los datos actualizados del usuario en formato {@link UserResponseDTO}.
	 * @throws IllegalArgumentException si la solicitud o el tipo de plan enviado son nulos.
	 */
	

	@PutMapping(path = "cambiar")
	public ResponseEntity<UserResponseDTO> cambiarPlan(@RequestBody ChangePlanRequestDTO request,
			Authentication authentication) {
		if (request == null || request.getPlan() == null) {
			throw new IllegalArgumentException("El tipo de plan es obligatorio");
		}

		User user = (User) authentication.getPrincipal();
		UserResponseDTO userResponse = userService.cambiarPlan(user.getId(), request.getPlan());
		return ResponseEntity.ok(userResponse);
	}
	
	/**
	 * Maneja la excepción lanzada cuando no se encuentra el usuario autenticado en la base de datos.
	 *
	 * @return {@link ResponseEntity} con estado {@code 404 Not Found} y el mensaje descriptivo.
	 */
	

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<String> manejarUserNotFoundException() {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body("El usuario no existe");
	}
	
	/**
	 * Maneja la excepción lanzada cuando los argumentos de la solicitud son inválidos o están incompletos.
	 *
	 * @param e excepción capturada con el detalle del error de validación.
	 * @return {@link ResponseEntity} con estado {@code 400 Bad Request} y el mensaje de error.
	 */
	

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<String> manejarIllegalArgumentException(IllegalArgumentException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(e.getMessage());
	}
	
	/**
	 * Maneja la excepción lanzada cuando el cuerpo JSON recibido no puede deserializarse a un tipo de plan válido.
	 *
	 * @return {@link ResponseEntity} con estado {@code 400 Bad Request} y el mensaje descriptivo.
	 */
	

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<String> manejarHttpMessageNotReadableException() {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body("Tipo de plan inválido");
	}
}

