package co.edu.unbosque.traducia.service.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.unbosque.traducia.dto.DeepLRequestDTO;
import co.edu.unbosque.traducia.dto.DeepLResponseDTO;
import co.edu.unbosque.traducia.dto.TraduccionRequestDTO;
import co.edu.unbosque.traducia.entity.AuthenticationType;
import co.edu.unbosque.traducia.entity.ProveedorTraduccion;
import co.edu.unbosque.traducia.service.ExternalHTTPRequestHandler;

/**
 * Pruebas unitarias para {@link DeeplTraductorStrategy} utilizando JUnit 5,
 * Mockito y AssertJ.
 *
 * <p>Valida la identificación del proveedor {@link ProveedorTraduccion#DEEPL},
 * la normalización de códigos de idioma (incluyendo {@code zh-CN} y
 * {@code zh-TW}), la construcción de la petición {@link DeepLRequestDTO} hacia
 * {@link ExternalHTTPRequestHandler} y la propagación de excepciones ante
 * fallos externos.</p>
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class DeeplTraductorStrategyTest {

	/**
	 * URL esperada del endpoint de traducción de la API gratuita de DeepL.
	 */
	private static final String URL_BASE = "https://api-free.deepl.com/v2/translate";

	/**
	 * Clave simulada para inyectar en el campo {@code apiKey} de la estrategia.
	 */
	private static final String API_KEY = "test-deepl-api-key";

	/**
	 * Instancia de la estrategia bajo prueba.
	 */
	private DeeplTraductorStrategy strategy;

	/**
	 * Inicializa la estrategia e inyecta el valor de {@code apiKey} mediante
	 * {@link ReflectionTestUtils}.
	 */
	@BeforeEach
	void setUp() {
		strategy = new DeeplTraductorStrategy();
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
	 * Crea un mock de {@link DeepLResponseDTO} configurado con el texto
	 * traducido en la primera posición de {@code translations}.
	 *
	 * @param textoTraducido texto simulado devuelto por DeepL.
	 * @return mock de {@link DeepLResponseDTO}.
	 */
	private DeepLResponseDTO crearRespuestaMock(String textoTraducido) {
		DeepLResponseDTO responseMock = mock(DeepLResponseDTO.class, RETURNS_DEEP_STUBS);
		when(responseMock.getTranslations().get(0).getText()).thenReturn(textoTraducido);
		return responseMock;
	}

	/**
	 * Verifica que {@link DeeplTraductorStrategy#getProveedor()} retorne
	 * {@link ProveedorTraduccion#DEEPL}.
	 */
	@Test
	@DisplayName("getProveedor debe retornar ProveedorTraduccion.DEEPL")
	void getProveedorDebeRetornarDeepl() {
		assertThat(strategy.getProveedor()).isEqualTo(ProveedorTraduccion.DEEPL);
	}

	/**
	 * Verifica que {@code traducir} convierta un código de idioma estándar a
	 * mayúsculas, envíe la petición correctamente a DeepL y retorne el texto
	 * traducido.
	 */
	@Test
	@DisplayName("traducir debe normalizar código estándar a mayúsculas y retornar traducción")
	void traducirConIdiomaEstandarDebeNormalizarYRetornarTexto() {
		TraduccionRequestDTO dto = crearSolicitud("es", "en", "Hola mundo");
		DeepLResponseDTO responseMock = crearRespuestaMock("Hello world");

		try (MockedStatic<ExternalHTTPRequestHandler> mockedHandler = mockStatic(ExternalHTTPRequestHandler.class)) {
			mockedHandler.when(() -> ExternalHTTPRequestHandler.doPostAndConvertToDTO(
					eq(URL_BASE),
					eq(API_KEY),
					eq(AuthenticationType.DEEPL),
					any(DeepLRequestDTO.class),
					eq(DeepLResponseDTO.class)))
					.thenReturn(responseMock);

			String resultado = strategy.traducir(dto);

			ArgumentCaptor<DeepLRequestDTO> captor = ArgumentCaptor.forClass(DeepLRequestDTO.class);
			mockedHandler.verify(() -> ExternalHTTPRequestHandler.doPostAndConvertToDTO(
					eq(URL_BASE),
					eq(API_KEY),
					eq(AuthenticationType.DEEPL),
					captor.capture(),
					eq(DeepLResponseDTO.class)));

			assertThat(resultado).isEqualTo("Hello world");
			assertThat(dto.getDestino()).isEqualTo("EN");
			assertThat(captor.getValue().getTarget_lang()).isEqualTo("EN");
			assertThat(captor.getValue().getText()).containsExactly("Hola mundo");
		}
	}

	/**
	 * Verifica que {@code traducir} convierta el código {@code zh-CN} a
	 * {@code ZH-HANS} antes de invocar la API de DeepL.
	 */
	@Test
	@DisplayName("traducir debe convertir zh-CN a ZH-HANS")
	void traducirConChinoSimplificadoDebeConvertirAZhHans() {
		TraduccionRequestDTO dto = crearSolicitud("es", "zh-CN", "Buenos días");
		DeepLResponseDTO responseMock = crearRespuestaMock("早上好");

		try (MockedStatic<ExternalHTTPRequestHandler> mockedHandler = mockStatic(ExternalHTTPRequestHandler.class)) {
			mockedHandler.when(() -> ExternalHTTPRequestHandler.doPostAndConvertToDTO(
					eq(URL_BASE),
					eq(API_KEY),
					eq(AuthenticationType.DEEPL),
					any(DeepLRequestDTO.class),
					eq(DeepLResponseDTO.class)))
					.thenReturn(responseMock);

			String resultado = strategy.traducir(dto);

			ArgumentCaptor<DeepLRequestDTO> captor = ArgumentCaptor.forClass(DeepLRequestDTO.class);
			mockedHandler.verify(() -> ExternalHTTPRequestHandler.doPostAndConvertToDTO(
					eq(URL_BASE),
					eq(API_KEY),
					eq(AuthenticationType.DEEPL),
					captor.capture(),
					eq(DeepLResponseDTO.class)));

			assertThat(resultado).isEqualTo("早上好");
			assertThat(dto.getDestino()).isEqualTo("ZH-HANS");
			assertThat(captor.getValue().getTarget_lang()).isEqualTo("ZH-HANS");
			assertThat(captor.getValue().getText()).containsExactly("Buenos días");
		}
	}

	/**
	 * Verifica que {@code traducir} convierta el código {@code zh-TW} a
	 * {@code ZH-HANT} antes de invocar la API de DeepL.
	 */
	@Test
	@DisplayName("traducir debe convertir zh-TW a ZH-HANT")
	void traducirConChinoTradicionalDebeConvertirAZhHant() {
		TraduccionRequestDTO dto = crearSolicitud("es", "zh-TW", "Buenas noches");
		DeepLResponseDTO responseMock = crearRespuestaMock("晚安");

		try (MockedStatic<ExternalHTTPRequestHandler> mockedHandler = mockStatic(ExternalHTTPRequestHandler.class)) {
			mockedHandler.when(() -> ExternalHTTPRequestHandler.doPostAndConvertToDTO(
					eq(URL_BASE),
					eq(API_KEY),
					eq(AuthenticationType.DEEPL),
					any(DeepLRequestDTO.class),
					eq(DeepLResponseDTO.class)))
					.thenReturn(responseMock);

			String resultado = strategy.traducir(dto);

			ArgumentCaptor<DeepLRequestDTO> captor = ArgumentCaptor.forClass(DeepLRequestDTO.class);
			mockedHandler.verify(() -> ExternalHTTPRequestHandler.doPostAndConvertToDTO(
					eq(URL_BASE),
					eq(API_KEY),
					eq(AuthenticationType.DEEPL),
					captor.capture(),
					eq(DeepLResponseDTO.class)));

			assertThat(resultado).isEqualTo("晚安");
			assertThat(dto.getDestino()).isEqualTo("ZH-HANT");
			assertThat(captor.getValue().getTarget_lang()).isEqualTo("ZH-HANT");
			assertThat(captor.getValue().getText()).containsExactly("Buenas noches");
		}
	}

	/**
	 * Verifica que {@code traducir} propague la excepción cuando falla la
	 * comunicación con el servicio externo de DeepL.
	 */
	@Test
	@DisplayName("traducir debe propagar excepción si falla la petición externa")
	void traducirDebePropagarExcepcionCuandoFallaPeticionExterna() {
		TraduccionRequestDTO dto = crearSolicitud("es", "fr", "Hola");

		try (MockedStatic<ExternalHTTPRequestHandler> mockedHandler = mockStatic(ExternalHTTPRequestHandler.class)) {
			mockedHandler.when(() -> ExternalHTTPRequestHandler.doPostAndConvertToDTO(
					eq(URL_BASE),
					eq(API_KEY),
					eq(AuthenticationType.DEEPL),
					any(DeepLRequestDTO.class),
					eq(DeepLResponseDTO.class)))
					.thenThrow(new RuntimeException("Error de conexión con DeepL"));

			assertThatThrownBy(() -> strategy.traducir(dto))
					.isInstanceOf(RuntimeException.class)
					.hasMessage("Error de conexión con DeepL");
		}
	}
}