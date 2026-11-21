package co.edu.unbosque.traducia.service.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.unbosque.traducia.dto.NvidiaMessageRequestDTO;
import co.edu.unbosque.traducia.dto.NvidiaRequestDTO;
import co.edu.unbosque.traducia.dto.NvidiaResponseDTO;
import co.edu.unbosque.traducia.dto.TraduccionRequestDTO;
import co.edu.unbosque.traducia.entity.AuthenticationType;
import co.edu.unbosque.traducia.entity.ProveedorTraduccion;
import co.edu.unbosque.traducia.service.ExternalHTTPRequestHandler;

/**
 * Pruebas unitarias para {@link GemmaTraductorStrategy} utilizando JUnit 5,
 * Mockito y AssertJ.
 *
 * <p>Valida la identificación del proveedor {@link ProveedorTraduccion#GEMMA},
 * la construcción del cuerpo {@link NvidiaRequestDTO} (modelo, límite de
 * tokens, modo sin streaming, mensaje {@code system} con prompt de traducción
 * y mensaje {@code user}), la invocación a {@link ExternalHTTPRequestHandler}
 * con autenticación {@link AuthenticationType#BEARER} y la propagación de
 * errores de comunicación o respuesta vacía.</p>
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class GemmaTraductorStrategyTest {

	/**
	 * URL esperada del endpoint de chat completions de NVIDIA.
	 */
	private static final String URL_BASE = "https://integrate.api.nvidia.com/v1/chat/completions";

	/**
	 * Clave simulada para inyectar en el campo {@code apiKey} de la estrategia.
	 */
	private static final String API_KEY = "test-nvidia-api-key";

	/**
	 * Instancia de la estrategia bajo prueba.
	 */
	private GemmaTraductorStrategy strategy;

	/**
	 * Inicializa la estrategia e inyecta el valor de {@code apiKey} mediante
	 * {@link ReflectionTestUtils}.
	 */
	@BeforeEach
	void setUp() {
		strategy = new GemmaTraductorStrategy();
		ReflectionTestUtils.setField(strategy, "apiKey", API_KEY);
	}

	/**
	 * Crea un {@link TraduccionRequestDTO} con los datos indicados para las
	 * pruebas.
	 *
	 * @param origen  código del idioma de origen.
	 * @param destino código del idioma de destino.
	 * @param texto   texto a traducir.
	 * @return instancia configurada de {@link TraduccionRequestDTO}.
	 */
	private TraduccionRequestDTO crearSolicitud(String origen, String destino, String texto) {
		TraduccionRequestDTO dto = new TraduccionRequestDTO();
		dto.setOrigen(origen);
		dto.setDestino(destino);
		dto.setTexto(texto);
		return dto;
	}

	/**
	 * Verifica que {@link GemmaTraductorStrategy#getProveedor()} retorne
	 * {@link ProveedorTraduccion#GEMMA}.
	 */
	@Test
	@DisplayName("getProveedor debe retornar ProveedorTraduccion.GEMMA")
	void getProveedorDebeRetornarGemma() {
		assertThat(strategy.getProveedor()).isEqualTo(ProveedorTraduccion.GEMMA);
	}

	/**
	 * Verifica que {@code traducir} construya correctamente el objeto
	 * {@link NvidiaRequestDTO} con el modelo Gemma 4 31B, el prompt del sistema
	 * y el texto del usuario, envíe la petición con Bearer token y retorne el
	 * contenido devuelto por {@link NvidiaResponseDTO#obtenerContenido()}.
	 */
	@Test
	@DisplayName("traducir debe construir la petición para NVIDIA y retornar el texto traducido")
	void traducirDebeConstruirPeticionNvidiaYRetornarContenido() {
		TraduccionRequestDTO dto = crearSolicitud("es", "en", "Hola mundo");
		NvidiaResponseDTO responseMock = mock(NvidiaResponseDTO.class);
		when(responseMock.obtenerContenido()).thenReturn("Hello world");
		List<NvidiaRequestDTO> solicitudesEnviadas = new ArrayList<>();

		try (MockedStatic<ExternalHTTPRequestHandler> mockedHandler = mockStatic(ExternalHTTPRequestHandler.class)) {
			mockedHandler.when(() -> ExternalHTTPRequestHandler.doPostAndConvertToDTO(
					eq(URL_BASE),
					eq(API_KEY),
					eq(AuthenticationType.BEARER),
					any(NvidiaRequestDTO.class),
					eq(NvidiaResponseDTO.class)))
					.thenAnswer(invocation -> {
						solicitudesEnviadas.add(invocation.getArgument(3, NvidiaRequestDTO.class));
						return responseMock;
					});

			String resultado = strategy.traducir(dto);

			assertThat(resultado).isEqualTo("Hello world");
			assertThat(solicitudesEnviadas).hasSize(1);

			NvidiaRequestDTO solicitudEnviada = solicitudesEnviadas.get(0);
			assertThat(solicitudEnviada.getModel()).isEqualTo("google/gemma-4-31b-it");
			assertThat(solicitudEnviada.getMax_tokens()).isEqualTo(4096);
			assertThat(solicitudEnviada.isStream()).isFalse();
			assertThat(solicitudEnviada.getMessages()).hasSize(2);

			NvidiaMessageRequestDTO mensajeSistema = solicitudEnviada.getMessages().get(0);
			NvidiaMessageRequestDTO mensajeUsuario = solicitudEnviada.getMessages().get(1);

			assertThat(mensajeSistema.getRole()).isEqualTo("system");
			assertThat(mensajeSistema.getContent())
					.contains("Source language: es")
					.contains("Target language: en")
					.contains("Return nothing but the final translation.");

			assertThat(mensajeUsuario.getRole()).isEqualTo("user");
			assertThat(mensajeUsuario.getContent()).isEqualTo("Hola mundo");
		}
	}

	/**
	 * Verifica que {@code traducir} incluya {@code "auto"} como idioma de origen
	 * en el prompt de sistema cuando se solicita detección automática.
	 */
	@Test
	@DisplayName("traducir debe incluir el origen auto en el prompt del sistema")
	void traducirConOrigenAutoDebeIncluirAutoEnPrompt() {
		TraduccionRequestDTO dto = crearSolicitud("auto", "fr", "Buenos días");
		NvidiaResponseDTO responseMock = mock(NvidiaResponseDTO.class);
		when(responseMock.obtenerContenido()).thenReturn("Bonjour");
		List<NvidiaRequestDTO> solicitudesEnviadas = new ArrayList<>();

		try (MockedStatic<ExternalHTTPRequestHandler> mockedHandler = mockStatic(ExternalHTTPRequestHandler.class)) {
			mockedHandler.when(() -> ExternalHTTPRequestHandler.doPostAndConvertToDTO(
					eq(URL_BASE),
					eq(API_KEY),
					eq(AuthenticationType.BEARER),
					any(NvidiaRequestDTO.class),
					eq(NvidiaResponseDTO.class)))
					.thenAnswer(invocation -> {
						solicitudesEnviadas.add(invocation.getArgument(3, NvidiaRequestDTO.class));
						return responseMock;
					});

			String resultado = strategy.traducir(dto);

			assertThat(resultado).isEqualTo("Bonjour");
			assertThat(solicitudesEnviadas).hasSize(1);
			assertThat(solicitudesEnviadas.get(0).getMessages().get(0).getContent())
					.contains("Source language: auto")
					.contains("Target language: fr");
		}
	}

	/**
	 * Verifica que {@code traducir} propague la excepción cuando falla la
	 * petición HTTP hacia la API de NVIDIA.
	 */
	@Test
	@DisplayName("traducir debe propagar excepción si falla la petición externa")
	void traducirDebePropagarExcepcionCuandoFallaPeticionExterna() {
		TraduccionRequestDTO dto = crearSolicitud("es", "en", "Hola");

		try (MockedStatic<ExternalHTTPRequestHandler> mockedHandler = mockStatic(ExternalHTTPRequestHandler.class)) {
			mockedHandler.when(() -> ExternalHTTPRequestHandler.doPostAndConvertToDTO(
					eq(URL_BASE),
					eq(API_KEY),
					eq(AuthenticationType.BEARER),
					any(NvidiaRequestDTO.class),
					eq(NvidiaResponseDTO.class)))
					.thenThrow(new RuntimeException("Error de conexión con NVIDIA"));

			assertThatThrownBy(() -> strategy.traducir(dto))
					.isInstanceOf(RuntimeException.class)
					.hasMessage("Error de conexión con NVIDIA");
		}
	}

	/**
	 * Verifica que {@code traducir} propague {@link IndexOutOfBoundsException}
	 * cuando la respuesta de NVIDIA llega sin elementos en {@code choices}.
	 */
	@Test
	@DisplayName("traducir debe propagar IndexOutOfBoundsException cuando la respuesta no tiene choices")
	void traducirDebePropagarExcepcionCuandoRespuestaNoTieneChoices() {
		TraduccionRequestDTO dto = crearSolicitud("es", "en", "Hola");
		NvidiaResponseDTO responseMock = mock(NvidiaResponseDTO.class);
		when(responseMock.obtenerContenido()).thenThrow(new IndexOutOfBoundsException("Sin opciones de respuesta"));

		try (MockedStatic<ExternalHTTPRequestHandler> mockedHandler = mockStatic(ExternalHTTPRequestHandler.class)) {
			mockedHandler.when(() -> ExternalHTTPRequestHandler.doPostAndConvertToDTO(
					eq(URL_BASE),
					eq(API_KEY),
					eq(AuthenticationType.BEARER),
					any(NvidiaRequestDTO.class),
					eq(NvidiaResponseDTO.class)))
					.thenReturn(responseMock);

			assertThatThrownBy(() -> strategy.traducir(dto))
					.isInstanceOf(IndexOutOfBoundsException.class)
					.hasMessage("Sin opciones de respuesta");
		}
	}
}