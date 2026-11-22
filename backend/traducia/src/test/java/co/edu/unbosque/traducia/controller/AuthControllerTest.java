package co.edu.unbosque.traducia.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import co.edu.unbosque.traducia.dto.RegisterRequestDTO;
import co.edu.unbosque.traducia.dto.UserResponseDTO;
import co.edu.unbosque.traducia.entity.User;
import co.edu.unbosque.traducia.exception.EmailAlreadyExistsException;
import co.edu.unbosque.traducia.exception.GlobalExceptionHandler;
import co.edu.unbosque.traducia.exception.InvalidPasswordException;
import co.edu.unbosque.traducia.exception.UsernameAlreadyExistsException;
import co.edu.unbosque.traducia.security.JwtUtil;
import co.edu.unbosque.traducia.service.UserService;

/**
 * Pruebas unitarias para {@link AuthController} utilizando JUnit 5, Mockito,
 * MockMvc, Hamcrest y AssertJ.
 *
 * <p>Valida los flujos de inicio de sesión ({@code POST /auth/login}) y
 * registro ({@code POST /auth/register}), las validaciones {@code @Valid}
 * gestionadas por {@link GlobalExceptionHandler} y las respuestas de error
 * de autenticación y registro.</p>
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private UserService userService;

	@Mock
	private ModelMapper modelMapper;

	@InjectMocks
	private AuthController authController;

	private MockMvc mockMvc;

	/**
	 * Configura {@link MockMvc} de forma aislada con el controlador, el manejador
	 * global {@link GlobalExceptionHandler} y la propiedad {@code front.url}.
	 */
	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(authController)
				.setControllerAdvice(new GlobalExceptionHandler())
				.addPlaceholderValue("front.url", "http://localhost:4200")
				.build();
	}

	/**
	 * Verifica que {@code POST /auth/login} retorne HTTP 200 y el token JWT
	 * cuando las credenciales del usuario son válidas.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("POST /auth/login debe retornar 200 y token JWT con credenciales válidas")
	void loginDebeRetornarOkConCredencialesValidas() throws Exception {
		User usuario = mock(User.class);
		Authentication authentication = mock(Authentication.class);
		when(authentication.getPrincipal()).thenReturn(usuario);

		UserResponseDTO userResponseDTO = new UserResponseDTO();

		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(authentication);
		when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwt-token-prueba");
		when(modelMapper.map(usuario, UserResponseDTO.class)).thenReturn(userResponseDTO);

		String body = """
				{
				  "nombre": "dsantiago",
				  "password": "Password123!"
				}
				""";

		MvcResult resultado = mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isOk())
				.andReturn();

		assertThat(resultado.getResponse().getContentAsString(StandardCharsets.UTF_8))
				.contains("jwt-token-prueba");
		verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
		verify(jwtUtil).generateToken(usuario);
		verify(modelMapper).map(usuario, UserResponseDTO.class);
	}

	/**
	 * Verifica que {@code POST /auth/login} retorne HTTP 400 mediante
	 * {@link GlobalExceptionHandler} cuando faltan campos obligatorios.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("POST /auth/login debe retornar 400 cuando faltan campos obligatorios")
	void loginDebeRetornarBadRequestCuandoFaltanCampos() throws Exception {
		String bodyInvalido = """
				{
				  "nombre": "",
				  "password": ""
				}
				""";

		mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(bodyInvalido))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.error", is("Error de validación")));

		verifyNoInteractions(authenticationManager, jwtUtil, userService);
	}

	/**
	 * Verifica que {@code POST /auth/login} retorne HTTP 401 cuando las
	 * credenciales son inválidas ({@link AuthenticationException}).
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("POST /auth/login debe retornar 401 cuando las credenciales son inválidas")
	void loginDebeRetornarUnauthorizedCuandoCredencialesSonInvalidas() throws Exception {
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new BadCredentialsException("Credenciales inválidas"));

		String body = """
				{
				  "nombre": "dsantiago",
				  "password": "ClaveIncorrecta123!"
				}
				""";

		mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isUnauthorized())
				.andExpect(content().string(is("Contraseña inválido")));
	}

	/**
	 * Verifica que {@code POST /auth/login} retorne HTTP 403 cuando la cuenta aún
	 * no ha verificado su correo electrónico ({@link DisabledException}).
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("POST /auth/login debe retornar 403 cuando la cuenta no ha verificado correo")
	void loginDebeRetornarForbiddenCuandoCuentaEstaDeshabilitada() throws Exception {
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new DisabledException("Cuenta deshabilitada"));

		String body = """
				{
				  "nombre": "dsantiago",
				  "password": "Password123!"
				}
				""";

		mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isForbidden())
				.andExpect(content().string(is("Debes verificar tu correo electrónico antes de iniciar sesión")));
	}

	/**
	 * Verifica que {@code POST /auth/register} retorne HTTP 201 cuando los datos
	 * de registro son válidos.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("POST /auth/register debe retornar 201 cuando el registro es válido")
	void registerDebeRetornarCreatedCuandoDatosSonValidos() throws Exception {
		UserResponseDTO responseDTO = new UserResponseDTO();
		when(userService.crear(any(RegisterRequestDTO.class))).thenReturn(responseDTO);

		String body = """
				{
				  "nombre": "dsantiago",
				  "correo": "dsantiago@ejemplo.com",
				  "password": "Password123!"
				}
				""";

		MvcResult resultado = mockMvc.perform(post("/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isCreated())
				.andReturn();

		assertThat(resultado.getResponse().getStatus()).isEqualTo(201);
		verify(userService).crear(any(RegisterRequestDTO.class));
	}

	/**
	 * Verifica que {@code POST /auth/register} retorne HTTP 400 mediante
	 * {@link GlobalExceptionHandler} cuando el correo o la contraseña no cumplen
	 * las reglas de validación.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("POST /auth/register debe retornar 400 cuando falla la validación del DTO")
	void registerDebeRetornarBadRequestCuandoFallaValidacion() throws Exception {
		String bodyInvalido = """
				{
				  "nombre": "dsantiago",
				  "correo": "correo-malo",
				  "password": "123"
				}
				""";

		mockMvc.perform(post("/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(bodyInvalido))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.error", is("Error de validación")));

		verifyNoInteractions(userService);
	}

	/**
	 * Verifica que {@code POST /auth/register} retorne HTTP 409 cuando el correo
	 * ya está registrado ({@link EmailAlreadyExistsException}).
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("POST /auth/register debe retornar 409 cuando el correo ya existe")
	void registerDebeRetornarConflictCuandoCorreoYaExiste() throws Exception {
		when(userService.crear(any(RegisterRequestDTO.class)))
				.thenThrow(new EmailAlreadyExistsException("Correo duplicado"));

		String body = """
				{
				  "nombre": "dsantiago",
				  "correo": "dsantiago@ejemplo.com",
				  "password": "Password123!"
				}
				""";

		mockMvc.perform(post("/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isConflict())
				.andExpect(content().string(is("El correo ya existe")));
	}

	/**
	 * Verifica que {@code POST /auth/register} retorne HTTP 409 cuando el nombre
	 * de usuario ya existe ({@link UsernameAlreadyExistsException}).
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("POST /auth/register debe retornar 409 cuando el usuario ya existe")
	void registerDebeRetornarConflictCuandoUsuarioYaExiste() throws Exception {
		when(userService.crear(any(RegisterRequestDTO.class)))
				.thenThrow(new UsernameAlreadyExistsException("Usuario duplicado"));

		String body = """
				{
				  "nombre": "dsantiago",
				  "correo": "dsantiago@ejemplo.com",
				  "password": "Password123!"
				}
				""";

		mockMvc.perform(post("/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isConflict())
				.andExpect(content().string(is("El usuario ya existe")));
	}

	/**
	 * Verifica que {@code POST /auth/register} retorne HTTP 400 cuando el
	 * servicio lanza {@link InvalidPasswordException}.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("POST /auth/register debe retornar 400 cuando la contraseña no es válida")
	void registerDebeRetornarBadRequestCuandoPasswordEsInvalida() throws Exception {
		when(userService.crear(any(RegisterRequestDTO.class)))
				.thenThrow(new InvalidPasswordException("Contraseña insegura"));

		String body = """
				{
				  "nombre": "dsantiago",
				  "correo": "dsantiago@ejemplo.com",
				  "password": "Password123!"
				}
				""";

		mockMvc.perform(post("/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(is("Contraseña insegura")));
	}
}
