package co.edu.unbosque.traducia.controller;

import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import co.edu.unbosque.traducia.entity.User;
import co.edu.unbosque.traducia.security.JwtUtil;
import co.edu.unbosque.traducia.service.UserService;
import co.edu.unbosque.traducia.dto.LoginRequestDTO;
import co.edu.unbosque.traducia.dto.LoginResponseDTO;
import co.edu.unbosque.traducia.dto.UserResponseDTO;

@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "https://sebastianmalagon.netlify.app",
		"http://localhost:4200" })
@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;

	private final JwtUtil jwtUtil;

	private final UserService userService;

	public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService) {
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
		this.userService = userService;
	}

	@PostMapping(path = "login")
	public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getCorreo(), loginRequest.getPassword()));

		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String jwt = jwtUtil.generateToken(userDetails);

		UserResponseDTO userResponse = null;
		if (userDetails instanceof User) {
			User user = (User) userDetails;
			userResponse = new UserResponseDTO();
			userResponse.setNombre(user.getUsername());
			userResponse.setCorreo(user.getEmail());
			userResponse.setRol(user.getRole().name());
			userResponse.setPlanId(user.getPlanType().name());
		}

		return ResponseEntity.ok(new LoginResponseDTO(userResponse, jwt));
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<String> manejarAuthenticationException() {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body("Correo o contraseña inválidos o usuario no encontrado");
	}

}
