package co.edu.unbosque.traducia.service.strategy;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import co.edu.unbosque.traducia.dto.DeepLRequestDTO;
import co.edu.unbosque.traducia.dto.DeepLResponseDTO;
import co.edu.unbosque.traducia.dto.TraduccionRequestDTO;
import co.edu.unbosque.traducia.entity.AuthenticationType;
import co.edu.unbosque.traducia.entity.ProveedorTraduccion;
import co.edu.unbosque.traducia.service.ExternalHTTPRequestHandler;

/**
 * Estrategia de traducción para el proveedor DeepL.
 *
 * Realiza la traducción consumiendo el endpoint {@code /v2/translate} de la API
 * gratuita de DeepL mediante una petición HTTP POST. La autenticación se hace
 * con la clave configurada en la propiedad {@code deepl.api.key}.
 *
 * Esta clase forma parte del patrón Strategy: el servicio de traducción la
 * selecciona cuando el proveedor solicitado es
 * {@link ProveedorTraduccion#DEEPL}.
 *
 * @see TraductorStrategy
 * @see ExternalHTTPRequestHandler
 */
@Service
public class DeeplTraductorStrategy implements TraductorStrategy {
	
	/**
	 * URL del endpoint de traducción de la API gratuita de DeepL.
	 */
	private final String URL_BASE = "https://api-free.deepl.com/v2/translate";
	
	/**
	 * Clave de autenticación de DeepL, inyectada desde la propiedad
	 * {@code deepl.api.key} de la configuración de la aplicación.
	 */
	@Value("${deepl.api.key}")
	private String apiKey;
	
	/**
	 * Traduce el texto con DeepL.
	 *
	 * Normaliza el código del idioma de destino a los valores que acepta DeepL
	 * (ver {@link #modificarCodigoIdioma(String)}), construye la solicitud y la
	 * envía a la API. Devuelve la primera traducción de la respuesta.
	 *
	 * Nota: el idioma de origen del DTO no se envía; DeepL lo detecta
	 * automáticamente. Además, este método modifica el campo {@code destino} del
	 * DTO recibido, dejándolo con el código ya normalizado.
	 *
	 * @param dto petición con el idioma de destino y el texto a traducir
	 * @return texto traducido por DeepL
	 */
	@Override
	public String traducir(TraduccionRequestDTO dto) {
		dto.setDestino(modificarCodigoIdioma(dto.getDestino()));
		DeepLRequestDTO solicitud = new DeepLRequestDTO();
		solicitud.setTarget_lang(dto.getDestino());
		solicitud.getText().add(dto.getTexto());
		DeepLResponseDTO response = ExternalHTTPRequestHandler.doPostAndConvertToDTO(URL_BASE, apiKey, AuthenticationType.DEEPL, solicitud, DeepLResponseDTO.class);
		return response.getTranslations().get(0).getText();
	}

	/**
	 * Indica que esta estrategia corresponde a {@link ProveedorTraduccion#DEEPL}.
	 *
	 * @return {@code ProveedorTraduccion.DEEPL}
	 */
	@Override
	public ProveedorTraduccion getProveedor() {
		return ProveedorTraduccion.DEEPL;
	}
	
	/**
	 * Convierte un código de idioma de la aplicación al formato que espera DeepL.
	 *
	 * Los códigos chinos se traducen a las variantes de DeepL: {@code zh-CN}
	 * pasa a {@code ZH-HANS} (simplificado) y {@code zh-TW} pasa a
	 * {@code ZH-HANT} (tradicional). Cualquier otro código se devuelve en
	 * mayúsculas.
	 *
	 * @param codigo código de idioma de la aplicación (por ejemplo {@code es},
	 *               {@code en}, {@code zh-CN})
	 * @return código de idioma en el formato de DeepL
	 */
	private String modificarCodigoIdioma(String codigo) {
		if(codigo.equals("zh-CN")) {
			codigo = "ZH-HANS";
		} else if(codigo.equals("zh-TW")) {
			codigo = "ZH-HANT";
		}
		return codigo.toUpperCase();
	}

}