package co.edu.unbosque.traducia.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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

import co.edu.unbosque.traducia.dto.IdiomaResponseDTO;
import co.edu.unbosque.traducia.exception.GlobalExceptionHandler;
import co.edu.unbosque.traducia.service.IdiomaService;

/**
 * Pruebas unitarias para {@link IdiomaController} utilizando JUnit 5, Mockito,
 * MockMvc, Hamcrest y AssertJ.
 *
 * <p>Valida la consulta de idiomas disponibles en {@code GET /idiomas},
 * comprobando respuestas con elementos y listas vacías.</p>
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class IdiomaControllerTest {

	@Mock
	private IdiomaService idiomaServ;

	@InjectMocks
	private IdiomaController idiomaController;

	private MockMvc mockMvc;

	/**
	 * Configura {@link MockMvc} de forma aislada con el controlador, el manejador
	 * global {@link GlobalExceptionHandler} y la propiedad {@code front.url}.
	 */
	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(idiomaController)
				.setControllerAdvice(new GlobalExceptionHandler())
				.addPlaceholderValue("front.url", "http://localhost:4200")
				.build();
	}

	/**
	 * Verifica que {@code GET /idiomas} retorne HTTP 200 y la lista de idiomas
	 * cuando existen registros disponibles.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("GET /idiomas debe retornar 200 con la lista de idiomas disponibles")
	void listarIdiomaDebeRetornarOkConListaDeIdiomas() throws Exception {
		IdiomaResponseDTO idioma1 = new IdiomaResponseDTO();
		IdiomaResponseDTO idioma2 = new IdiomaResponseDTO();
		when(idiomaServ.listarIdioma()).thenReturn(List.of(idioma1, idioma2));

		MvcResult resultado = mockMvc.perform(get("/idiomas")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andReturn();

		assertThat(resultado.getResponse().getStatus()).isEqualTo(200);
		verify(idiomaServ).listarIdioma();
	}

	/**
	 * Verifica que {@code GET /idiomas} retorne HTTP 200 y una lista vacía cuando
	 * no hay idiomas registrados.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("GET /idiomas debe retornar 200 con lista vacía cuando no hay idiomas")
	void listarIdiomaDebeRetornarOkConListaVacia() throws Exception {
		when(idiomaServ.listarIdioma()).thenReturn(List.of());

		mockMvc.perform(get("/idiomas")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(is(0))));

		verify(idiomaServ).listarIdioma();
	}
}