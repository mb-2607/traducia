package co.edu.unbosque.traducia.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import co.edu.unbosque.traducia.dto.SolicitudTraduccionResponseDTO;
import co.edu.unbosque.traducia.dto.TraduccionRequestDTO;
import co.edu.unbosque.traducia.exception.IdiomaEqualsException;
import co.edu.unbosque.traducia.exception.PlanCharacterLimitExceededException;
import co.edu.unbosque.traducia.service.TraduccionService;

/**
 * Pruebas unitarias para {@link TraduccionController} utilizando MockMvc,
 * JUnit 5, Mockito y AssertJ.
 *
 * <p>Valida los endpoints {@code POST /traductor/traducir},
 * {@code GET /traductor/historial} (paginado), {@code GET /traductor/{id}} y el
 * manejo de excepciones {@link PlanCharacterLimitExceededException} e
 * {@link IdiomaEqualsException}.</p>
 */
@ExtendWith(MockitoExtension.class)
class TraduccionControllerTest {

	/**
	 * Simulador de peticiones HTTP hacia el controlador.
	 */
	private MockMvc mockMvc;

	/**
	 * Mock del servicio de traducción.
	 */
	@Mock
	private TraduccionService traduccionService;

	/**
	 * Controlador bajo prueba con el servicio inyectado.
	 */
	@InjectMocks
	private TraduccionController traduccionController;

