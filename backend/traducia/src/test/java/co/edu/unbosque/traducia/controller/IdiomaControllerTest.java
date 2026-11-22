package co.edu.unbosque.traducia.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import co.edu.unbosque.traducia.dto.IdiomaResponseDTO;
import co.edu.unbosque.traducia.entity.PlanType;
import co.edu.unbosque.traducia.entity.ProveedorTraduccion;
import co.edu.unbosque.traducia.entity.User;
import co.edu.unbosque.traducia.service.IdiomaService;
import co.edu.unbosque.traducia.service.UserService;

/**
 * Pruebas unitarias para {@link IdiomaController} utilizando JUnit 5, Mockito,
 * MockMvc y Hamcrest.
 *
 * <p>Valida la consulta del catálogo de idiomas ({@code GET /idiomas}) y de los
 * proveedores de traducción disponibles ({@code GET /idiomas/proveedores}) con
 * y sin usuario autenticado.</p>
 *
 * @version 1.1
 */
@ExtendWith(MockitoExtension.class)
class IdiomaControllerTest {

	@Mock
	private IdiomaService idiomaService;

	@Mock
	private UserService userService;

	@InjectMocks
	private IdiomaController idiomaController;

	private MockMvc mockMvc;

	/**
	 * Configura {@link MockMvc} de forma aislada con el controlador y el
	 * placeholder {@code front.url} requerido por {@code @CrossOrigin}.
	 */
	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(idiomaController)
				.addPlaceholderValue("front.url", "http://localhost:4200")
				.build();
	}

	/**
	 * Verifica que {@code GET /idiomas} retorne HTTP 200 junto con los idiomas
	 * devueltos por {@link IdiomaService}.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("GET /idiomas debe retornar 200 con la lista de idiomas registrados")
	void listarIdiomaDebeRetornarOkConListaDeIdiomas() throws Exception {
		List<IdiomaResponseDTO> idiomas = List.of(new IdiomaResponseDTO(), new IdiomaResponseDTO());

		when(idiomaService.listarIdioma()).thenReturn(idiomas);

		mockMvc.perform(get("/idiomas"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()", is(2)));

		verify(idiomaService).listarIdioma();
		verifyNoInteractions(userService);
	}

	/**
	 * Verifica que {@code GET /idiomas} retorne HTTP 200 con una lista vacía
	 * cuando no existen idiomas registrados.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("GET /idiomas debe retornar 200 con lista vacía si no hay idiomas")
	void listarIdiomaDebeRetornarOkConListaVacia() throws Exception {
		when(idiomaService.listarIdioma()).thenReturn(List.of());

		mockMvc.perform(get("/idiomas"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()", is(0)));

		verify(idiomaService).listarIdioma();
		verifyNoInteractions(userService);
	}

	/**
	 * Verifica que {@code GET /idiomas/proveedores} marque la disponibilidad de
	 * cada proveedor según el {@link PlanType} del usuario autenticado.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("GET /idiomas/proveedores debe indicar disponibilidad según el plan del usuario")
	void listarProveedoresDebeIndicarDisponibilidadSegunPlan() throws Exception {
		User usuario = new User();
		usuario.setPlanType(PlanType.FREE);
		Authentication auth = new UsernamePasswordAuthenticationToken("dsantiago", null);

		when(userService.obtenerEntidadPorNombre("dsantiago")).thenReturn(usuario);

		ProveedorTraduccion[] esperados = ProveedorTraduccion.values();
		boolean disponiblePrimero = PlanType.FREE.getListaProveedor().contains(esperados[0]);

		mockMvc.perform(get("/idiomas/proveedores")
				.principal(auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()", is(esperados.length)))
				.andExpect(jsonPath("$[0].nombre", is(esperados[0].name())))
				.andExpect(jsonPath("$[0].tipoProveedor", is(String.valueOf(esperados[0].getTipoProveedor()))))
				.andExpect(jsonPath("$[0].disponibleEnPlan", is(disponiblePrimero)));

		verify(userService).obtenerEntidadPorNombre("dsantiago");
		verifyNoInteractions(idiomaService);
	}

	/**
	 * Verifica que {@code GET /idiomas/proveedores} retorne todos los proveedores
	 * con {@code disponibleEnPlan} en {@code false} cuando no hay autenticación.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("GET /idiomas/proveedores sin autenticación debe retornar disponibleEnPlan en false")
	void listarProveedoresSinAutenticacionDebeRetornarDisponibleFalse() throws Exception {
		ProveedorTraduccion[] esperados = ProveedorTraduccion.values();

		mockMvc.perform(get("/idiomas/proveedores"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()", is(esperados.length)))
				.andExpect(jsonPath("$[0].nombre", is(esperados[0].name())))
				.andExpect(jsonPath("$[0].disponibleEnPlan", is(false)));

		verifyNoInteractions(idiomaService, userService);
	}
}