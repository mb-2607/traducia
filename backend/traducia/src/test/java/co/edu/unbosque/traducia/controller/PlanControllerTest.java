package co.edu.unbosque.traducia.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import co.edu.unbosque.traducia.dto.UserResponseDTO;
import co.edu.unbosque.traducia.entity.PlanType;
import co.edu.unbosque.traducia.entity.User;
import co.edu.unbosque.traducia.exception.GlobalExceptionHandler;
import co.edu.unbosque.traducia.exception.UserNotFoundException;
import co.edu.unbosque.traducia.service.UserService;

/**
 * Pruebas unitarias para {@link PlanController} utilizando JUnit 5, Mockito,
 * MockMvc, Hamcrest y AssertJ.
 *
 * <p>Valida la consulta del catálogo de planes, la consulta del plan actual y
 * la actualización del plan del usuario autenticado ({@code FREE},
 * {@code ORIA} e {@code INFINITIA}), el manejo de validaciones {@code @Valid}
 * mediante {@link GlobalExceptionHandler} y los manejadores de excepciones
 * locales del controlador.</p>
 *
 * @version 1.1
 */
@ExtendWith(MockitoExtension.class)
class PlanControllerTest {

	@Mock
	private UserService userService;

	@InjectMocks
	private PlanController planController;

	private MockMvc mockMvc;

