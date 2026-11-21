package co.edu.unbosque.traducia.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import co.edu.unbosque.traducia.dto.ForgotPasswordRequestDTO;
import co.edu.unbosque.traducia.dto.ResetPasswordRequestDTO;
import co.edu.unbosque.traducia.exception.GlobalExceptionHandler;
import co.edu.unbosque.traducia.exception.InvalidPasswordException;
import co.edu.unbosque.traducia.exception.InvalidTokenException;
import co.edu.unbosque.traducia.exception.UserNotFoundException;
import co.edu.unbosque.traducia.service.PasswordResetService;

/**
 * Pruebas unitarias para {@link PasswordResetController} utilizando JUnit 5,
 * Mockito, MockMvc, Hamcrest y AssertJ.
 *
 * <p>Valida los endpoints de solicitud y restablecimiento de contraseña, las
 * validaciones {@code @Valid} manejadas por {@link GlobalExceptionHandler} y
 * los manejadores de excepciones locales del controlador.</p>
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class PasswordResetControllerTest {

	@Mock
	private PasswordResetService passwordResetService;

	@InjectMocks
	private PasswordResetController passwordResetController;

	private MockMvc mockMvc;

	/**
	 * Configura {@link MockMvc} de forma aislada con el controlador, el manejador
	 * global {@link GlobalExceptionHandler} y la propiedad {@code front.url}.
	 */
	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(passwordResetController)
				.setControllerAdvice(new GlobalExceptionHandler())
				.addPlaceholderValue("front.url", "http://localhost:4200")
				.build();
	}

	/**
	 * Verifica que {@code POST /auth/forgot-password} retorne HTTP 200 cuando el
	 * correo es válido.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("POST /auth/forgot-password debe retornar 200 con correo válido")
	void forgotPasswordDebeRetornarOkConCorreoValido() throws Exception {
		doNothing().when(passwordResetService).solicitarRecuperacion(any(ForgotPasswordRequestDTO.class));

		String body = """
				{
				  "correo": "usuario@ejemplo.com"
				}
				""";

		MvcResult resultado = mockMvc.perform(post("/auth/forgot-password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isOk())
				.andReturn();

		assertThat(resultado.getResponse().getContentAsString(StandardCharsets.UTF_8))
				.isEqualTo("Se ha enviado un enlace de recuperación a tu correo electrónico");
		verify(passwordResetService).solicitarRecuperacion(any(ForgotPasswordRequestDTO.class));
	}

	/**
	 * Verifica que {@code POST /auth/forgot-password} retorne HTTP 400 mediante
	 * {@link GlobalExceptionHandler} cuando falla la validación {@code @Valid}.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("POST /auth/forgot-password debe retornar 400 cuando el correo es inválido")
	void forgotPasswordDebeRetornarBadRequestCuandoFallaValidacion() throws Exception {
		String bodyInvalido = """
				{
				  "correo": "correo-invalido"
				}
				""";

		mockMvc.perform(post("/auth/forgot-password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(bodyInvalido))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.error", is("Error de validación")));

		verifyNoInteractions(passwordResetService);
	}

	/**
	 * Verifica que {@code POST /auth/forgot-password} retorne HTTP 404 cuando el
	 * correo no pertenece a ningún usuario registrado.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("POST /auth/forgot-password debe retornar 404 cuando el usuario no existe")
	void forgotPasswordDebeRetornarNotFoundCuandoUsuarioNoExiste() throws Exception {
		doThrow(new UserNotFoundException())
				.when(passwordResetService).solicitarRecuperacion(any(ForgotPasswordRequestDTO.class));

		String body = """
				{
				  "correo": "noexiste@ejemplo.com"
				}
				""";

		mockMvc.perform(post("/auth/forgot-password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isNotFound())
				.andExpect(content().string(is("No existe un usuario registrado con ese correo")));
	}

	/**
	 * Verifica que {@code POST /auth/forgot-password} retorne HTTP 403 cuando la
	 * cuenta se encuentra bloqueada o deshabilitada.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("POST /auth/forgot-password debe retornar 403 cuando la cuenta está bloqueada")
	void forgotPasswordDebeRetornarForbiddenCuandoCuentaEstaBloqueada() throws Exception {
		doThrow(new IllegalStateException("La cuenta está bloqueada"))
				.when(passwordResetService).solicitarRecuperacion(any(ForgotPasswordRequestDTO.class));

		String body = """
				{
				  "correo": "bloqueado@ejemplo.com"
				}
				""";

		mockMvc.perform(post("/auth/forgot-password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isForbidden())
				.andExpect(content().string(is("La cuenta está bloqueada")));
	}

	/**
	 * Verifica que {@code POST /auth/reset-password} retorne HTTP 200 cuando el
	 * token y la nueva contraseña son válidos.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("POST /auth/reset-password debe retornar 200 cuando los datos son válidos")
	void resetPasswordDebeRetornarOkCuandoDatosSonValidos() throws Exception {
		doNothing().when(passwordResetService).restablecerPassword(any(ResetPasswordRequestDTO.class));

		String body = """
				{
				  "token": "token-valido-123",
				  "nuevaPassword": "PasswordSegura123!"
				}
				""";

		MvcResult resultado = mockMvc.perform(post("/auth/reset-password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isOk())
				.andReturn();

		assertThat(resultado.getResponse().getContentAsString(StandardCharsets.UTF_8))
				.isEqualTo("Contraseña restablecida correctamente");
		verify(passwordResetService).restablecerPassword(any(ResetPasswordRequestDTO.class));
	}

	/**
	 * Verifica que {@code POST /auth/reset-password} retorne HTTP 400 cuando el
	 * token es inválido o expiró ({@link InvalidTokenException}).
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("POST /auth/reset-password debe retornar 400 cuando el token es inválido")
	void resetPasswordDebeRetornarBadRequestCuandoTokenEsInvalido() throws Exception {
		doThrow(new InvalidTokenException("Token inválido o expirado"))
				.when(passwordResetService).restablecerPassword(any(ResetPasswordRequestDTO.class));

		String body = """
				{
				  "token": "token-invalido",
				  "nuevaPassword": "PasswordSegura123!"
				}
				""";

		mockMvc.perform(post("/auth/reset-password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(is("Token inválido o expirado")));
	}

	/**
	 * Verifica que {@code POST /auth/reset-password} retorne HTTP 400 cuando la
	 * nueva contraseña incumple las reglas ({@link InvalidPasswordException}).
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("POST /auth/reset-password debe retornar 400 cuando la contraseña no es válida")
	void resetPasswordDebeRetornarBadRequestCuandoPasswordEsInvalida() throws Exception {
		doThrow(new InvalidPasswordException("La contraseña no cumple los requisitos mínimos"))
				.when(passwordResetService).restablecerPassword(any(ResetPasswordRequestDTO.class));

		String body = """
				{
				  "token": "token-valido-123",
				  "nuevaPassword": "PasswordSegura123!"
				}
				""";

		mockMvc.perform(post("/auth/reset-password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(is("La contraseña no cumple los requisitos mínimos")));
	}
}