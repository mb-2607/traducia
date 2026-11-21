package co.edu.unbosque.traducia.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import co.edu.unbosque.traducia.dto.AuditoriaLogDTO;
import co.edu.unbosque.traducia.exception.GlobalExceptionHandler;
import co.edu.unbosque.traducia.service.AuditoriaService;

/**
 * Pruebas unitarias para {@link AuditoriaController} utilizando JUnit 5,
 * Mockito, MockMvc, Hamcrest y AssertJ.
 *
 * <p>Valida la consulta paginada de registros de auditoría en
 * {@code GET /auditoria/obtener}, verificando tanto respuestas con resultados
 * como páginas vacías y la correcta propagación de los parámetros de
 * paginación hacia {@link AuditoriaService}.</p>
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class AuditoriaControllerTest {

	@Mock
	private AuditoriaService auditServ;

	@InjectMocks
	private AuditoriaController auditoriaController;

	private MockMvc mockMvc;

	/**
	 * Configura {@link MockMvc} de forma aislada con el controlador, el manejador
	 * global {@link GlobalExceptionHandler}, el resolvedor de {@link Pageable} y
	 * la propiedad {@code front.url}.
	 */
	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(auditoriaController)
				.setControllerAdvice(new GlobalExceptionHandler())
				.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
				.addPlaceholderValue("front.url", "http://localhost:4200")
				.build();
	}

	/**
	 * Verifica que {@code GET /auditoria/obtener} retorne HTTP 200 con la página
	 * de registros y propague correctamente los parámetros {@code page} y
	 * {@code size} al servicio.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("GET /auditoria/obtener debe retornar 200 con registros paginados")
	void listarDebeRetornarOkConRegistrosPaginados() throws Exception {
		AuditoriaLogDTO log1 = new AuditoriaLogDTO();
		AuditoriaLogDTO log2 = new AuditoriaLogDTO();
		Page<AuditoriaLogDTO> pagina = new PageImpl<>(List.of(log1, log2), PageRequest.of(1, 5), 2);

		when(auditServ.listar(any(Pageable.class))).thenReturn(pagina);

		MvcResult resultado = mockMvc.perform(get("/auditoria/obtener")
				.param("page", "1")
				.param("size", "5")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(2)))
				.andExpect(jsonPath("$.totalElements", is(7)))
				.andReturn();

		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(auditServ).listar(pageableCaptor.capture());

		Pageable pageableRecibido = pageableCaptor.getValue();
		assertThat(resultado.getResponse().getStatus()).isEqualTo(200);
		assertThat(pageableRecibido.getPageNumber()).isEqualTo(1);
		assertThat(pageableRecibido.getPageSize()).isEqualTo(5);
	}

	/**
	 * Verifica que {@code GET /auditoria/obtener} retorne HTTP 200 con una página
	 * vacía cuando no existen registros de auditoría.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("GET /auditoria/obtener debe retornar 200 con página vacía cuando no hay registros")
	void listarDebeRetornarOkConPaginaVacia() throws Exception {
		Page<AuditoriaLogDTO> paginaVacia = Page.empty(PageRequest.of(0, 10));

		when(auditServ.listar(any(Pageable.class))).thenReturn(paginaVacia);

		mockMvc.perform(get("/auditoria/obtener")
				.param("page", "0")
				.param("size", "10")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(0)))
				.andExpect(jsonPath("$.totalElements", is(0)));

		verify(auditServ).listar(any(Pageable.class));
	}
}