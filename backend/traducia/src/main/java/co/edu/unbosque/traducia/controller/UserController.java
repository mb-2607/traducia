package co.edu.unbosque.traducia.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.traducia.dto.AdminChangePasswordRequestDTO;
import co.edu.unbosque.traducia.dto.AdminCreateUserRequestDTO;
import co.edu.unbosque.traducia.dto.UpdateUserRequestDTO;
import co.edu.unbosque.traducia.dto.UserResponseDTO;
import co.edu.unbosque.traducia.entity.User;
import co.edu.unbosque.traducia.exception.EmailAlreadyExistsException;
import co.edu.unbosque.traducia.exception.InvalidPasswordException;
import co.edu.unbosque.traducia.exception.SelfActionNotAllowedException;
import co.edu.unbosque.traducia.exception.UserNotFoundException;
import co.edu.unbosque.traducia.exception.UsernameAlreadyExistsException;
import co.edu.unbosque.traducia.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controlador REST encargado de gestionar las operaciones administrativas sobre los usuarios.
 * <p>
 * Expone los endpoints bajo la ruta base {@code /user} para listar, consultar,
 * crear, actualizar, cambiar contraseñas y eliminar usuarios. Delega la lógica
 * de negocio en {@link UserService}, valida que un administrador no realice
 * acciones de modificación o eliminación sobre su propia cuenta y maneja de forma
 * centralizada las excepciones de negocio con sus respectivos códigos de estado HTTP.
 * </p>
 *
 * @author
 * @version 1.0
 * @see UserService
 * @see UserResponseDTO
 * @see AdminCreateUserRequestDTO
 * @see UpdateUserRequestDTO
 * @see AdminChangePasswordRequestDTO
 */
@Tag(
	name = "Usuarios",
	description = "Endpoints para la gestión de usuarios por parte de los administradores."
)
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "${front.url}",
		"http://localhost:4200" })
@RestController
@RequestMapping("/user")
public class UserController {

	/**
	 * Servicio encargado de ejecutar las operaciones de negocio relacionadas con los usuarios.
	 */
	private final UserService userService;

	/**
	 * Crea una nueva instancia del controlador inyectando el servicio de usuarios.
	 *
	 * @param userService servicio utilizado para gestionar los usuarios del sistema.
	 */
	public UserController(UserService userService) {
		this.userService = userService;
	}

	/**
	 * Obtiene el listado completo de todos los usuarios registrados.
	 *
	 * @return {@link ResponseEntity} con estado {@code 200 OK} y la lista de usuarios en formato {@link UserResponseDTO}.
	 */
	@GetMapping(path = "listar")
	public ResponseEntity<List<UserResponseDTO>> listar() {
		List<UserResponseDTO> usuarios = userService.listar();
		return ResponseEntity.ok(usuarios);
	}

	/**
	 * Obtiene los usuarios registrados de forma paginada y ordenada según los parámetros recibidos.
	 *
	 * @param pageable configuración de paginación y ordenamiento de la consulta.
	 * @return {@link ResponseEntity} con estado {@code 200 OK} y la página de usuarios en formato {@link UserResponseDTO}.
	 */
	@GetMapping(path = "paginado")
	public ResponseEntity<Page<UserResponseDTO>> listarPaginado(Pageable pageable) {
		Page<UserResponseDTO> usuarios = userService.listar(pageable);
		return ResponseEntity.ok(usuarios);
	}

	/**
	 * Consulta la información de un usuario específico a partir de su identificador.
	 *
	 * @param id identificador único del usuario que se desea consultar.
	 * @return {@link ResponseEntity} con estado {@code 200 OK} y los datos del usuario encontrado.
	 */
	@GetMapping(path = "{id}")
	public ResponseEntity<UserResponseDTO> obtenerPorId(@PathVariable Long id) {
		UserResponseDTO userResponse = userService.obtenerPorId(id);
		return ResponseEntity.ok(userResponse);
	}

	/**
	 * Crea un nuevo usuario desde el panel administrativo con los datos suministrados.
	 *
	 * @param request objeto DTO con la información necesaria para crear el usuario, incluyendo su rol y plan.
	 * @return {@link ResponseEntity} con estado {@code 201 Created} y la información del usuario creado.
	 */
	@PostMapping(path = "crear")
	public ResponseEntity<UserResponseDTO> crear(@Valid @RequestBody AdminCreateUserRequestDTO request) {
		UserResponseDTO userResponse = userService.crear(request);
		return new ResponseEntity<UserResponseDTO>(userResponse, HttpStatus.CREATED);
	}

