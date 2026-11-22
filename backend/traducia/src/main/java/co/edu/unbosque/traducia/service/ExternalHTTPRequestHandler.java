package co.edu.unbosque.traducia.service;

import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import co.edu.unbosque.traducia.entity.AuthenticationType;
import co.edu.unbosque.traducia.exception.ExternalAPIException;
import co.edu.unbosque.traducia.dto.ArchivoMultipartDTO;

/**
 * Utilidad para realizar peticiones HTTP a APIs externas y convertir la
 * respuesta JSON en objetos DTO.
 * <p>
 * Usa un único {@link HttpClient} compartido (HTTP/2, con tiempo máximo de
 * conexión de 20 segundos) y Gson para la deserialización.
 * </p>
 *
 * @author TradudIA
 * @version 1.0
 */
public class ExternalHTTPRequestHandler {

	/**
	 * Cliente HTTP compartido por todas las peticiones (HTTP/2, timeout de conexión
	 * de 20 s).
	 */
	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2)
			.connectTimeout(Duration.ofSeconds(20)).build();

	/**
	 * Realiza una petición HTTP GET a una API externa y deserializa el cuerpo JSON
	 * de la respuesta en un objeto del tipo indicado.
	 * <p>
	 * La petición incluye los encabezados {@code Content-Type: application/json} y
	 * {@code Authorization}, este último construido con
	 * {@link AuthenticationType#crearFormatoLlave(String)}.
	 * </p>
	 *
	 * @param <T>       tipo del DTO al que se convertirá la respuesta.
	 * @param url       dirección completa del recurso a consultar.
	 * @param apiKey    llave o token de acceso de la API externa.
	 * @param authType  tipo de autenticación que define el esquema del encabezado
	 *                  {@code Authorization}.
	 * @param classType clase del DTO destino para la deserialización con Gson.
	 * @return objeto de tipo {@code T} con los datos de la respuesta.
	 * @throws ExternalAPIException     si ocurre un error de comunicación (código
	 *                                  502), la petición es interrumpida (código
	 *                                  504) o la API responde con un código HTTP
	 *                                  fuera del rango 2xx.
	 * @throws IllegalStateException    si el cuerpo de la respuesta no es un JSON
	 *                                  válido para {@code classType}.
	 * @throws IllegalArgumentException si {@code url} no es una URI válida.
	 */
	public static <T> T doGetAndConvertToDTO(String url, String apiKey, AuthenticationType authType,
			Class<T> classType) {
		HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url))
				.header("Content-Type", "application/json").header("Authorization", authType.crearFormatoLlave(apiKey))
				.build();

		HttpResponse<String> response = null;
		try {
			response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException e) {
			throw new ExternalAPIException(502);
		} catch (InterruptedException e) {
			throw new ExternalAPIException(504);
		}

		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new ExternalAPIException(response.statusCode());
		}
		String json = response.body();

		try {
			T dto = new GsonBuilder().create().fromJson(json, classType);
			return dto;
		} catch (JsonParseException e) {
			throw new IllegalStateException("Error al deserializar el JSON");
		}

	}

	/**
	 * Realiza una petición HTTP POST a una API externa enviando un cuerpo JSON y
	 * deserializa el cuerpo JSON de la respuesta en un objeto del tipo indicado.
	 * <p>
	 * El objeto {@code body} se serializa a JSON con Gson y se envía con los
	 * encabezados {@code Content-Type: application/json} y {@code Authorization},
	 * este último construido con
	 * {@link AuthenticationType#crearFormatoLlave(String)}. La petición tiene un
	 * tiempo máximo de espera de 60 segundos.
	 * </p>
	 *
	 * @param <T>       tipo del DTO al que se convertirá la respuesta.
	 * @param url       dirección completa del recurso al que se enviará la
	 *                  petición.
	 * @param apiKey    llave o token de acceso de la API externa.
	 * @param authType  tipo de autenticación que define el esquema del encabezado
	 *                  {@code Authorization}.
	 * @param body      objeto (DTO o {@code Map}) que se serializará a JSON y se
	 *                  enviará como cuerpo de la petición.
	 * @param classType clase del DTO destino para la deserialización con Gson.
	 * @return objeto de tipo {@code T} con los datos de la respuesta.
	 * @throws ExternalAPIException     si ocurre un error de comunicación o se
	 *                                  agota el tiempo de espera (código 502), la
	 *                                  petición es interrumpida (código 504) o la
	 *                                  API responde con un código HTTP fuera del
	 *                                  rango 2xx.
	 * @throws IllegalStateException    si el cuerpo de la respuesta no es un JSON
	 *                                  válido para {@code classType}.
	 * @throws IllegalArgumentException si {@code url} no es una URI válida.
	 */
	public static <T> T doPostAndConvertToDTO(String url, String apiKey, AuthenticationType authType, Object body,
			Class<T> classType) {

		Gson gson = new Gson();
		String jsonBody = gson.toJson(body);

		HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(jsonBody))
				.uri(URI.create(url)).timeout(Duration.ofSeconds(60)).header("Content-Type", "application/json")
				.header("Authorization", authType.crearFormatoLlave(apiKey)).build();

		HttpResponse<String> response = null;
		try {
			response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException e) {
			throw new ExternalAPIException(502);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ExternalAPIException(504);
		}

		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new ExternalAPIException(response.statusCode());
		}

		try {
			return gson.fromJson(response.body(), classType);
		} catch (JsonParseException e) {
			throw new IllegalStateException("Error al deserializar el JSON", e);
		}
	}

	/**
	 * Realiza una petición POST multipart/form-data a una API externa.
	 * <p>
	 * Envía los campos de texto recibidos y un archivo bajo el campo {@code file}.
	 * Utiliza el cliente HTTP compartido y deserializa la respuesta JSON con Gson.
	 * </p>
	 *
	 * @param <T>           tipo del DTO de respuesta.
	 * @param url           dirección completa de la API externa.
	 * @param apiKey        llave o token de acceso de la API externa.
	 * @param authType      esquema de autenticación del encabezado Authorization.
	 * @param campos        campos de texto, por ejemplo model y language.
	 * @param archivo       contenido binario del audio.
	 * @param nombreArchivo nombre seguro del archivo, por ejemplo audio.webm.
	 * @param classType     clase del DTO de respuesta.
	 * @return respuesta deserializada; nunca null.
	 * @throws ExternalAPIException     si falla la comunicación, vence el tiempo de
	 *                                  espera o la respuesta no es 2xx.
	 * @throws IllegalArgumentException si el archivo está vacío o los encabezados
	 *                                  multipart no son válidos.
	 * @throws IllegalStateException    si la respuesta JSON no puede convertirse en
	 *                                  un DTO no nulo.
	 * @throws NullPointerException     si un argumento obligatorio es null.
	 */
	public static <T> T doPostMultipartAndConvertToDTO(String url, String apiKey, AuthenticationType authType,
			Map<String, String> campos, byte[] archivo, String nombreArchivo, Class<T> classType) {

		Objects.requireNonNull(url, "La URL es obligatoria");
		Objects.requireNonNull(apiKey, "La llave de acceso es obligatoria");
		Objects.requireNonNull(authType, "El tipo de autenticación es obligatorio");
		Objects.requireNonNull(campos, "Los campos son obligatorios");
		Objects.requireNonNull(archivo, "El archivo es obligatorio");
		Objects.requireNonNull(nombreArchivo, "El nombre del archivo es obligatorio");
		Objects.requireNonNull(classType, "La clase del DTO es obligatoria");
		if (archivo.length == 0) {
			throw new IllegalArgumentException("El archivo de audio está vacío");
		}

		if (!nombreArchivo.matches("[A-Za-z0-9._-]{1,100}")) {
			throw new IllegalArgumentException("El nombre del archivo no es válido");
		}

		String boundary = "TraduciaBoundary" + UUID.randomUUID();
		List<HttpRequest.BodyPublisher> partes = new ArrayList<>();
		for (Map.Entry<String, String> campo : campos.entrySet()) {
			validarCampoMultipart(campo.getKey(), campo.getValue());
			String parte = "--" + boundary + "\r\n" + "Content-Disposition: form-data; name=\"" + campo.getKey()
					+ "\"\r\n\r\n" + campo.getValue() + "\r\n";

			partes.add(HttpRequest.BodyPublishers.ofString(parte, StandardCharsets.UTF_8));
		}

		String encabezadoArchivo = "--" + boundary + "\r\n"
				+ "Content-Disposition: form-data; name=\"file\"; filename=\"" + nombreArchivo + "\"\r\n"
				+ "Content-Type: application/octet-stream\r\n\r\n";

		partes.add(HttpRequest.BodyPublishers.ofString(encabezadoArchivo, StandardCharsets.UTF_8));
		partes.add(HttpRequest.BodyPublishers.ofByteArray(archivo));
		partes.add(HttpRequest.BodyPublishers.ofString("\r\n--" + boundary + "--\r\n", StandardCharsets.UTF_8));

		HttpRequest.BodyPublisher cuerpo = HttpRequest.BodyPublishers
				.concat(partes.toArray(HttpRequest.BodyPublisher[]::new));
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(90))
				.header("Content-Type", "multipart/form-data; boundary=" + boundary)
				.header("Authorization", authType.crearFormatoLlave(apiKey)).POST(cuerpo).build();

		HttpResponse<String> response;
		try {
			response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		} catch (HttpTimeoutException e) {
			throw new ExternalAPIException(504);
		} catch (IOException e) {
			throw new ExternalAPIException(502);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ExternalAPIException(504);
		}

		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new ExternalAPIException(response.statusCode());
		}

		try {
			T dto = new GsonBuilder().create().fromJson(response.body(), classType);

			if (dto == null) {
				throw new IllegalStateException("La API externa devolvió una respuesta JSON nula");
			}

			return dto;
		} catch (JsonParseException e) {
			throw new IllegalStateException("Error al deserializar el JSON", e);
		}
	}

	/**
	 * Valida los campos de texto utilizados para construir el formulario multipart.
	 * <p>
	 * Restringe los nombres a caracteres seguros y rechaza saltos de línea en los
	 * valores utilizados por la solicitud de transcripción.
	 * </p>
	 *
	 * @param nombre nombre del campo.
	 * @param valor  contenido del campo.
	 * @throws IllegalArgumentException si el nombre o el valor no son válidos.
	 */
	private static void validarCampoMultipart(String nombre, String valor) {
		if (nombre == null || !nombre.matches("[A-Za-z0-9_-]{1,100}")) {
			throw new IllegalArgumentException("El nombre de un campo multipart no es válido");
		}

		if (valor == null || valor.contains("\r") || valor.contains("\n")) {
			throw new IllegalArgumentException("El valor de un campo multipart no es válido");
		}
	}

	/**
	 * Envía un archivo mediante multipart con encabezados específicos del
	 * proveedor. Permite elegir el campo binario y los encabezados del proveedor.
	 * Utiliza el HTTP_CLIENT existente y conserva los métodos de transcripción.
	 *
	 * @param <T>         tipo del DTO de respuesta
	 * @param url         endpoint completo controlado por la configuración del
	 *                    servidor
	 * @param encabezados encabezados adicionales; no deben incluir Content-Type
	 * @param campos      campos de texto del formulario
	 * @param archivo     campo, nombre seguro y bytes del archivo
	 * @param timeout     tiempo máximo de la petición, mayor que cero
	 * @param classType   clase del DTO de respuesta
	 * @return DTO no nulo deserializado con Gson
	 * @throws ExternalAPIException     si falla la comunicación o el JSON externo
	 * @throws IllegalArgumentException si hay encabezados o nombres no válidos
	 */
	public static <T> T doPostMultipartWithHeadersAndConvertToDTO(String url, Map<String, String> encabezados,
			Map<String, String> campos, ArchivoMultipartDTO archivo, Duration timeout, Class<T> classType) {
		Objects.requireNonNull(encabezados, "Los encabezados son obligatorios");
		Objects.requireNonNull(campos, "Los campos son obligatorios");
		Objects.requireNonNull(archivo, "El archivo es obligatorio");
		Objects.requireNonNull(timeout, "El tiempo máximo es obligatorio");
		Objects.requireNonNull(classType, "La clase de respuesta es obligatoria");
		if (timeout.isZero() || timeout.isNegative()) {
			throw new IllegalArgumentException("El tiempo máximo debe ser positivo");
		}

		String boundary = "TraduciaDocumento" + UUID.randomUUID();
		HttpRequest.BodyPublisher cuerpo = construirMultipartDocumento(boundary, campos, archivo);
		HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url)).timeout(timeout)
				.header("Content-Type", "multipart/form-data; boundary=" + boundary)
				.header("Accept", "application/json").POST(cuerpo);

		encabezados.forEach((nombre, valor) -> {
			validarEncabezadoDocumento(nombre, valor);
			if ("Content-Type".equalsIgnoreCase(nombre)) {
				throw new IllegalArgumentException("Content-Type se genera con el boundary multipart");
			}
			builder.header(nombre, valor);
		});

		return enviarMultipartDocumento(builder.build(), classType);
	}

	/** Construye el multipart sin convertir los bytes del archivo en texto. */
	private static HttpRequest.BodyPublisher construirMultipartDocumento(String boundary, Map<String, String> campos,
			ArchivoMultipartDTO archivo) {
		List<HttpRequest.BodyPublisher> partes = new ArrayList<>();
		campos.forEach((nombre, valor) -> {
			validarEncabezadoDocumento(nombre, valor);
			String parte = "--" + boundary + "\r\n" + "Content-Disposition: form-data; name=\"" + nombre + "\"\r\n\r\n"
					+ valor + "\r\n";
			partes.add(HttpRequest.BodyPublishers.ofString(parte, StandardCharsets.UTF_8));
		});
		validarEncabezadoDocumento(archivo.campo(), archivo.nombre());
		if (!archivo.nombre().matches("[A-Za-z0-9._-]{1,100}")) {
			throw new IllegalArgumentException("El nombre del archivo multipart no es seguro");
		}
		String cabecera = "--" + boundary + "\r\n" + "Content-Disposition: form-data; name=\"" + archivo.campo()
				+ "\"; filename=\"" + archivo.nombre() + "\"\r\n" + "Content-Type: application/octet-stream\r\n\r\n";
		partes.add(HttpRequest.BodyPublishers.ofString(cabecera, StandardCharsets.UTF_8));
		partes.add(HttpRequest.BodyPublishers.ofByteArray(archivo.contenido()));
		partes.add(HttpRequest.BodyPublishers.ofString("\r\n--" + boundary + "--\r\n", StandardCharsets.UTF_8));
		return HttpRequest.BodyPublishers.concat(partes.toArray(HttpRequest.BodyPublisher[]::new));
	}

	/** Rechaza caracteres que podrían alterar encabezados o partes multipart. */
	private static void validarEncabezadoDocumento(String nombre, String valor) {
		if (nombre == null || !nombre.matches("[A-Za-z0-9_-]{1,100}") || valor == null || valor.contains("\r")
				|| valor.contains("\n")) {
			throw new IllegalArgumentException("Un encabezado o campo multipart no es válido");
		}
	}

	/**
	 * Ejecuta el envío y evita propagar al cliente cuerpos externos con datos
	 * internos.
	 */
	private static <T> T enviarMultipartDocumento(HttpRequest request, Class<T> classType) {
		try {
			HttpResponse<String> response = HTTP_CLIENT.send(request,
					HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				throw new ExternalAPIException(response.statusCode());
			}
			T dto = new Gson().fromJson(response.body(), classType);
			if (dto == null) {
				throw new ExternalAPIException(502);
			}
			return dto;
		} catch (HttpTimeoutException e) {
			throw new ExternalAPIException(504);
		} catch (IOException e) {
			throw new ExternalAPIException(502);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ExternalAPIException(504);
		} catch (JsonParseException e) {
			throw new ExternalAPIException(502);
		}
	}

	/**
	 * Consulta un trabajo remoto con encabezados y timeout explícitos. Conserva el
	 * método GET original y reutiliza el envío y validación comunes.
	 *
	 * @param <T>         tipo del DTO de respuesta
	 * @param url         dirección completa de consulta
	 * @param encabezados encabezados de autenticación del proveedor
	 * @param timeout     tiempo máximo de esta consulta
	 * @param classType   tipo que se deserializa con Gson
	 * @return respuesta no nula
	 */
	public static <T> T doGetWithHeadersAndConvertToDTO(String url, Map<String, String> encabezados, Duration timeout,
			Class<T> classType) {
		HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url)).timeout(timeout)
				.header("Accept", "application/json").GET();
		encabezados.forEach((nombre, valor) -> {
			validarEncabezadoDocumento(nombre, valor);
			builder.header(nombre, valor);
		});
		return enviarMultipartDocumento(builder.build(), classType);
	}

}
