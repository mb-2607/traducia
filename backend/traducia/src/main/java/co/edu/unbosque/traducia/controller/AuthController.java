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
import io.swagger.v3.oas.annotations.tags.Tag;
import co.edu.unbosque.traducia.dto.LoginRequestDTO;
import co.edu.unbosque.traducia.dto.LoginResponseDTO;
import co.edu.unbosque.traducia.dto.RegisterRequestDTO;
import co.edu.unbosque.traducia.dto.UserResponseDTO;

/**
 * Controlador REST encargado de gestionar los procesos de autenticación
 * y registro de usuarios dentro de la aplicación.
 * <p>
 * Proporciona servicios para iniciar sesión, registrar nuevas cuentas
 * y gestionar las excepciones relacionadas con estos procesos.
 * Utiliza Spring Security para validar las credenciales de los usuarios
 * y JWT para gestionar la autenticación mediante tokens.
 * </p>
 *
 * @author
 * @version 1.0
 * @see UserService
 * @see JwtUtil
 * @see AuthenticationManager
 */
@Tag(
    name = "Autenticación",
    description = "Endpoints para el registro de usuarios, inicio de sesión y gestión de errores de autenticación."
)
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "${front.url}",
		"http://localhost:4200" })
@RestController
@RequestMapping("/auth")
public class AuthController {
	
	/**
	 * Administrador encargado de autenticar las credenciales de los usuarios
	 * mediante Spring Security.
	 */
	private final AuthenticationManager authenticationManager;
	
	/**
	 * Utilidad encargada de generar y gestionar los tokens JWT
	 * utilizados para la autenticación de los usuarios.
	 */
	private final JwtUtil jwtUtil;
	
	/**
	 * Servicio encargado de gestionar las operaciones relacionadas
	 * con los usuarios, como el registro de nuevas cuentas.
	 */
	private final UserService userService;
	
	/**
	 * Componente encargado de convertir objetos entre diferentes tipos,
	 * principalmente entre entidades y DTO.
	 */
	private final ModelMapper modelMapper;
	
	/**
	 * Construye el controlador de autenticación e inicializa sus dependencias.
	 *
	 * @param authenticationManager administrador encargado de autenticar las credenciales de los usuarios.
	 * @param jwtUtil utilidad encargada de generar los tokens JWT.
	 * @param userService servicio encargado de gestionar las operaciones de los usuarios.
	 * @param modelMapper componente encargado de convertir entidades y DTO.
	 */
	public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
			UserService userService, ModelMapper modelMapper) {
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
		this.userService = userService;
		this.modelMapper = modelMapper;
	}
	
	/**
	 * Autentica a un usuario mediante sus credenciales y genera un token JWT
	 * para permitir el acceso a los recursos protegidos de la aplicación.
	 * <p>
	 * Si las credenciales son válidas, obtiene los detalles del usuario,
	 * genera el token de autenticación y convierte la entidad a un DTO
	 * para construir la respuesta.
	 * </p>
	 *
	 * @param loginRequest objeto que contiene el nombre de usuario y la contraseña.
	 * @return respuesta HTTP con los datos del usuario y el token JWT generado.
	 * @throws AuthenticationException si las credenciales proporcionadas no son válidas.
	 */
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

		return ResponseEntity.ok(new LoginResponseDTO(userResponse, jwt));
	}
	
	/**
	 * Registra un nuevo usuario en la aplicación.
	 * <p>
	 * Delega la creación de la cuenta al servicio de usuarios y devuelve
	 * la información del usuario registrado.
	 * </p>
	 *
	 * @param registerRequest objeto que contiene los datos necesarios para registrar al usuario.
	 * @return respuesta HTTP con los datos del usuario registrado y el estado CREATED (201).
	 * @throws EmailAlreadyExistsException si el correo electrónico ya se encuentra registrado.
	 * @throws UsernameAlreadyExistsException si el nombre de usuario ya se encuentra registrado.
	 * @throws InvalidPasswordException si la contraseña no cumple con los requisitos establecidos.
	 */
	@PostMapping(path = "register")
	public ResponseEntity<UserResponseDTO> register(@RequestBody RegisterRequestDTO registerRequest) {
		UserResponseDTO userResponse = userService.crear(registerRequest);
		return new ResponseEntity<UserResponseDTO>(userResponse, HttpStatus.CREATED);
	}
	
	/**
	 * Gestiona las excepciones producidas durante el proceso de autenticación.
	 * <p>
	 * Devuelve una respuesta HTTP con estado UNAUTHORIZED (401) cuando
	 * las credenciales proporcionadas no son válidas.
	 * </p>
	 *
	 * @return respuesta HTTP con un mensaje indicando el fallo de autenticación.
	 */
	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<String> manejarAuthenticationException() {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body("Contraseña inválido");
	}
	
	/**
	 * Gestiona las excepciones generadas cuando se intenta registrar
	 * un usuario con un correo electrónico que ya está registrado.
	 *
	 * @return respuesta HTTP con estado CONFLICT (409) y un mensaje indicando
	 *         que el correo electrónico ya existe.
	 */
	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<String> manejarEmailAlreadyExistsException() {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body("El correo ya existe");
	}
	
	/**
	 * Gestiona las excepciones generadas cuando se intenta registrar
	 * un usuario con un nombre de usuario que ya está registrado.
	 *
	 * @return respuesta HTTP con estado CONFLICT (409) y un mensaje indicando
	 *         que el nombre de usuario ya existe.
	 */
	@ExceptionHandler(UsernameAlreadyExistsException.class)
	public ResponseEntity<String> manejarUsernameAlreadyExistException() {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body("El usuario ya existe");
	}
	
	/**
	 * Gestiona las excepciones producidas cuando una contraseña no cumple
	 * con los requisitos establecidos durante el registro de un usuario.
	 * <p>
	 * Devuelve el mensaje de la excepción para informar al cliente
	 * sobre el motivo del error.
	 * </p>
	 *
	 * @param e excepción que contiene el mensaje del error de validación.
	 * @return respuesta HTTP con estado BAD_REQUEST (400) y el mensaje de la excepción.
	 */
	@ExceptionHandler(InvalidPasswordException.class)
	public ResponseEntity<String> manejarInvalidPasswordException(InvalidPasswordException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(e.getMessage());
	}

}
