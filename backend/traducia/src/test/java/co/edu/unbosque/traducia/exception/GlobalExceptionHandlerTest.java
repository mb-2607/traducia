package co.edu.unbosque.traducia.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Pruebas unitarias para {@link GlobalExceptionHandler} utilizando JUnit 5,
 * MockMvc, Hamcrest y AssertJ.
 *
 * <p>Verifica que los errores de validación de Bean Validation ({@code @Valid})
 * sean interceptados y devueltos con estado HTTP 400 y la estructura JSON
 * estandarizada.</p>
 *
 * @author
 * @version 1.0
 */
class GlobalExceptionHandlerTest {

	private MockMvc mockMvc;

	/**
	 * Configura una instancia aislada de {@link MockMvc} con un controlador de
	 * prueba y el manejador global {@link GlobalExceptionHandler}.
	 */
	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new DummyController())
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	/**
	 * Verifica que al enviar un cuerpo con campos inválidos se retorne un estado
	 * HTTP 400 (Bad Request) con el detalle de cada campo fallido.
	 *
	 * @throws Exception si ocurre un error al ejecutar la petición simulada.
	 */
	@Test
	@DisplayName("Debe retornar 400 y el detalle de campos cuando falla @Valid")
	void debeRetornarBadRequestCuandoFallaValidacion() throws Exception {
		String bodyInvalido = """
				{
				  "nombre": "",
				  "correo": "correo-invalido"
				}
				""";

		MvcResult resultado = mockMvc.perform(post("/test/validar")
				.contentType(MediaType.APPLICATION_JSON)
				.content(bodyInvalido))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.error", is("Error de validación")))
				.andExpect(jsonPath("$.timestamp", notNullValue()))
				.andExpect(jsonPath("$.errores.nombre", is("El nombre es obligatorio")))
				.andExpect(jsonPath("$.errores.correo", is("El correo no es válido")))
				.andReturn();

		assertThat(resultado.getResponse().getContentAsString())
				.contains("Error de validación")
				.contains("El nombre es obligatorio");
	}

	/**
	 * Controlador auxiliar utilizado únicamente para disparar validaciones
	 * {@code @Valid} en las pruebas unitarias.
	 */
	@RestController
	static class DummyController {

		/**
		 * Endpoint de prueba que recibe un DTO validado.
		 *
		 * @param request cuerpo de la petición a validar.
		 * @return respuesta exitosa cuando el DTO cumple todas las restricciones.
		 */
		@PostMapping("/test/validar")
		String probar(@Valid @RequestBody DummyRequest request) {
			return "OK";
		}
	}

	/**
	 * DTO auxiliar con restricciones de validación para probar la captura de
	 * {@code MethodArgumentNotValidException}.
	 */
	static class DummyRequest {

		@NotBlank(message = "El nombre es obligatorio")
		private String nombre;

		@Email(message = "El correo no es válido")
		@NotBlank(message = "El correo es obligatorio")
		private String correo;

		/**
		 * Obtiene el nombre del objeto de prueba.
		 *
		 * @return nombre configurado.
		 */
		public String getNombre() {
			return nombre;
		}

		/**
		 * Asigna el nombre del objeto de prueba.
		 *
		 * @param nombre valor del nombre.
		 */
		public void setNombre(String nombre) {
			this.nombre = nombre;
		}

		/**
		 * Obtiene el correo del objeto de prueba.
		 *
		 * @return correo configurado.
		 */
		public String getCorreo() {
			return correo;
		}

		/**
		 * Asigna el correo del objeto de prueba.
		 *
		 * @param correo valor del correo.
		 */
		public void setCorreo(String correo) {
			this.correo = correo;
		}
	}
}
