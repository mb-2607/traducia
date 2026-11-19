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

@Tag(
	    name = "Planes",
	    description = "Endpoints para que el usuario autenticado gestione su propio tipo de plan."
	)
	@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "${front.url}",
			"http://localhost:4200" })
	@RestController
	@RequestMapping("/plan")
public class PlanController {
	private final UserService userService;

	public PlanController(UserService userService) {
		this.userService = userService;
	}

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

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<String> manejarUserNotFoundException() {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body("El usuario no existe");
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<String> manejarIllegalArgumentException(IllegalArgumentException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(e.getMessage());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<String> manejarHttpMessageNotReadableException() {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body("Tipo de plan inválido");
	}
}

