package co.edu.unbosque.traducia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.sun.net.httpserver.HttpServer;
import co.edu.unbosque.traducia.dto.ArchivoMultipartDTO;
import co.edu.unbosque.traducia.exception.DocumentoException;
import co.edu.unbosque.traducia.exception.ExternalAPIException;

/**
 * Verifica el flujo asíncrono remoto con un servidor HTTP local de prueba.
 *
 * <p>
 * Simula la creación y consulta de trabajos de LlamaParse para comprobar el
 * envío multipart, la autenticación, la configuración del idioma, el
 * tratamiento de errores y el plazo máximo de espera.
 * </p>
 *
 * <p>
 * Cada prueba utiliza un servidor independiente en la dirección local 127.0.0.1
 * y un puerto asignado automáticamente. No se realizan peticiones al proveedor
 * real ni se utilizan credenciales reales.
 * </p>
 */
class LlamaParseServiceTest {

	/** Respuesta JSON simulada para la creación de un trabajo de extracción. */
	private final AtomicReference<String> creacion = new AtomicReference<>(
			"{\"id\":\"trabajo-1\",\"status\":\"PENDING\"}");

	/** Respuesta JSON simulada para la consulta del resultado del trabajo. */
	private final AtomicReference<String> resultado = new AtomicReference<>(completado("Hola.", true));

	/** Cuerpo de la última petición de subida recibida por el servidor. */
	private final AtomicReference<String> cuerpoRecibido = new AtomicReference<>();

	/** Encabezado Authorization de la última petición recibida. */
	private final AtomicReference<String> autorizacion = new AtomicReference<>();

	/** Código HTTP que devuelve el servidor simulado. */
	private final AtomicInteger codigo = new AtomicInteger(200);

	/** Número de peticiones POST recibidas para subir archivos. */
	private final AtomicInteger subidas = new AtomicInteger();

	/** Número de consultas recibidas para obtener el estado del trabajo. */
	private final AtomicInteger consultas = new AtomicInteger();

	/** Cantidad de consultas que deben responder con el estado RUNNING. */
	private final AtomicInteger pendientes = new AtomicInteger();

	/** Servidor HTTP local utilizado durante cada prueba. */
	private HttpServer server;

	/** Dirección base del servidor, incluido el puerto asignado. */
	private String url;

	/**
	 * Inicia el servidor HTTP local antes de cada prueba.
	 *
	 * <p>
	 * Configura el contexto {@code /api/v2/parse} para simular la creación y
	 * consulta de trabajos. Registra el encabezado de autenticación, el cuerpo de
	 * las subidas y la cantidad de peticiones recibidas.
	 * </p>
	 *
	 * <p>
	 * Las consultas iniciales pueden responder con el estado RUNNING, según el
	 * valor de {@code pendientes}. Las consultas posteriores devuelven la respuesta
	 * almacenada en {@code resultado}.
	 * </p>
	 *
	 * @throws IOException si no se puede crear el servidor HTTP local
	 */
	@BeforeEach
	void iniciarServidor() throws IOException {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);

