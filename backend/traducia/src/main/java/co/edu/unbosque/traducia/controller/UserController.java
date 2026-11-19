package co.edu.unbosque.traducia.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import co.edu.unbosque.traducia.dto.AdminCreateUserRequestDTO;
import co.edu.unbosque.traducia.dto.UpdateUserRequestDTO;
import co.edu.unbosque.traducia.dto.UserResponseDTO;
import co.edu.unbosque.traducia.exception.EmailAlreadyExistsException;
import co.edu.unbosque.traducia.exception.InvalidPasswordException;
import co.edu.unbosque.traducia.exception.SelfActionNotAllowedException;
import co.edu.unbosque.traducia.exception.UserNotFoundException;
import co.edu.unbosque.traducia.exception.UsernameAlreadyExistsException;
import co.edu.unbosque.traducia.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
	    name = "Usuarios",
	    description = "Endpoints para la gestión de usuarios por parte de los administradores."
	)
	@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "${front.url}",
			"http://localhost:4200" })
	@RestController
	@RequestMapping("/user")

public class UserController {
	
	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping(path = "listar")
	public ResponseEntity<List<UserResponseDTO>> listar() {
		List<UserResponseDTO> usuarios = userService.listar();
		return ResponseEntity.ok(usuarios);
	}

	@GetMapping(path = "paginado")
	public ResponseEntity<Page<UserResponseDTO>> listarPaginado(Pageable pageable) {
		Page<UserResponseDTO> usuarios = userService.listar(pageable);
		return ResponseEntity.ok(usuarios);
	}

	@GetMapping(path = "{id}")
	public ResponseEntity<UserResponseDTO> obtenerPorId(@PathVariable Long id) {
		UserResponseDTO userResponse = userService.obtenerPorId(id);
		return ResponseEntity.ok(userResponse);
	}

	@PostMapping(path = "crear")
	public ResponseEntity<UserResponseDTO> crear(@RequestBody AdminCreateUserRequestDTO request) {
		UserResponseDTO userResponse = userService.crear(request);
		return new ResponseEntity<UserResponseDTO>(userResponse, HttpStatus.CREATED);
	}

	@PutMapping(path = "actualizar/{id}")
	public ResponseEntity<UserResponseDTO> actualizar(@PathVariable Long id,
			@RequestBody UpdateUserRequestDTO request) {
		UserResponseDTO userResponse = userService.actualizar(id, request);
		return ResponseEntity.ok(userResponse);
	}

	@DeleteMapping(path = "eliminar/{id}")
	public ResponseEntity<String> eliminar(@PathVariable Long id) {
		userService.eliminar(id);
		return ResponseEntity.ok("Usuario eliminado");
	}
	@ExceptionHandler(SelfActionNotAllowedException.class)
	public ResponseEntity<String> manejarSelfActionNotAllowedException(SelfActionNotAllowedException e) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(e.getMessage());
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<String> manejarUserNotFoundException() {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body("El usuario no existe");
	}

	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<String> manejarEmailAlreadyExistsException() {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body("El correo ya existe");
	}

	@ExceptionHandler(UsernameAlreadyExistsException.class)
	public ResponseEntity<String> manejarUsernameAlreadyExistException() {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body("El usuario ya existe");
	}

	@ExceptionHandler(InvalidPasswordException.class)
	public ResponseEntity<String> manejarInvalidPasswordException(InvalidPasswordException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(e.getMessage());
	}
	
}


