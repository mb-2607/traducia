package co.edu.unbosque.traducia.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import co.edu.unbosque.traducia.dto.SolicitudTraduccionResponseDTO;
import co.edu.unbosque.traducia.dto.TraduccionRequestDTO;
import co.edu.unbosque.traducia.exception.GlobalExceptionHandler;
import co.edu.unbosque.traducia.exception.PlanCharacterLimitExceededException;
import co.edu.unbosque.traducia.service.TraduccionService;

/**
 * Pruebas unitarias para {@link TraduccionController} utilizando JUnit 5,
 * Mockito, MockMvc, Hamcrest y AssertJ.
 *
 * <p>Valida el endpoint {@code POST /traductor/traducir}, la extracción del
 * usuario autenticado, el manejo de errores de validación mediante
 * {@link GlobalExceptionHandler} y la captura de
 * {@link PlanCharacterLimitExceededException}.</p>
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class TraduccionControllerTest {

	@Mock
	private TraduccionService traduccionServ;

	@InjectMocks
	private TraduccionController traduccionController;

	private MockMvc mockMvc;

	/**
	 * Configura {@link MockMvc} de forma aislada con el controlador, el manejador
	 * global {@link GlobalExceptionHandler} y la propiedad {@code front.url}.
	 */
	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(traduccionController)
				.setControllerAdvice(new GlobalExceptionHandler())
				.addPlaceholderValue("front.url", "http://localhost:4200")
				.build();
	}

	/**
	 * Verifica que {@code POST /traductor/traducir} retorne HTTP 201 cuando la
	 * solicitud y el usuario autenticado son válidos.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("POST /traductor/traducir debe retornar 201 cuando la solicitud es válida")
	void traducirDebeRetornarCreatedCuandoSolicitudEsValida() throws Exception {
		Authentication authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn("dsantiago");

		SolicitudTraduccionResponseDTO responseDTO = new SolicitudTraduccionResponseDTO();
		when(traduccionServ.traducir(any(TraduccionRequestDTO.class), eq("dsantiago")))
				.thenReturn(responseDTO);

		String body = """
				{
				  "origen": "es",
				  "destino": "en",
				  "texto": "Hola mundo"
				}
				""";

		MvcResult resultado = mockMvc.perform(post("/traductor/traducir")
				.principal(authentication)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isCreated())
				.andReturn();

		assertThat(resultado.getResponse().getStatus()).isEqualTo(201);
		verify(traduccionServ).traducir(any(TraduccionRequestDTO.class), eq("dsantiago"));
	}

	/**
	 * Verifica que {@code POST /traductor/traducir} retorne HTTP 400 mediante
	 * {@link GlobalExceptionHandler} cuando el cuerpo JSON está vacío o no cumple
	 * las reglas {@code @Valid}.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("POST /traductor/traducir debe retornar 400 cuando falla la validación")
	void traducirDebeRetornarBadRequestCuandoFallaValidacion() throws Exception {
		Authentication authentication = mock(Authentication.class);

		String bodyInvalido = "{}";

		mockMvc.perform(post("/traductor/traducir")
				.principal(authentication)
				.contentType(MediaType.APPLICATION_JSON)
				.content(bodyInvalido))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.error", is("Error de validación")));

		verifyNoInteractions(traduccionServ);
	}

	/**
	 * Verifica que {@code POST /traductor/traducir} retorne HTTP 400 con el
	 * mensaje correspondiente cuando se excede el límite de caracteres del plan
	 * ({@link PlanCharacterLimitExceededException}).
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("POST /traductor/traducir debe retornar 400 cuando se excede el límite del plan")
	void traducirDebeRetornarBadRequestCuandoExcedeLimiteDelPlan() throws Exception {
		Authentication authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn("dsantiago");

		when(traduccionServ.traducir(any(TraduccionRequestDTO.class), eq("dsantiago")))
				.thenThrow(new PlanCharacterLimitExceededException("Límite superado"));

		String body = """
				{
				  "origen": "es",
				  "destino": "en",
				  "texto": "Texto muy largo para el plan"
				}
				""";

		MvcResult resultado = mockMvc.perform(post("/traductor/traducir")
				.principal(authentication)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isBadRequest())
				.andReturn();

		assertThat(resultado.getResponse().getContentAsString(StandardCharsets.UTF_8))
				.isEqualTo("Cantidad de caracteres excedidad frente al plan actual");
		verify(traduccionServ).traducir(any(TraduccionRequestDTO.class), eq("dsantiago"));
	}
}