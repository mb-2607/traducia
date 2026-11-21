package co.edu.unbosque.traducia.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

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
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import co.edu.unbosque.traducia.dto.AdminChangePasswordRequestDTO;
import co.edu.unbosque.traducia.dto.AdminCreateUserRequestDTO;
import co.edu.unbosque.traducia.dto.UpdateUserRequestDTO;
import co.edu.unbosque.traducia.dto.UserResponseDTO;
import co.edu.unbosque.traducia.entity.User;
import co.edu.unbosque.traducia.exception.EmailAlreadyExistsException;
import co.edu.unbosque.traducia.exception.GlobalExceptionHandler;
import co.edu.unbosque.traducia.exception.InvalidPasswordException;
import co.edu.unbosque.traducia.exception.UserNotFoundException;
import co.edu.unbosque.traducia.exception.UsernameAlreadyExistsException;
import co.edu.unbosque.traducia.service.UserService;

/**
 * Pruebas unitarias para {@link UserController} utilizando JUnit 5, Mockito,
 * MockMvc, Hamcrest y AssertJ.
 *
 * <p>Valida los endpoints administrativos de {@code /user} (listar, paginado,
 * consultar por id, crear, actualizar, cambiar contraseña y eliminar), el bloqueo
 * de auto-modificación o auto-eliminación del administrador y el manejo de
 * excepciones de negocio.</p>
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

	@Mock
	private UserService userService;

	@InjectMocks
	private UserController userController;

	private MockMvc mockMvc;

	/**
	 * Configura {@link MockMvc} de forma aislada con el controlador, el manejador
	 * global {@link GlobalExceptionHandler}, el resolvedor de {@link Pageable} y
	 * la propiedad {@code front.url}.
	 */
	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(userController)
				.setControllerAdvice(new GlobalExceptionHandler())
				.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
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
	 * Verifica que {@code GET /user/listar} retorne HTTP 200 con la lista de
	 * usuarios registrados.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("GET /user/listar debe retornar 200 con la lista de usuarios")
	void listarDebeRetornarOkConListaDeUsuarios() throws Exception {
		UserResponseDTO u1 = new UserResponseDTO();
		UserResponseDTO u2 = new UserResponseDTO();
		when(userService.listar()).thenReturn(List.of(u1, u2));

		MvcResult resultado = mockMvc.perform(get("/user/listar")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andReturn();

		assertThat(resultado.getResponse().getStatus()).isEqualTo(200);
		verify(userService).listar();
	}

	/**
	 * Verifica que {@code GET /user/paginado} retorne HTTP 200 con la página de
	 * usuarios y propague los parámetros de paginación.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("GET /user/paginado debe retornar 200 con usuarios paginados")
	void listarPaginadoDebeRetornarOkConPagina() throws Exception {
		UserResponseDTO u1 = new UserResponseDTO();
		Page<UserResponseDTO> pagina = new PageImpl<>(List.of(u1), PageRequest.of(0, 5), 1);
		when(userService.listar(any(Pageable.class))).thenReturn(pagina);

		mockMvc.perform(get("/user/paginado")
				.param("page", "0")
				.param("size", "5")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.totalElements", is(1)));

		verify(userService).listar(any(Pageable.class));
	}

	/**
	 * Verifica que {@code GET /user/{id}} retorne HTTP 200 cuando el usuario
	 * existe y HTTP 404 cuando no se encuentra.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("GET /user/{id} debe retornar 200 si existe y 404 si no existe")
	void obtenerPorIdDebeRetornarOkONotFound() throws Exception {
		UserResponseDTO responseDTO = new UserResponseDTO();
		when(userService.obtenerPorId(1L)).thenReturn(responseDTO);
		when(userService.obtenerPorId(99L)).thenThrow(new UserNotFoundException());

		mockMvc.perform(get("/user/1").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		mockMvc.perform(get("/user/99").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(content().string(is("El usuario no existe")));

		verify(userService).obtenerPorId(1L);
		verify(userService).obtenerPorId(99L);
	}

	/**
	 * Verifica que {@code POST /user/crear} retorne HTTP 201 con datos válidos y
	 * HTTP 409/400 ante conflictos de duplicidad o contraseña inválida.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("POST /user/crear debe retornar 201 cuando los datos son válidos")
	void crearDebeRetornarCreatedCuandoDatosSonValidos() throws Exception {
		UserResponseDTO responseDTO = new UserResponseDTO();
		when(userService.crear(any(AdminCreateUserRequestDTO.class))).thenReturn(responseDTO);

		String body = """
				{
				  "nombre": "nuevoUsuario",
				  "correo": "nuevo@ejemplo.com",
				  "password": "Password123!",
				  "rol": "USER",
				  "planId": "FREE"
				}
				""";

		mockMvc.perform(post("/user/crear")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andDo(print())
				.andExpect(status().isCreated());

		verify(userService).crear(any(AdminCreateUserRequestDTO.class));
	}

	/**
	 * Verifica que {@code POST /user/crear} maneje
	 * {@link EmailAlreadyExistsException}, {@link UsernameAlreadyExistsException} e
	 * {@link InvalidPasswordException} con sus respectivos códigos HTTP.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("POST /user/crear debe manejar excepciones de duplicidad y contraseña inválida")
	void crearDebeManejarExcepcionesDeNegocio() throws Exception {
		String body = """
				{
				  "nombre": "usuario",
				  "correo": "usuario@ejemplo.com",
				  "password": "Password123!",
				  "rol": "USER",
				  "planId": "FREE"
				}
				""";

		when(userService.crear(any(AdminCreateUserRequestDTO.class)))
				.thenThrow(new EmailAlreadyExistsException("Correo ya registrado"));

		mockMvc.perform(post("/user/crear")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isConflict())
				.andExpect(content().string(is("El correo ya existe")));

		when(userService.crear(any(AdminCreateUserRequestDTO.class)))
				.thenThrow(new UsernameAlreadyExistsException("Usuario ya registrado"));

		mockMvc.perform(post("/user/crear")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isConflict())
				.andExpect(content().string(is("El usuario ya existe")));

		when(userService.crear(any(AdminCreateUserRequestDTO.class)))
				.thenThrow(new InvalidPasswordException("Contraseña débil"));

		mockMvc.perform(post("/user/crear")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(is("Contraseña débil")));
	}

	@Test
	@DisplayName("PUT /user/actualizar/{id} debe retornar 200 en otro usuario y 403 en sí mismo")
	void actualizarDebeRetornarOkOForbiddenSegunId() throws Exception {
		Authentication authAdmin = crearAutenticacion(1L);
		UserResponseDTO responseDTO = new UserResponseDTO();
		when(userService.actualizar(eq(2L), any(UpdateUserRequestDTO.class))).thenReturn(responseDTO);

		String body = """
				{
				  "nombre": "actualizado",
				  "correo": "actualizado@ejemplo.com",
				  "rol": "ADMIN",
				  "planId": "ORIA"
				}
				""";

		mockMvc.perform(put("/user/actualizar/2")
				.principal(authAdmin)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isOk());

		verify(userService).actualizar(eq(2L), any(UpdateUserRequestDTO.class));

		MvcResult resultadoPropio = mockMvc.perform(put("/user/actualizar/1")
				.principal(authAdmin)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isForbidden())
				.andReturn();

		assertThat(resultadoPropio.getResponse().getContentAsString(StandardCharsets.UTF_8))
				.isEqualTo("Un administrador no puede actualizarse a sí mismo");
		verify(userService, never()).actualizar(eq(1L), any());
	}

	/**
	 * Verifica que {@code PUT /user/cambiar-password/{id}} retorne HTTP 200 y
	 * delegue el cambio de contraseña en {@link UserService}.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("PUT /user/cambiar-password/{id} debe retornar 200 cuando es válido")
	void cambiarPasswordDebeRetornarOk() throws Exception {
		UserResponseDTO responseDTO = new UserResponseDTO();
		when(userService.cambiarPassword(eq(2L), any(AdminChangePasswordRequestDTO.class)))
				.thenReturn(responseDTO);

		String body = """
				{
				  "nuevaPassword": "NuevaPassword123!"
				}
				""";

		mockMvc.perform(put("/user/cambiar-password/2")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isOk());

		verify(userService).cambiarPassword(eq(2L), any(AdminChangePasswordRequestDTO.class));
	}

	/**
	 * Verifica que {@code DELETE /user/eliminar/{id}} retorne HTTP 200 al
	 * eliminar otro usuario y HTTP 403 si el administrador intenta eliminarse a
	 * sí mismo.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("DELETE /user/eliminar/{id} debe retornar 200 en otro usuario y 403 en sí mismo")
	void eliminarDebeRetornarOkOForbiddenSegunId() throws Exception {
		Authentication authAdmin = crearAutenticacion(1L);

		mockMvc.perform(delete("/user/eliminar/2")
				.principal(authAdmin))
				.andExpect(status().isOk())
				.andExpect(content().string(is("Usuario eliminado")));

		verify(userService).eliminar(2L);

		MvcResult resultadoPropio = mockMvc.perform(delete("/user/eliminar/1")
				.principal(authAdmin))
				.andExpect(status().isForbidden())
				.andReturn();

		assertThat(resultadoPropio.getResponse().getContentAsString(StandardCharsets.UTF_8))
				.isEqualTo("Un administrador no puede eliminarse a sí mismo");
		verify(userService, never()).eliminar(1L);
	}

	/**
	 * Verifica que {@code POST /user/crear} retorne HTTP 400 mediante
	 * {@link GlobalExceptionHandler} cuando el cuerpo JSON está vacío.
	 *
	 * @throws Exception si falla la ejecución de la petición simulada.
	 */
	@Test
	@DisplayName("POST /user/crear debe retornar 400 cuando falla la validación")
	void crearDebeRetornarBadRequestCuandoFallaValidacion() throws Exception {
		mockMvc.perform(post("/user/crear")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.error", is("Error de validación")));

		verifyNoInteractions(userService);
	}
}
