package co.edu.unbosque.traducia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import co.edu.unbosque.traducia.dto.DocumentoExtraidoResponseDTO;
import co.edu.unbosque.traducia.entity.User;
import co.edu.unbosque.traducia.exception.DocumentoException;

/**
 * Prueba límites de plan, preservación de texto y ruta local de TXT.
 *
 * <p>
 * Verifica que {@link DocumentoService} conserve el contenido completo, informe
 * cuando se supera el límite del plan y rechace documentos cuyo texto contiene
 * únicamente espacios o saltos de línea.
 * </p>
 *
 * <p>
 * También comprueba que el conteo utiliza unidades UTF-16, de forma consistente
 * con {@link String#length()} y con la validación del servicio de traducción
 * existente.
 * </p>
 *
 * <p>
 * Utiliza archivos en memoria y servicios simulados mediante Mockito. No
 * requiere acceso a una base de datos ni a APIs externas.
 * </p>
 */
class DocumentoServiceTest {

	/**
	 * Comprueba que un archivo TXT de 501 caracteres conserva todo su contenido
	 * cuando el usuario de prueba dispone de un límite de 500.
	 *
	 * <p>
	 * Verifica el texto devuelto, su longitud, el límite informado y el indicador
	 * de exceso. También comprueba que la lectura local de TXT no invoca al
	 * proveedor LlamaParse.
	 * </p>
	 */
	@Test
	void informaElExcesoSinRecortarNiInvocarLlamaParseParaTxt() {
		LlamaParseService proveedor = mock(LlamaParseService.class);
		UserService usuarios = mock(UserService.class);
		when(usuarios.obtenerEntidadPorNombre("usuario")).thenReturn(new User());
		DocumentoService servicio = new DocumentoService(new DocumentoArchivoValidator("10MB"), proveedor, usuarios);
		String texto = "a".repeat(501);
		DocumentoExtraidoResponseDTO respuesta = servicio.extraer(archivo(texto), "es", "usuario");
		assertEquals(texto, respuesta.texto());
		assertEquals(501, respuesta.caracteres());
		assertEquals(500, respuesta.limiteCaracteres());
		assertTrue(respuesta.excedeLimite());

		verifyNoInteractions(proveedor);
	}

	/**
	 * Comprueba que la longitud del texto se calcula mediante unidades UTF-16,
	 * igual que en el servicio de traducción existente.
	 *
	 * <p>
	 * El emoji utilizado ocupa dos unidades UTF-16, por lo que el resultado
	 * esperado es dos aunque se represente como un único símbolo visible.
	 * </p>
	 */
	@Test
	void cuentaUtf16ComoElServicioDeTraduccionExistente() {
		UserService usuarios = mock(UserService.class);
		when(usuarios.obtenerEntidadPorNombre("usuario")).thenReturn(new User());
		DocumentoService servicio = new DocumentoService(new DocumentoArchivoValidator("10MB"),
				mock(LlamaParseService.class), usuarios);

		assertEquals(2, servicio.extraer(archivo("🐱"), "es", "usuario").caracteres());
	}

	/**
	 * Comprueba que un archivo TXT compuesto únicamente por espacios y un salto de
	 * línea se rechaza mediante una {@link DocumentoException} con código HTTP 422.
	 */
	@Test
	void rechazaArchivoConSoloEspacios() {
		UserService usuarios = mock(UserService.class);
		when(usuarios.obtenerEntidadPorNombre("usuario")).thenReturn(new User());
		DocumentoService servicio = new DocumentoService(new DocumentoArchivoValidator("10MB"),
				mock(LlamaParseService.class), usuarios);

		assertEquals(422,
				assertThrows(DocumentoException.class, () -> servicio.extraer(archivo("  \n"), "es", "usuario"))
						.getStatusCode());
	}

	/**
	 * Construye un archivo TXT multipart en memoria con contenido UTF-8.
	 *
	 * @param texto contenido que se incorpora al archivo de prueba
	 * @return archivo llamado {@code ejemplo.txt}, con tipo MIME {@code text/plain}
	 *         y campo multipart {@code archivo}
	 */
	private static MockMultipartFile archivo(String texto) {
		return new MockMultipartFile("archivo", "ejemplo.txt", "text/plain", texto.getBytes(StandardCharsets.UTF_8));
	}
}