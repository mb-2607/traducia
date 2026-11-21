package co.edu.unbosque.traducia.service;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import co.edu.unbosque.traducia.dto.ArchivoMultipartDTO;
import co.edu.unbosque.traducia.dto.LlamaParseJobDTO;
import co.edu.unbosque.traducia.dto.LlamaParseResultDTO;
import co.edu.unbosque.traducia.exception.DocumentoException;

/**
 * Adaptador HTTPS para LlamaParse alojado en LlamaCloud, API v2. Envía el
 * archivo, consulta el trabajo con espera acotada y verifica cada página. Todas
 * las peticiones se ejecutan en ExternalHTTPRequestHandler.
 *
 * La concurrencia se limita a dos extracciones por instancia del backend. No
 * reintenta subidas: un reintento podría crear trabajos y cargos duplicados.
 */
@Service
public class LlamaParseService {

	private static final int MAX_CARACTERES = 100_000;
	private static final Set<String> TIERS = Set.of("cost_effective", "agentic", "agentic_plus");
	private static final Set<String> EN_ESPERA = Set.of("PENDING", "RUNNING");
	private final Semaphore disponibles = new Semaphore(2);
	private final String baseUrl;
	private final String apiKey;
	private final String tier;
	private final int maxEsperaSegundos;
	private final long intervaloMs;

	/**
	 * Configura el servicio alojado. HTTP se admite solo para pruebas locales.
	 *
	 * @param baseUrl           raíz HTTPS del proveedor, sin /api/v2
	 * @param apiKey            clave de LlamaCloud; nunca es el JWT de TraducIA
	 * @param tier              nivel de procesamiento con OCR
	 * @param maxEsperaSegundos plazo total de la extracción, de 1 a 300 segundos
	 * @param intervaloMs       intervalo entre consultas, de 10 a 10000
	 *                          milisegundos
	 */
	public LlamaParseService(@Value("${llamaparse.base-url:https://api.cloud.llamaindex.ai}") String baseUrl,
			@Value("${llamaparse.api.key:}") String apiKey, @Value("${llamaparse.tier:cost_effective}") String tier,
			@Value("${llamaparse.max-espera-segundos:180}") int maxEsperaSegundos,
			@Value("${llamaparse.intervalo-consulta-ms:2000}") long intervaloMs) {
		this.baseUrl = validarBaseUrl(baseUrl);
		if (!TIERS.contains(tier) || maxEsperaSegundos < 1 || maxEsperaSegundos > 300 || intervaloMs < 10
				|| intervaloMs > 10_000) {
			throw new IllegalArgumentException("Revisa tier y tiempos de LlamaParse");
		}
		this.apiKey = apiKey.strip();
		this.tier = tier;
		this.maxEsperaSegundos = maxEsperaSegundos;
		this.intervaloMs = intervaloMs;
	}

	/**
	 * Solicita OCR/extracción completa y espera a que termine el trabajo.
	 *
	 * @param archivo   archivo validado; usa el campo multipart file
	 * @param idiomaOcr idioma escrito en el documento, por ejemplo es
	 * @return texto plano completo, sin recortarlo al límite del plan
	 */
	public String extraer(ArchivoMultipartDTO archivo, String idiomaOcr) {
		if (apiKey.isEmpty()) {
			throw new DocumentoException(503, "Configura llamaparse.api.key en el backend.");
		}
		String idioma = idiomaOcr == null ? "es" : idiomaOcr.strip().toLowerCase(Locale.ROOT);
		if (!idioma.matches("[a-z]{2,3}")) {
			throw new DocumentoException(400, "Usa un código de idioma OCR como es, en, fr o pt.");
		}
		if (!disponibles.tryAcquire()) {
			throw new DocumentoException(503, "Ya hay extracciones en curso. Inténtalo dentro de unos segundos.");
		}
		try {
			long fin = System.nanoTime() + Duration.ofSeconds(maxEsperaSegundos).toNanos();
			Map<String, String> encabezados = Map.of("Authorization", "Bearer " + apiKey);
			LlamaParseJobDTO trabajo = ExternalHTTPRequestHandler.doPostMultipartWithHeadersAndConvertToDTO(
					baseUrl + "/api/v2/parse/upload", encabezados, Map.of("configuration", crearConfiguracion(idioma)),
					archivo, tiempoRestante(fin, 60), LlamaParseJobDTO.class);
			validarId(trabajo.id());
			return consultarHastaCompletar(trabajo.id(), encabezados, fin);
		} finally {
			disponibles.release();
		}
	}

	/** Genera la configuración JSON como un campo de texto multipart. */
	private String crearConfiguracion(String idioma) {
		Map<String, Object> control = Map.of("timeouts",
				Map.of("base_in_seconds", 120, "extra_time_per_page_in_seconds", 1), "job_failure_conditions",
				Map.of("allowed_page_failure_ratio", 0.01));
		return new Gson().toJson(Map.of("tier", tier, "version", "latest", "processing_options",
				Map.of("ocr_parameters", Map.of("languages", List.of(idioma))), "processing_control", control));
	}

