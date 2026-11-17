package co.edu.unbosque.traducia.controller;

import org.springframework.security.core.AuthenticationException;
import org.modelmapper.ModelMapper;
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
import co.edu.unbosque.traducia.exception.EmailAlreadyExistsException;
import co.edu.unbosque.traducia.exception.InvalidPasswordException;
import co.edu.unbosque.traducia.exception.UsernameAlreadyExistsException;
import co.edu.unbosque.traducia.security.JwtUtil;
import co.edu.unbosque.traducia.service.UserService;
import co.edu.unbosque.traducia.dto.LoginRequestDTO;
import co.edu.unbosque.traducia.dto.LoginResponseDTO;
import co.edu.unbosque.traducia.dto.RegisterRequestDTO;
import co.edu.unbosque.traducia.dto.UserResponseDTO;

@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "https://sebastianmalagon.netlify.app",
		"http://localhost:4200" })
@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;

	private final JwtUtil jwtUtil;

	private final UserService userService;
	
	private final ModelMapper modelMapper;

	public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
			UserService userService, ModelMapper modelMapper) {
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
		this.userService = userService;
		this.modelMapper = modelMapper;
	}

	@PostMapping(path = "login")
	public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getNombre(), loginRequest.getPassword()));

		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String jwt = jwtUtil.generateToken(userDetails);

		UserResponseDTO userResponse = null;
		if (userDetails instanceof User) {
			User user = (User) userDetails;
			userResponse = modelMapper.map(user, UserResponseDTO.class);
		}

		return new ResponseEntity<LoginResponseDTO>(new LoginResponseDTO(userResponse, jwt), HttpStatus.CREATED);
	}
	
	@PostMapping(path = "register")
	public ResponseEntity<UserResponseDTO> register(@RequestBody RegisterRequestDTO registerRequest) {
		UserResponseDTO userResponse = userService.crear(registerRequest);
		return ResponseEntity.ok(userResponse);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<String> manejarAuthenticationException() {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body("Contraseña inválido");
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
