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
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.unbosque.traducia.dto.NvidiaRequestDTO;
import co.edu.unbosque.traducia.dto.NvidiaResponseDTO;
import co.edu.unbosque.traducia.dto.TraduccionRequestDTO;
import co.edu.unbosque.traducia.entity.AuthenticationType;
import co.edu.unbosque.traducia.entity.ProveedorTraduccion;
import co.edu.unbosque.traducia.service.ExternalHTTPRequestHandler;

/**
 * Pruebas unitarias para {@link RivaTraductorStrategy} utilizando JUnit 5,
 * Mockito y AssertJ.
 *
 * <p>Valida la identificación del proveedor {@link ProveedorTraduccion#RIVA},
 * el flujo de traducción en dos pasos utilizando inglés como idioma intermedio,
 * la normalización de códigos de idioma (minúsculas y conversión de {@code no}
 * a {@code nb}), la reutilización del cuerpo {@link NvidiaRequestDTO} y la
 * propagación de errores en cualquiera de las dos llamadas externas.</p>
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class RivaTraductorStrategyTest {

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
	private RivaTraductorStrategy strategy;

	/**
	 * Inicializa la estrategia e inyecta el valor de {@code apiKey} mediante
	 * {@link ReflectionTestUtils}.
	 */
	@BeforeEach
	void setUp() {
		strategy = new RivaTraductorStrategy();
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
	 * Verifica que {@link RivaTraductorStrategy#getProveedor()} retorne
	 * {@link ProveedorTraduccion#RIVA}.
	 */
	@Test
	@DisplayName("getProveedor debe retornar ProveedorTraduccion.RIVA")
	void getProveedorDebeRetornarRiva() {
		assertThat(strategy.getProveedor()).isEqualTo(ProveedorTraduccion.RIVA);
	}

	/**
	 * Verifica que {@code traducir} ejecute las dos peticiones consecutivas a
	 * NVIDIA (origen a inglés e inglés a destino), normalizando los códigos a
	 * minúsculas, configurando el modelo Riva sin streaming y encadenando la
	 * salida del primer paso como entrada del segundo.
	 */
	@Test
	@DisplayName("traducir debe ejecutar dos pasos con inglés intermedio y retornar el resultado final")
	void traducirDebeEjecutarDosPasosConInglesIntermedioYRetornarResultado() {
		TraduccionRequestDTO dto = crearSolicitud("ES", "FR", "Hola mundo");

		NvidiaResponseDTO respuestaPaso1 = mock(NvidiaResponseDTO.class);
		when(respuestaPaso1.obtenerContenido()).thenReturn("Hello world");

		NvidiaResponseDTO respuestaPaso2 = mock(NvidiaResponseDTO.class);
		when(respuestaPaso2.obtenerContenido()).thenReturn("Bonjour le monde");

		List<String> mensajesSistema = new ArrayList<>();
		List<String> mensajesUsuario = new ArrayList<>();

		try (MockedStatic<ExternalHTTPRequestHandler> mockedHandler = mockStatic(ExternalHTTPRequestHandler.class)) {
			mockedHandler.when(() -> ExternalHTTPRequestHandler.doPostAndConvertToDTO(
					eq(URL_BASE),
					eq(API_KEY),
					eq(AuthenticationType.BEARER),
					any(NvidiaRequestDTO.class),
					eq(NvidiaResponseDTO.class)))
					.thenAnswer(invocation -> {
						NvidiaRequestDTO solicitud = invocation.getArgument(3, NvidiaRequestDTO.class);
						assertThat(solicitud.getModel()).isEqualTo("nvidia/riva-translate-4b-instruct-v2");
						assertThat(solicitud.getMax_tokens()).isEqualTo(4096);
						assertThat(solicitud.isStream()).isFalse();
						assertThat(solicitud.getMessages()).hasSize(2);
						assertThat(solicitud.getMessages().get(0).getRole()).isEqualTo("system");
						assertThat(solicitud.getMessages().get(1).getRole()).isEqualTo("user");

						mensajesSistema.add(solicitud.getMessages().get(0).getContent());
						mensajesUsuario.add(solicitud.getMessages().get(1).getContent());

						return mensajesSistema.size() == 1 ? respuestaPaso1 : respuestaPaso2;
					});

			String resultado = strategy.traducir(dto);

			assertThat(resultado).isEqualTo("Bonjour le monde");
			assertThat(mensajesSistema).containsExactly("es-en", "en-fr");
			assertThat(mensajesUsuario).containsExactly("Hola mundo", "Hello world");
		}
	}

	/**
	 * Verifica que {@code traducir} transforme el código {@code no} (noruego)
	 * en {@code nb} tanto cuando aparece como idioma de origen como cuando se
	 * especifica como idioma de destino, y normalice códigos compuestos a
	 * minúsculas.
	 */
	@Test
	@DisplayName("traducir debe convertir el código no a nb en origen y destino")
	void traducirDebeConvertirNoruegoNoANbEnOrigenYDestino() {
		TraduccionRequestDTO dtoOrigenNoruego = crearSolicitud("no", "PT-BR", "Hei");
		TraduccionRequestDTO dtoDestinoNoruego = crearSolicitud("ES", "no", "Hola");

		NvidiaResponseDTO respIntermedia = mock(NvidiaResponseDTO.class);
		when(respIntermedia.obtenerContenido()).thenReturn("Hi");

		NvidiaResponseDTO respFinalPortugues = mock(NvidiaResponseDTO.class);
		when(respFinalPortugues.obtenerContenido()).thenReturn("Oi");

		NvidiaResponseDTO respFinalNoruego = mock(NvidiaResponseDTO.class);
		when(respFinalNoruego.obtenerContenido()).thenReturn("Hei");

		List<String> paresIdiomas = new ArrayList<>();

		try (MockedStatic<ExternalHTTPRequestHandler> mockedHandler = mockStatic(ExternalHTTPRequestHandler.class)) {
			mockedHandler.when(() -> ExternalHTTPRequestHandler.doPostAndConvertToDTO(
					eq(URL_BASE),
					eq(API_KEY),
					eq(AuthenticationType.BEARER),
					any(NvidiaRequestDTO.class),
					eq(NvidiaResponseDTO.class)))
					.thenAnswer(invocation -> {
						NvidiaRequestDTO solicitud = invocation.getArgument(3, NvidiaRequestDTO.class);
						paresIdiomas.add(solicitud.getMessages().get(0).getContent());
						int llamada = paresIdiomas.size();
						if (llamada == 1 || llamada == 3) {
							return respIntermedia;
						}
						return llamada == 2 ? respFinalPortugues : respFinalNoruego;
					});

			String resultadoPortugues = strategy.traducir(dtoOrigenNoruego);
			String resultadoNoruego = strategy.traducir(dtoDestinoNoruego);

			assertThat(resultadoPortugues).isEqualTo("Oi");
			assertThat(resultadoNoruego).isEqualTo("Hei");
			assertThat(paresIdiomas).containsExactly("nb-en", "en-pt-br", "es-en", "en-nb");
		}
	}

	/**
	 * Verifica que {@code traducir} propague la excepción si falla la primera
	 * petición externa y no intente ejecutar la segunda llamada.
	 */
	@Test
	@DisplayName("traducir debe propagar excepción en el primer paso sin ejecutar el segundo")
	void traducirDebePropagarExcepcionEnPrimeraLlamadaSinEjecutarSegunda() {
		TraduccionRequestDTO dto = crearSolicitud("es", "de", "Hola");
		AtomicInteger llamadas = new AtomicInteger(0);

		try (MockedStatic<ExternalHTTPRequestHandler> mockedHandler = mockStatic(ExternalHTTPRequestHandler.class)) {
			mockedHandler.when(() -> ExternalHTTPRequestHandler.doPostAndConvertToDTO(
					eq(URL_BASE),
					eq(API_KEY),
					eq(AuthenticationType.BEARER),
					any(NvidiaRequestDTO.class),
					eq(NvidiaResponseDTO.class)))
					.thenAnswer(invocation -> {
						llamadas.incrementAndGet();
						throw new RuntimeException("Error en primer paso de Riva");
					});

			assertThatThrownBy(() -> strategy.traducir(dto))
					.isInstanceOf(RuntimeException.class)
					.hasMessage("Error en primer paso de Riva");

			assertThat(llamadas.get()).isEqualTo(1);
		}
	}

	/**
	 * Verifica que {@code traducir} propague {@link IndexOutOfBoundsException}
	 * cuando la segunda respuesta de NVIDIA no contiene elementos en
	 * {@code choices}.
	 */
	@Test
	@DisplayName("traducir debe propagar IndexOutOfBoundsException si falla la segunda respuesta")
	void traducirDebePropagarExcepcionEnSegundaLlamada() {
		TraduccionRequestDTO dto = crearSolicitud("es", "it", "Hola");
		AtomicInteger llamadas = new AtomicInteger(0);

		NvidiaResponseDTO respuestaPaso1 = mock(NvidiaResponseDTO.class);
		when(respuestaPaso1.obtenerContenido()).thenReturn("Hello");

		NvidiaResponseDTO respuestaPaso2 = mock(NvidiaResponseDTO.class);
		when(respuestaPaso2.obtenerContenido()).thenThrow(new IndexOutOfBoundsException("Sin choices en paso 2"));

		try (MockedStatic<ExternalHTTPRequestHandler> mockedHandler = mockStatic(ExternalHTTPRequestHandler.class)) {
			mockedHandler.when(() -> ExternalHTTPRequestHandler.doPostAndConvertToDTO(
					eq(URL_BASE),
					eq(API_KEY),
					eq(AuthenticationType.BEARER),
					any(NvidiaRequestDTO.class),
					eq(NvidiaResponseDTO.class)))
					.thenAnswer(invocation -> llamadas.incrementAndGet() == 1 ? respuestaPaso1 : respuestaPaso2);

			assertThatThrownBy(() -> strategy.traducir(dto))
					.isInstanceOf(IndexOutOfBoundsException.class)
					.hasMessage("Sin choices en paso 2");

			assertThat(llamadas.get()).isEqualTo(2);
		}
	}
}