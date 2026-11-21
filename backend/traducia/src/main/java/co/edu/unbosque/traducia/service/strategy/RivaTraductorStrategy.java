package co.edu.unbosque.traducia.service.strategy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import co.edu.unbosque.traducia.dto.NvidiaMessageRequestDTO;
import co.edu.unbosque.traducia.dto.NvidiaRequestDTO;
import co.edu.unbosque.traducia.dto.NvidiaResponseDTO;
import co.edu.unbosque.traducia.dto.TraduccionRequestDTO;
import co.edu.unbosque.traducia.entity.AuthenticationType;
import co.edu.unbosque.traducia.entity.ProveedorTraduccion;
import co.edu.unbosque.traducia.service.ExternalHTTPRequestHandler;

/**
 * Estrategia de traducción que utiliza el modelo Riva Translate
 * ({@code nvidia/riva-translate-4b-instruct-v2}) alojado en la API de chat de
 * NVIDIA.
 * <p>
 * Riva no recibe instrucciones en lenguaje natural: el mensaje de rol
 * {@code system} contiene únicamente el par de idiomas (por ejemplo,
 * {@code "es-en"}) y el mensaje de rol {@code user} contiene el texto a
 * traducir. Como el modelo se usa con inglés como idioma intermedio, la
 * traducción se hace en dos pasos:
 * </p>
 * <ol>
 *   <li>Idioma de origen → inglés (par {@code "<origen>-en"}).</li>
 *   <li>Inglés → idioma de destino (par {@code "en-<destino>"}), usando como
 *   entrada la salida del primer paso.</li>
 * </ol>
 * <p>
 * Es una de las implementaciones de {@link TraductorStrategy} y se asocia al
 * proveedor {@link ProveedorTraduccion#RIVA}.
 * </p>
 *
 * @see TraductorStrategy
 * @see NvidiaRequestDTO
 * @see NvidiaResponseDTO
 */
@Service
public class RivaTraductorStrategy implements TraductorStrategy {
	
	/** URL del endpoint de chat completions de NVIDIA. */
	private final String URL_BASE = "https://integrate.api.nvidia.com/v1/chat/completions";
	
	/**
	 * API key de NVIDIA, inyectada desde la propiedad {@code nvidia.api.key}
	 * de la configuración de Spring.
	 */
	@Value("${nvidia.api.key}")
	private String apiKey;
	
	/**
	 * Traduce el texto indicado en la petición usando Riva, con inglés como
	 * idioma intermedio.
	 * <p>
	 * Realiza dos peticiones HTTP consecutivas a NVIDIA, sin streaming y con un
	 * máximo de 4096 tokens de salida: la primera traduce del idioma de origen
	 * al inglés, y la segunda traduce ese resultado del inglés al idioma de
	 * destino. Se reutiliza el mismo {@link NvidiaRequestDTO}, reemplazando
	 * los mensajes de sistema y de usuario entre una llamada y otra.
	 * </p>
	 *
	 * @param dto petición con los idiomas de origen y destino y el texto a traducir
	 * @return el texto traducido al idioma de destino
	 * @throws NullPointerException      si alguna de las respuestas no contiene {@code choices}
	 * @throws IndexOutOfBoundsException si alguna de las respuestas trae {@code choices} vacío
	 */
	@Override
	public String traducir(TraduccionRequestDTO dto) {
		NvidiaRequestDTO solicitud = new NvidiaRequestDTO();
		solicitud.getMessages().add(new NvidiaMessageRequestDTO("system", modificarCodigoIdioma(dto.getOrigen())+"-en"));
		solicitud.getMessages().add(new NvidiaMessageRequestDTO("user", dto.getTexto()));
		solicitud.setModel("nvidia/riva-translate-4b-instruct-v2");
		solicitud.setMax_tokens(4096);
		solicitud.setStream(false);
		
		NvidiaResponseDTO respuesta = ExternalHTTPRequestHandler.doPostAndConvertToDTO(URL_BASE, apiKey, AuthenticationType.BEARER, solicitud, NvidiaResponseDTO.class);
		solicitud.getMessages().set(0,(new NvidiaMessageRequestDTO("system", modificarCodigoIdioma("en-"+modificarCodigoIdioma(dto.getDestino())))));
		solicitud.getMessages().set(1,(new NvidiaMessageRequestDTO("user", respuesta.obtenerContenido())));
		
		respuesta = ExternalHTTPRequestHandler.doPostAndConvertToDTO(URL_BASE, apiKey, AuthenticationType.BEARER, solicitud, NvidiaResponseDTO.class);
		return respuesta.obtenerContenido();
	}

	/**
	 * Indica que esta estrategia corresponde a {@link ProveedorTraduccion#RIVA}.
	 *
	 * @return {@code ProveedorTraduccion.RIVA}
	 */
	@Override
	public ProveedorTraduccion getProveedor() {
		return ProveedorTraduccion.RIVA;
	}
	
	/**
	 * Adapta un código de idioma al formato que espera Riva.
	 * <p>
	 * Convierte el código a minúsculas y, si recibe {@code "no"} (noruego),
	 * lo transforma en {@code "nb"} (noruego bokmål).
	 * </p>
	 *
	 * @param codigo código de idioma (por ejemplo, {@code "es"} o {@code "en-US"})
	 * @return el código normalizado (por ejemplo, {@code "es"} o {@code "en-us"})
	 */
	private String modificarCodigoIdioma(String codigo) {
		if(codigo.equals("no")) {
			return "nb";
		}
		return codigo.toLowerCase();
	}
}