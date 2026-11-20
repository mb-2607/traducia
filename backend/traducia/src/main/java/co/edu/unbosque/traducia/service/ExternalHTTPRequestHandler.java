package co.edu.unbosque.traducia.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import co.edu.unbosque.traducia.entity.AuthenticationType;
import co.edu.unbosque.traducia.exception.ExternalAPIException;

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
	
	/** Cliente HTTP compartido por todas las peticiones (HTTP/2, timeout de conexión de 20 s). */
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
	public static <T> T doGetAndConvertToDTO(String url, String apiKey, AuthenticationType authType,Class<T> classType){
		HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url))
				.header("Content-Type", "application/json")
				.header("Authorization", authType.crearFormatoLlave(apiKey))
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

}