	/**
	 * Configura {@link MockMvc} de forma aislada con el controlador, el manejador
	 * global {@link GlobalExceptionHandler} y la propiedad {@code front.url}.
	 */
	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(planController)
				.setControllerAdvice(new GlobalExceptionHandler())
				.addPlaceholderValue("front.url", "http://localhost:4200")
				.build();
	}

	/**
	 * Crea un objeto {@link Authentication} simulado cuyo principal es un
	 * {@link User} con el identificador indicado.
	 *
	 * @param userId identificador del usuario autenticado.
	 * @return instancia simulada de {@link Authentication}.
	 */
	private Authentication crearAutenticacion(Long userId) {
		User usuario = mock(User.class);
		when(usuario.getId()).thenReturn(userId);

		Authentication authentication = mock(Authentication.class);
		when(authentication.getPrincipal()).thenReturn(usuario);
		return authentication;
	}

	/**
	 * Crea un objeto {@link Authentication} simulado con el nombre de usuario
	 * indicado.
	 *
	 * @param nombreUsuario nombre del usuario autenticado.
	 * @return instancia simulada de {@link Authentication}.
	 */
	private Authentication crearAutenticacionPorNombre(String nombreUsuario) {
		Authentication authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn(nombreUsuario);
		return authentication;
	}

	/**
	 * Verifica que {@code GET /plan} retorne HTTP 200 junto con todos los planes
	 * definidos en {@link PlanType} y sus propiedades.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("GET /plan debe retornar 200 con el catálogo completo de planes")
	void listarPlanesDebeRetornarOkConCatalogoDePlanes() throws Exception {
		PlanType[] esperados = PlanType.values();

		mockMvc.perform(get("/plan"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()", is(esperados.length)))
				.andExpect(jsonPath("$[0].plan", is(esperados[0].name())))
				.andExpect(jsonPath("$[0].numeroCaracterPorSolicitud").isNumber())
				.andExpect(jsonPath("$[0].proveedores").isArray());

		verifyNoInteractions(userService);
	}

	/**
	 * Verifica que {@code GET /plan/actual} retorne HTTP 200 con el detalle del
	 * plan vigente del usuario autenticado.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("GET /plan/actual debe retornar 200 con el plan vigente del usuario autenticado")
	void obtenerPlanActualDebeRetornarOkConPlanDelUsuario() throws Exception {
		Authentication authentication = crearAutenticacionPorNombre("dsantiago");
		User usuario = new User();
		usuario.setPlanType(PlanType.FREE);

		when(userService.obtenerEntidadPorNombre("dsantiago")).thenReturn(usuario);

		mockMvc.perform(get("/plan/actual")
				.principal(authentication))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.plan", is(PlanType.FREE.name())))
				.andExpect(jsonPath("$.numeroCaracterPorSolicitud",
						is((int) PlanType.FREE.getNumeroCaracterPorSolicitud())))
				.andExpect(jsonPath("$.proveedores.length()", is(PlanType.FREE.getListaProveedor().size())));

		verify(userService).obtenerEntidadPorNombre("dsantiago");
	}

	/**
	 * Verifica que {@code GET /plan/actual} retorne HTTP 404 cuando el usuario
	 * autenticado no existe en la base de datos ({@link UserNotFoundException}).
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("GET /plan/actual debe retornar 404 cuando el usuario no existe")
	void obtenerPlanActualDebeRetornarNotFoundCuandoUsuarioNoExiste() throws Exception {
		Authentication authentication = crearAutenticacionPorNombre("inexistente");

		when(userService.obtenerEntidadPorNombre("inexistente")).thenThrow(new UserNotFoundException());

		mockMvc.perform(get("/plan/actual")
				.principal(authentication))
				.andExpect(status().isNotFound())
				.andExpect(content().string(is("El usuario no existe")));
	}

	/**
	 * Verifica que {@code PUT /plan/cambiar} retorne HTTP 200 cuando el plan
	 * {@code ORIA} y el usuario autenticado son válidos.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("PUT /plan/cambiar debe retornar 200 cuando el plan ORIA es válido")
	void cambiarPlanDebeRetornarOkCuandoPlanEsValido() throws Exception {
		Authentication authentication = crearAutenticacion(1L);
		UserResponseDTO responseDTO = new UserResponseDTO();

		when(userService.cambiarPlan(eq(1L), eq(PlanType.ORIA))).thenReturn(responseDTO);

		String body = """
				{
				  "plan": "ORIA"
				}
				""";

		MvcResult resultado = mockMvc.perform(put("/plan/cambiar")
				.principal(authentication)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isOk())
				.andReturn();

		assertThat(resultado.getResponse().getStatus()).isEqualTo(200);
		verify(userService).cambiarPlan(eq(1L), eq(PlanType.ORIA));
	}

	/**
	 * Verifica que {@code PUT /plan/cambiar} retorne HTTP 400 mediante
	 * {@link GlobalExceptionHandler} cuando el campo {@code plan} es nulo.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("PUT /plan/cambiar debe retornar 400 cuando el plan es null")
	void cambiarPlanDebeRetornarBadRequestCuandoPlanEsNull() throws Exception {
		Authentication authentication = mock(Authentication.class);

		String bodyInvalido = """
				{
				  "plan": null
				}
				""";

		mockMvc.perform(put("/plan/cambiar")
				.principal(authentication)
				.contentType(MediaType.APPLICATION_JSON)
				.content(bodyInvalido))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.error", is("Error de validación")));

		verifyNoInteractions(userService);
	}

	/**
	 * Verifica que {@code PUT /plan/cambiar} retorne HTTP 400 con el mensaje del
	 * manejador local cuando el enum de plan no existe.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("PUT /plan/cambiar debe retornar 400 cuando el tipo de plan no es válido")
	void cambiarPlanDebeRetornarBadRequestCuandoEnumEsInvalido() throws Exception {
		Authentication authentication = mock(Authentication.class);

		String bodyInvalido = """
				{
				  "plan": "PLAN_INEXISTENTE"
				}
				""";

		MvcResult resultado = mockMvc.perform(put("/plan/cambiar")
				.principal(authentication)
				.contentType(MediaType.APPLICATION_JSON)
				.content(bodyInvalido))
				.andExpect(status().isBadRequest())
				.andReturn();

		assertThat(resultado.getResponse().getContentAsString(StandardCharsets.UTF_8))
				.isEqualTo("Tipo de plan inválido");
		verifyNoInteractions(userService);
	}

	/**
	 * Verifica que {@code PUT /plan/cambiar} retorne HTTP 404 cuando el usuario
	 * autenticado no existe en la base de datos ({@link UserNotFoundException})
	 * al solicitar el plan {@code FREE}.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("PUT /plan/cambiar debe retornar 404 cuando el usuario no existe")
	void cambiarPlanDebeRetornarNotFoundCuandoUsuarioNoExiste() throws Exception {
		Authentication authentication = crearAutenticacion(99L);

		when(userService.cambiarPlan(eq(99L), eq(PlanType.FREE))).thenThrow(new UserNotFoundException());

		String body = """
				{
				  "plan": "FREE"
				}
				""";

		mockMvc.perform(put("/plan/cambiar")
				.principal(authentication)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isNotFound())
				.andExpect(content().string(is("El usuario no existe")));
	}

	/**
	 * Verifica que {@code PUT /plan/cambiar} retorne HTTP 400 cuando el servicio
	 * lanza {@link IllegalArgumentException} al solicitar el plan {@code INFINITIA}.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("PUT /plan/cambiar debe retornar 400 cuando el servicio lanza IllegalArgumentException")
	void cambiarPlanDebeRetornarBadRequestCuandoArgumentoEsInvalido() throws Exception {
		Authentication authentication = crearAutenticacion(1L);

		when(userService.cambiarPlan(eq(1L), eq(PlanType.INFINITIA)))
				.thenThrow(new IllegalArgumentException("El plan seleccionado no está disponible"));

		String body = """
				{
				  "plan": "INFINITIA"
				}
				""";

		mockMvc.perform(put("/plan/cambiar")
				.principal(authentication)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(is("El plan seleccionado no está disponible")));
	}
}