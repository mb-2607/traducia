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
 * Estrategia de traducción para el modelo de IA Riva.
 *
 * Implementación simulada: por ahora devuelve un texto fijo y no llama al
 * modelo real.
 */
@Service
public class RivaTraductorStrategy implements TraductorStrategy {
	
	private final String URL_BASE = "https://integrate.api.nvidia.com/v1/chat/completions";
	
	@Value("${nvidia.api.key}")
	private String apiKey;
	/**
	 * Traduce el texto con Riva.
	 *
	 * Implementación simulada que devuelve un texto fijo.
	 *
	 * @param dto petición con idiomas de origen y destino y texto
	 * @return texto fijo de prueba
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
	
	private String modificarCodigoIdioma(String codigo) {
		if(codigo.equals("no")) {
			return "nb";
		}
		return codigo.toLowerCase();
	}
}