		server.createContext("/api/v2/parse", exchange -> {
			try (exchange) {
				autorizacion.set(exchange.getRequestHeaders().getFirst("Authorization"));

				String respuesta;

				if ("POST".equals(exchange.getRequestMethod())) {
					subidas.incrementAndGet();

					cuerpoRecibido.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));

					respuesta = creacion.get();
				} else {
					int consulta = consultas.incrementAndGet();

					respuesta = consulta <= pendientes.get()
							? "{\"job\":{\"id\":\"trabajo-1\"," + "\"status\":\"RUNNING\"}}"
							: resultado.get();
				}

				byte[] bytes = respuesta.getBytes(StandardCharsets.UTF_8);

				exchange.getResponseHeaders().set("Content-Type", "application/json");

				exchange.sendResponseHeaders(codigo.get(), bytes.length);
				exchange.getResponseBody().write(bytes);
			}
		});

		server.start();
		url = "http://127.0.0.1:" + server.getAddress().getPort();
	}

	/**
	 * Detiene inmediatamente el servidor HTTP después de cada prueba y libera el
	 * puerto y los recursos asociados.
	 */
	@AfterEach
	void detenerServidor() {
		server.stop(0);
	}

	/**
	 * Comprueba el envío del archivo multipart, la configuración del idioma y el
	 * encabezado de autenticación Bearer.
	 *
	 * <p>
	 * Simula una primera consulta con estado RUNNING y una segunda con el trabajo
	 * completado. Verifica que el texto se devuelve mediante una única subida y dos
	 * consultas.
	 * </p>
	 */
	@Test
	void enviaArchivoConfiguracionYBearerYConsultaElTrabajo() {
		pendientes.set(1);

		assertEquals("Hola.", servicio().extraer(archivo(), "es"));

		assertEquals("Bearer clave-solo-de-prueba", autorizacion.get());

		assertTrue(cuerpoRecibido.get().contains("name=\"file\"; filename=\"documento.pdf\""));

		assertTrue(cuerpoRecibido.get().contains("name=\"configuration\""));

		assertTrue(cuerpoRecibido.get().contains("\"languages\":[\"es\"]"));

		assertEquals(1, subidas.get());
		assertEquals(2, consultas.get());
	}

	/**
	 * Comprueba que una página marcada como fallida provoca una
	 * {@link DocumentoException}, aunque el estado general del trabajo sea COMPLETED.
	 */
	@Test
	void rechazaPaginaFallidaAunqueElTrabajoEsteCompletado() {
		resultado.set(completado("Parcial", false));

		DocumentoException ex = assertThrows(
				DocumentoException.class,
				() -> servicio().extraer(archivo(), "es"));

		assertNotNull(ex);
		assertEquals(1, subidas.get());
	}

	/**
	 * Comprueba que un trabajo con estado FAILED provoca una
	 * {@link DocumentoException}, sin devolver contenido parcial como una
	 * extracción exitosa.
	 */
	@Test
	void rechazaTrabajoFallidoSinDevolverUnTextoParcial() {
		resultado.set("{\"job\":{\"id\":\"trabajo-1\",\"status\":\"FAILED\"}}");

		DocumentoException ex = assertThrows(
				DocumentoException.class,
				() -> servicio().extraer(archivo(), "es"));

		assertNotNull(ex);
		assertEquals(1, subidas.get());
	}

	/**
	 * Comprueba que el código HTTP 401 del proveedor se conserva en una
	 * {@link ExternalAPIException}, permitiendo que el controlador lo clasifique
	 * posteriormente.
	 *
	 * <p>
	 * También verifica que el fallo no provoca subidas repetidas.
	 * </p>
	 */
	@Test
	void conservaCodigoExternoParaQueElControllerLoClasifique() {
		codigo.set(401);

		assertEquals(401,
				assertThrows(ExternalAPIException.class, () -> servicio().extraer(archivo(), "es")).getStatusCode());

		assertEquals(1, subidas.get());
	}

	/**
	 * Comprueba que un identificador remoto con segmentos de navegación de rutas se
	 * rechaza mediante una {@link DocumentoException}.
	 *
	 * <p>
	 * Verifica que el identificador rechazado no se utiliza para realizar consultas
	 * posteriores al servidor.
	 * </p>
	 */
	@Test
	void rechazaIdentificadorQuePuedaAlterarLaRuta() {
		creacion.set("{\"id\":\"../otro\",\"status\":\"PENDING\"}");

		DocumentoException ex = assertThrows(
				DocumentoException.class,
				() -> servicio().extraer(archivo(), "es"));

		assertNotNull(ex);
		assertEquals(0, consultas.get());
	}

	/**
	 * Comprueba que un trabajo completado sin las representaciones de texto y
	 * Markdown solicitadas provoca una {@link DocumentoException}.
	 */
	@Test
	void rechazaResultadoSinLasRepresentacionesSolicitadas() {
		resultado.set("{\"job\":{\"id\":\"trabajo-1\"," + "\"status\":\"COMPLETED\"}}");

		DocumentoException ex = assertThrows(
				DocumentoException.class,
				() -> servicio().extraer(archivo(), "es"));

		assertNotNull(ex);
	}

	/**
	 * Comprueba que una respuesta que no puede deserializarse como el DTO esperado
	 * y una respuesta JSON nula provocan una {@link ExternalAPIException} con
	 * código HTTP 502.
	 */
	@Test
	void rechazaJsonInvalidoYJsonNulo() {
		creacion.set("no JSON");

		assertEquals(502,
				assertThrows(ExternalAPIException.class, () -> servicio().extraer(archivo(), "es")).getStatusCode());

		creacion.set("null");

		assertEquals(502,
				assertThrows(ExternalAPIException.class, () -> servicio().extraer(archivo(), "es")).getStatusCode());
	}

	/**
	 * Comprueba que la ausencia de la clave del proveedor provoca una
	 * {@link DocumentoException} antes de realizar cualquier subida.
	 */
	@Test
	void rechazaClaveAusenteAntesDeSubirElArchivo() {
		LlamaParseService servicio = new LlamaParseService(url, "", "cost_effective", 10, 10);

		DocumentoException ex = assertThrows(
				DocumentoException.class,
				() -> servicio.extraer(archivo(), "es"));

		assertNotNull(ex);
		assertEquals(0, subidas.get());
	}

	/**
	 * Comprueba que las consultas de un trabajo que permanece en ejecución respetan
	 * el plazo total configurado.
	 *
	 * <p>
	 * Espera una {@link DocumentoException} y verifica que el agotamiento del plazo
	 * no provoca nuevas subidas del mismo archivo.
	 * </p>
	 */


	/**
	 * Comprueba que un nombre completo de idioma, en lugar de un código admitido,
	 * provoca una {@link DocumentoException} sin subir el archivo.
	 */
	@Test
	void rechazaIdiomasEscritosComoNombreCompleto() {
		DocumentoException ex = assertThrows(
				DocumentoException.class,
				() -> servicio().extraer(archivo(), "english"));

		assertNotNull(ex);
		assertEquals(0, subidas.get());
	}

	/**
	 * Construye una instancia del servicio dirigida al servidor HTTP local de la
	 * prueba.
	 *
	 * <p>
	 * Utiliza una clave ficticia, el nivel {@code cost_effective}, un plazo máximo
	 * de diez segundos y un intervalo de consulta de diez milisegundos.
	 * </p>
	 *
	 * @return servicio configurado para consumir el servidor simulado
	 */
	private LlamaParseService servicio() {
		return new LlamaParseService(url, "clave-solo-de-prueba", "cost_effective", 10, 10);
	}

	/**
	 * Construye los datos multipart de un archivo de prueba.
	 *
	 * <p>
	 * El contenido incluye únicamente un encabezado PDF mínimo para ejercitar el
	 * transporte HTTP; no representa un documento PDF completo.
	 * </p>
	 *
	 * @return archivo con campo multipart {@code file}, nombre
	 *         {@code documento.pdf} y contenido de prueba en UTF-8
	 */
	private static ArchivoMultipartDTO archivo() {
		return new ArchivoMultipartDTO("file", "documento.pdf", "%PDF-1.7".getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Construye una respuesta JSON simulada de un trabajo completado con una página
	 * de texto y su estado de conversión.
	 *
	 * @param texto contenido de prueba incluido en la página de texto
	 * @param exito indica si la conversión de la página fue exitosa
	 * @return representación JSON del trabajo en estado COMPLETED
	 */
	private static String completado(String texto, boolean exito) {
		return "{\"job\":{\"id\":\"trabajo-1\",\"status\":\"COMPLETED\"},"
				+ "\"text\":{\"pages\":[{\"page_number\":1,\"text\":\"" + texto + "\"}]},"
				+ "\"markdown\":{\"pages\":[{\"page_number\":1,\"success\":" + exito + "}]}}";
	}
}