	/**
	 * Inicializa {@link MockMvc} en modo standalone con soporte para
	 * {@link Pageable} y el placeholder {@code front.url}.
	 */
	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(traduccionController)
				.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
				.addPlaceholderValue("front.url", "http://localhost:4200")
				.build();
	}

	/**
	 * Crea una autenticación simulada con el nombre de usuario indicado.
	 *
	 * @param nombreUsuario nombre del usuario autenticado
	 * @return objeto {@link Authentication} configurado
	 */
	private Authentication crearAutenticacion(String nombreUsuario) {
		return new UsernamePasswordAuthenticationToken(nombreUsuario, null);
	}

	/**
	 * Crea un {@link SolicitudTraduccionResponseDTO} de prueba con datos básicos.
	 *
	 * @param id            identificador de la solicitud
	 * @param textoOriginal texto original enviado a traducir
	 * @return DTO configurado para pruebas
	 */
	private SolicitudTraduccionResponseDTO crearRespuestaDTO(long id, String textoOriginal) {
		SolicitudTraduccionResponseDTO dto = new SolicitudTraduccionResponseDTO();
		dto.setId(id);
		dto.setOrigen("es");
		dto.setDestino("en");
		dto.setTextoOriginal(textoOriginal);
		dto.setListaTraduccion(List.of());
		return dto;
	}

	/**
	 * Verifica que {@code POST /traductor/traducir} retorne HTTP 201 con la
	 * solicitud creada cuando el servicio ejecuta la traducción correctamente.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada
	 */
	@Test
	@DisplayName("POST /traductor/traducir debe retornar 201 Created con la solicitud generada")
	void traducirDebeRetornarCreatedConSolicitud() throws Exception {
		Authentication auth = crearAutenticacion("dsantiago");
		SolicitudTraduccionResponseDTO respuesta = crearRespuestaDTO(10L, "Hola mundo");

		when(traduccionService.traducir(any(TraduccionRequestDTO.class), eq("dsantiago")))
				.thenReturn(respuesta);

		String body = """
				{
				  "origen": "es",
				  "destino": "en",
				  "texto": "Hola mundo"
				}
				""";

		mockMvc.perform(post("/traductor/traducir")
				.principal(auth)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.origen").value("es"))
				.andExpect(jsonPath("$.destino").value("en"))
				.andExpect(jsonPath("$.textoOriginal").value("Hola mundo"));

		verify(traduccionService).traducir(any(TraduccionRequestDTO.class), eq("dsantiago"));
	}

	/**
	 * Verifica que {@code POST /traductor/traducir} retorne HTTP 400 con el
	 * mensaje configurado cuando el texto supera el límite de caracteres del plan.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada
	 */
	@Test
	@DisplayName("POST /traductor/traducir debe retornar 400 cuando se excede el límite del plan")
	void traducirDebeRetornarBadRequestCuandoExcedeLimiteDelPlan() throws Exception {
		Authentication auth = crearAutenticacion("dsantiago");

		when(traduccionService.traducir(any(TraduccionRequestDTO.class), eq("dsantiago")))
				.thenThrow(new PlanCharacterLimitExceededException());

		String body = """
				{
				  "origen": "es",
				  "destino": "en",
				  "texto": "Texto que excede el limite permitido"
				}
				""";

		MvcResult resultado = mockMvc.perform(post("/traductor/traducir")
				.principal(auth)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isBadRequest())
				.andReturn();

		assertThat(resultado.getResponse().getContentAsString(StandardCharsets.UTF_8))
				.isEqualTo("Cantidad de caracteres excedidad frente al plan actual");
	}

	/**
	 * Verifica que {@code POST /traductor/traducir} retorne HTTP 400 con el
	 * mensaje de {@link IdiomaEqualsException} cuando el idioma de origen y
	 * destino son iguales.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada
	 */
	@Test
	@DisplayName("POST /traductor/traducir debe retornar 400 cuando origen y destino son iguales")
	void traducirDebeRetornarBadRequestCuandoIdiomasSonIguales() throws Exception {
		Authentication auth = crearAutenticacion("dsantiago");

		when(traduccionService.traducir(any(TraduccionRequestDTO.class), eq("dsantiago")))
				.thenThrow(new IdiomaEqualsException("Los idiomas son iguales"));

		String body = """
				{
				  "origen": "es",
				  "destino": "es",
				  "texto": "Hola"
				}
				""";

		MvcResult resultado = mockMvc.perform(post("/traductor/traducir")
				.principal(auth)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isBadRequest())
				.andReturn();

		assertThat(resultado.getResponse().getContentAsString(StandardCharsets.UTF_8))
				.isEqualTo("Los idiomas son iguales");
	}

	/**
	 * Verifica que {@code GET /traductor/historial} retorne HTTP 200 junto con la
	 * página de solicitudes de traducción del usuario autenticado.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada
	 */
	@Test
	@DisplayName("GET /traductor/historial debe retornar 200 OK con el historial paginado del usuario")
	void listarHistorialDebeRetornarOkConListaDeSolicitudes() throws Exception {
		Authentication auth = crearAutenticacion("dsantiago");
		Page<SolicitudTraduccionResponseDTO> pagina = new PageImpl<>(
				List.of(crearRespuestaDTO(10L, "Hola"), crearRespuestaDTO(11L, "Adios")),
				PageRequest.of(0, 5),
				2);

		when(traduccionService.listarPorUsuario(eq("dsantiago"), any(Pageable.class))).thenReturn(pagina);

		mockMvc.perform(get("/traductor/historial")
				.param("page", "0")
				.param("size", "5")
				.principal(auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(2))
				.andExpect(jsonPath("$.totalElements").value(2))
				.andExpect(jsonPath("$.content[0].id").value(10))
				.andExpect(jsonPath("$.content[0].textoOriginal").value("Hola"))
				.andExpect(jsonPath("$.content[1].id").value(11))
				.andExpect(jsonPath("$.content[1].textoOriginal").value("Adios"));

		verify(traduccionService).listarPorUsuario(eq("dsantiago"), any(Pageable.class));
	}

	/**
	 * Verifica que {@code GET /traductor/{id}} retorne HTTP 200 con el detalle de
	 * la solicitud solicitada para el usuario autenticado.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada
	 */
	@Test
	@DisplayName("GET /traductor/{id} debe retornar 200 OK con el detalle de la solicitud")
	void obtenerPorIdDebeRetornarOkConDetalleDeSolicitud() throws Exception {
		Authentication auth = crearAutenticacion("dsantiago");
		SolicitudTraduccionResponseDTO detalle = crearRespuestaDTO(30L, "Buenos dias");

		when(traduccionService.obtenerPorIdYUsuario(30L, "dsantiago")).thenReturn(detalle);

		mockMvc.perform(get("/traductor/30")
				.principal(auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(30))
				.andExpect(jsonPath("$.origen").value("es"))
				.andExpect(jsonPath("$.destino").value("en"))
				.andExpect(jsonPath("$.textoOriginal").value("Buenos dias"));

		verify(traduccionService).obtenerPorIdYUsuario(30L, "dsantiago");
	}
}