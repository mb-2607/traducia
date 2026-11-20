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

public class ExternalHTTPRequestHandler {

	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2)
			.connectTimeout(Duration.ofSeconds(20)).build();
	
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
