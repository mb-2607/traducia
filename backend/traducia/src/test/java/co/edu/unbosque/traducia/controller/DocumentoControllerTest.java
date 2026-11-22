package co.edu.unbosque.traducia.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import co.edu.unbosque.traducia.dto.DocumentoExtraidoResponseDTO;
import co.edu.unbosque.traducia.dto.SolicitudTraduccionResponseDTO;
import co.edu.unbosque.traducia.exception.DocumentoException;
import co.edu.unbosque.traducia.exception.ExternalAPIException;
import co.edu.unbosque.traducia.service.DocumentoService;
import co.edu.unbosque.traducia.service.TraduccionService;

/**
 * Comprueba el reuso del traductor y el bloqueo de documentos que exceden el
 * plan.
 */
class DocumentoControllerTest {
	private final DocumentoService documentos = mock(DocumentoService.class);
	private final TraduccionService traducciones = mock(TraduccionService.class);
	private final DocumentoController controller = new DocumentoController(documentos, traducciones);
	private final Authentication authentication = new UsernamePasswordAuthenticationToken("usuario", null);
	private final MockMultipartFile archivo = new MockMultipartFile("archivo", new byte[] { 1 });

	/**
	 * Comprueba que un documento que supera el límite del plan provoca un error
	 * HTTP 400 sin invocar el servicio de traducción.
	 */
	@Test
	void noInvocaTraductoresSiElTextoSuperaElPlan() {
		when(documentos.extraer(archivo, "es", "usuario"))
				.thenReturn(new DocumentoExtraidoResponseDTO("texto", 501, 500, true));
		assertEquals(400,
				assertThrows(DocumentoException.class, () -> controller.traducir(archivo, "es", "en", authentication))
						.getStatusCode());
		verifyNoInteractions(traducciones);
	}

	/**
	 * Comprueba que un documento dentro del límite del plan se traduce mediante el
	 * servicio existente y devuelve HTTP 201.
	 */
	@Test
	void delegaLaTraduccionAlServicioExistenteYDevuelve201() {
		when(documentos.extraer(archivo, "es", "usuario"))
				.thenReturn(new DocumentoExtraidoResponseDTO("Hola.", 5, 500, false));
		when(traducciones.traducir(any(), eq("usuario"))).thenReturn(new SolicitudTraduccionResponseDTO());
		assertEquals(201, controller.traducir(archivo, "es", "en", authentication).getStatusCode().value());
	}

	/**
	 * Comprueba que un error HTTP 401 del proveedor externo se transforma en HTTP
	 * 502, sin presentarlo como un fallo de autenticación de la sesión del usuario
	 * de TraducIA.
	 */
	@Test
	void noConfundeClaveExternaInvalidaConSesionJwtInvalida() {
		assertEquals(502, controller.manejarProveedor(new ExternalAPIException(401)).getStatus());
	}
}