	/**
	 * Actualiza los datos de un usuario existente a partir de su identificador.
	 * <p>
	 * Verifica mediante el contexto de autenticación que el administrador no intente
	 * actualizar su propia cuenta antes de delegar la operación en el servicio.
	 * </p>
	 *
	 * @param id identificador único del usuario que se desea actualizar.
	 * @param request objeto DTO con los nuevos datos del usuario.
	 * @param authentication información de autenticación del administrador que realiza la solicitud.
	 * @return {@link ResponseEntity} con estado {@code 200 OK} y los datos actualizados del usuario.
	 * @throws SelfActionNotAllowedException si el administrador intenta actualizar su propia cuenta.
	 */
	@PutMapping(path = "actualizar/{id}")
	public ResponseEntity<UserResponseDTO> actualizar(@PathVariable Long id,
			@Valid @RequestBody UpdateUserRequestDTO request, Authentication authentication) {
		User adminActual = (User) authentication.getPrincipal();
		if (adminActual.getId().equals(id)) {
			throw new SelfActionNotAllowedException("Un administrador no puede actualizarse a sí mismo");
		}
		UserResponseDTO userResponse = userService.actualizar(id, request);
		return ResponseEntity.ok(userResponse);
	}

	/**
	 * Cambia la contraseña de un usuario específico por acción de un administrador.
	 *
	 * @param id identificador único del usuario al que se le cambiará la contraseña.
	 * @param request objeto DTO que contiene la nueva contraseña a asignar.
	 * @return {@link ResponseEntity} con estado {@code 200 OK} y los datos del usuario actualizado.
	 */
	@PutMapping(path = "cambiar-password/{id}")
	public ResponseEntity<UserResponseDTO> cambiarPassword(@PathVariable Long id,
			@Valid @RequestBody AdminChangePasswordRequestDTO request) {
		UserResponseDTO userResponse = userService.cambiarPassword(id, request);
		return ResponseEntity.ok(userResponse);
	}

	/**
	 * Elimina un usuario del sistema a partir de su identificador.
	 * <p>
	 * Verifica mediante el contexto de autenticación que el administrador no intente
	 * eliminar su propia cuenta antes de ejecutar la eliminación.
	 * </p>
	 *
	 * @param id identificador único del usuario que se desea eliminar.
	 * @param authentication información de autenticación del administrador que realiza la solicitud.
	 * @return {@link ResponseEntity} con estado {@code 200 OK} y un mensaje de confirmación.
	 * @throws SelfActionNotAllowedException si el administrador intenta eliminarse a sí mismo.
	 */
	@DeleteMapping(path = "eliminar/{id}")
	public ResponseEntity<String> eliminar(@PathVariable Long id, Authentication authentication) {
		User adminActual = (User) authentication.getPrincipal();
		if (adminActual.getId().equals(id)) {
			throw new SelfActionNotAllowedException("Un administrador no puede eliminarse a sí mismo");
		}
		userService.eliminar(id);
		return ResponseEntity.ok("Usuario eliminado");
	}

	/**
	 * Maneja la excepción lanzada cuando un administrador intenta modificarse o eliminarse a sí mismo.
	 *
	 * @param e excepción capturada con el detalle de la acción no permitida.
	 * @return {@link ResponseEntity} con estado {@code 403 Forbidden} y el mensaje de error.
	 */
	@ExceptionHandler(SelfActionNotAllowedException.class)
	public ResponseEntity<String> manejarSelfActionNotAllowedException(SelfActionNotAllowedException e) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(e.getMessage());
	}

	/**
	 * Maneja la excepción lanzada cuando no se encuentra un usuario con el identificador consultado.
	 *
	 * @return {@link ResponseEntity} con estado {@code 404 Not Found} y el mensaje descriptivo.
	 */
	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<String> manejarUserNotFoundException() {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body("El usuario no existe");
	}

	/**
	 * Maneja la excepción lanzada cuando el correo electrónico suministrado ya está registrado.
	 *
	 * @return {@link ResponseEntity} con estado {@code 409 Conflict} y el mensaje descriptivo.
	 */
	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<String> manejarEmailAlreadyExistsException() {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body("El correo ya existe");
	}

	/**
	 * Maneja la excepción lanzada cuando el nombre de usuario suministrado ya está registrado.
	 *
	 * @return {@link ResponseEntity} con estado {@code 409 Conflict} y el mensaje descriptivo.
	 */
	@ExceptionHandler(UsernameAlreadyExistsException.class)
	public ResponseEntity<String> manejarUsernameAlreadyExistException() {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body("El usuario ya existe");
	}

	/**
	 * Maneja la excepción lanzada cuando una contraseña no cumple con las reglas de validación.
	 *
	 * @param e excepción capturada con el motivo de invalidez de la contraseña.
	 * @return {@link ResponseEntity} con estado {@code 400 Bad Request} y el mensaje de error.
	 */
	@ExceptionHandler(InvalidPasswordException.class)
	public ResponseEntity<String> manejarInvalidPasswordException(InvalidPasswordException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(e.getMessage());
	}
}