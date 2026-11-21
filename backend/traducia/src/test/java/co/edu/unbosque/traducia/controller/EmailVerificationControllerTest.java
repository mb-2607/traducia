package co.edu.unbosque.traducia.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import co.edu.unbosque.traducia.dto.ResendVerificationRequestDTO;
import co.edu.unbosque.traducia.exception.GlobalExceptionHandler;
import co.edu.unbosque.traducia.exception.InvalidTokenException;
import co.edu.unbosque.traducia.exception.UserNotFoundException;
import co.edu.unbosque.traducia.service.EmailVerificationService;


/**
 * Pruebas unitarias para {@link EmailVerificationController} utilizando JUnit 5,
 * Mockito, MockMvc, Hamcrest y AssertJ.
 *
 * <p>Valida los endpoints de verificación y reenvío de correo electrónico, así como
 * las respuestas generadas por los manejadores de excepciones locales y globales.</p>
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class EmailVerificationControllerTest {

	private MockMvc mockMvc;

	@Mock
	private EmailVerificationService emailVerificationService;

	@InjectMocks
	private EmailVerificationController emailVerificationController;

	/**
	 * Inicializa {@link MockMvc} en modo aislado con el controlador bajo prueba y
	 * el {@link GlobalExceptionHandler}.
	 */
	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(emailVerificationController)
				.setControllerAdvice(new GlobalExceptionHandler())
				.addPlaceholderValue("front.url", "http://localhost:4200")
				.build();
	}

	/**
	 * Verifica que {@code GET /auth/verify-email} retorne HTTP 200 cuando el token
	 * es válido.
	 *
	 * @throws Exception si ocurre un error al ejecutar la petición simulada.
	 */
	@Test
	@DisplayName("GET /auth/verify-email debe retornar 200 cuando el token es válido")
	void verifyEmailDebeRetornarOkCuandoTokenEsValido() throws Exception {
		String token = "token-valido-123";
		doNothing().when(emailVerificationService).verificarCorreo(token);

		MvcResult resultado = mockMvc.perform(get("/auth/verify-email")
				.param("token", token))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Correo verificado correctamente")))
				.andReturn();

		verify(emailVerificationService).verificarCorreo(token);
		assertThat(resultado.getResponse().getContentAsString())
				.isEqualTo("Correo verificado correctamente. Ya puedes iniciar sesión");
	}

	/**
	 * Verifica que {@code GET /auth/verify-email} retorne HTTP 400 cuando el
	 * servicio lanza {@link InvalidTokenException}.
	 *
	 * @throws Exception si ocurre un error al ejecutar la petición simulada.
	 */
	@Test
	@DisplayName("GET /auth/verify-email debe retornar 400 cuando el token es inválido")
	void verifyEmailDebeRetornarBadRequestCuandoTokenEsInvalido() throws Exception {
		String token = "token-expirado";
		doThrow(new InvalidTokenException("Token inválido o expirado"))
				.when(emailVerificationService).verificarCorreo(token);

		mockMvc.perform(get("/auth/verify-email")
				.param("token", token))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(is("Token inválido o expirado")));

		verify(emailVerificationService).verificarCorreo(token);
	}

	/**
	 * Verifica que {@code GET /auth/verify-email} retorne HTTP 409 cuando la cuenta
	 * ya fue verificada previamente ({@link IllegalStateException}).
	 *
	 * @throws Exception si ocurre un error al ejecutar la petición simulada.
	 */
	@Test
	@DisplayName("GET /auth/verify-email debe retornar 409 cuando la cuenta ya está verificada")
	void verifyEmailDebeRetornarConflictCuandoCuentaYaEstaActiva() throws Exception {
		String token = "token-usado";
		doThrow(new IllegalStateException("La cuenta ya fue verificada"))
				.when(emailVerificationService).verificarCorreo(token);

		mockMvc.perform(get("/auth/verify-email")
				.param("token", token))
				.andExpect(status().isConflict())
				.andExpect(content().string(is("La cuenta ya fue verificada")));
	}

	/**
	 * Verifica que {@code POST /auth/resend-verification} retorne HTTP 200 cuando
	 * el correo es válido y el usuario existe.
	 *
	 * @throws Exception si ocurre un error al ejecutar la petición simulada.
	 */
	@Test
	@DisplayName("POST /auth/resend-verification debe retornar 200 con correo válido")
	void resendVerificationDebeRetornarOkCuandoCorreoEsValido() throws Exception {
		String body = """
				{
				  "correo": "usuario@ejemplo.com"
				}
				""";
		doNothing().when(emailVerificationService).reenviarVerificacion(any(ResendVerificationRequestDTO.class));

		MvcResult resultado = mockMvc.perform(post("/auth/resend-verification")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("nuevo enlace de verificación")))
				.andReturn();

		verify(emailVerificationService).reenviarVerificacion(any(ResendVerificationRequestDTO.class));
		assertThat(resultado.getResponse().getContentAsString())
				.isEqualTo("Se ha enviado un nuevo enlace de verificación a tu correo electrónico");
	}

	/**
	 * Verifica que {@code POST /auth/resend-verification} retorne HTTP 404 cuando
	 * no existe un usuario asociado al correo indicado.
	 *
	 * @throws Exception si ocurre un error al ejecutar la petición simulada.
	 */
	@Test
	@DisplayName("POST /auth/resend-verification debe retornar 404 cuando el usuario no existe")
	void resendVerificationDebeRetornarNotFoundCuandoUsuarioNoExiste() throws Exception {
		String body = """
				{
				  "correo": "noexiste@ejemplo.com"
				}
				""";
		doThrow(UserNotFoundException.class)
				.when(emailVerificationService).reenviarVerificacion(any(ResendVerificationRequestDTO.class));

		mockMvc.perform(post("/auth/resend-verification")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isNotFound())
				.andExpect(content().string(is("No existe un usuario registrado con ese correo")));
	}

	/**
	 * Verifica que {@code POST /auth/resend-verification} retorne HTTP 400 mediante
	 * {@link GlobalExceptionHandler} cuando el cuerpo incumple {@code @Valid}.
	 *
	 * @throws Exception si ocurre un error al ejecutar la petición simulada.
	 */
	@Test
	@DisplayName("POST /auth/resend-verification debe retornar 400 cuando falla @Valid")
	void resendVerificationDebeRetornarBadRequestCuandoDtoEsInvalido() throws Exception {
		mockMvc.perform(post("/auth/resend-verification")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.error", is("Error de validación")))
				.andExpect(jsonPath("$.errores", notNullValue()));

		verify(emailVerificationService, never()).reenviarVerificacion(any(ResendVerificationRequestDTO.class));
	}
}