	/**
	 * Consulta un trabajo sin volver a enviar el archivo ni crear trabajos nuevos.
	 */
	private String consultarHastaCompletar(String id, Map<String, String> encabezados, long fin) {
		while (true) {
			LlamaParseResultDTO resultado = ExternalHTTPRequestHandler.doGetWithHeadersAndConvertToDTO(
					baseUrl + "/api/v2/parse/" + id + "?expand=text,markdown", encabezados, tiempoRestante(fin, 30),
					LlamaParseResultDTO.class);
			if (resultado.job() == null || resultado.job().status() == null || !id.equals(resultado.job().id())) {
				throw new DocumentoException(502, "LlamaParse devolvió un trabajo sin identificador o estado válido.");
			}
			String estado = resultado.job().status();
			if ("COMPLETED".equals(estado)) {
				return reunirPaginas(resultado);
			}
			if ("FAILED".equals(estado) || "CANCELLED".equals(estado)) {
				throw new DocumentoException(422,
						"LlamaParse no pudo procesar el documento completo. Revisa formato, idioma y legibilidad.");
			}
			if (!EN_ESPERA.contains(estado)) {
				throw new DocumentoException(502, "LlamaParse devolvió un estado de trabajo desconocido.");
			}
			pausarConsulta(fin);
		}
	}

	/**
	 * Verifica que ninguna página haya fallado y conserva el orden y todo el texto.
	 * El límite técnico se comprueba antes de crear una respuesta demasiado grande.
	 */
	private static String reunirPaginas(LlamaParseResultDTO resultado) {
		if (resultado.text() == null || resultado.text().pages() == null || resultado.markdown() == null
				|| resultado.markdown().pages() == null) {
			throw new DocumentoException(502, "LlamaParse no devolvió el texto y los estados de página solicitados.");
		}
		List<LlamaParseResultDTO.PaginaTexto> textos = resultado.text().pages();
		List<LlamaParseResultDTO.PaginaEstado> estados = resultado.markdown().pages();
		if (textos.size() != estados.size() || textos.isEmpty()) {
			throw new DocumentoException(422, "La extracción no contiene un conjunto completo de páginas.");
		}
		StringBuilder texto = new StringBuilder();
		for (int i = 0; i < textos.size(); i++) {
			LlamaParseResultDTO.PaginaTexto pagina = textos.get(i);
			LlamaParseResultDTO.PaginaEstado estado = estados.get(i);
			validarPagina(pagina, estado, i + 1);
			long longitud = (long) texto.length() + pagina.text().length() + (i == 0 ? 0 : 2);
			if (longitud > MAX_CARACTERES) {
				throw new DocumentoException(422,
						"El documento supera los 100.000 caracteres de extracción. Divide el archivo antes de subirlo.");
			}
			if (i > 0) {
				texto.append("\n\n");
			}
			texto.append(pagina.text());
		}
		return texto.toString();
	}

	/**
	 * Rechaza páginas omitidas, desordenadas, fallidas o sin texto de respuesta.
	 */
	private static void validarPagina(LlamaParseResultDTO.PaginaTexto texto, LlamaParseResultDTO.PaginaEstado estado,
			int numero) {
		if (texto == null || estado == null || texto.numero() != numero || estado.numero() != numero
				|| texto.text() == null || !Boolean.TRUE.equals(estado.success())) {
			throw new DocumentoException(422, "Una página no pudo procesarse. No se devolverá una extracción parcial.");
		}
	}

	/** Espera acotada, con propagación de la interrupción del hilo. */
	private void pausarConsulta(long fin) {
		long restanteMs = tiempoRestante(fin, 30).toMillis();
		try {
			Thread.sleep(Math.min(intervaloMs, Math.max(1L, restanteMs)));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DocumentoException(504, "La extracción fue interrumpida.", e);
		}
	}

	/** Evita peticiones que prolonguen indefinidamente la espera del usuario. */
	private static Duration tiempoRestante(long fin, int maxSegundos) {
		long restante = fin - System.nanoTime();
		if (restante <= 0) {
			throw new DocumentoException(504, "Se agotó el tiempo de extracción. Prueba con un archivo más pequeño.");
		}
		return Duration.ofNanos(Math.min(restante, Duration.ofSeconds(maxSegundos).toNanos()));
	}

	/**
	 * El identificador externo nunca puede introducir rutas ni parámetros
	 * adicionales.
	 */
	private static void validarId(String id) {
		if (id == null || !id.matches("[A-Za-z0-9_-]{1,100}")) {
			throw new DocumentoException(502, "LlamaParse no devolvió un identificador de trabajo válido.");
		}
	}

	/**
	 * Valida un servidor configurado por el administrador, no por el formulario.
	 */
	private static String validarBaseUrl(String url) {
		URI uri = URI.create(url);
		boolean local = "http".equals(uri.getScheme())
				&& Set.of("localhost", "127.0.0.1").contains(uri.getHost() == null ? "" : uri.getHost());
		if ((!"https".equals(uri.getScheme()) && !local) || uri.getHost() == null || uri.getUserInfo() != null
				|| uri.getQuery() != null || uri.getFragment() != null
				|| (!uri.getPath().isEmpty() && !"/".equals(uri.getPath()))) {
			throw new IllegalArgumentException("llamaparse.base-url debe ser una raíz HTTPS válida");
		}
		return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
	}
}
