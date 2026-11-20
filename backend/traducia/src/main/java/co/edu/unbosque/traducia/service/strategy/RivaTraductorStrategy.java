package co.edu.unbosque.traducia.service.strategy;

import org.springframework.stereotype.Service;

import co.edu.unbosque.traducia.dto.TraduccionRequestDTO;
import co.edu.unbosque.traducia.entity.ProveedorTraduccion;

/**
 * Estrategia de traducción para el modelo de IA Riva.
 *
 * Implementación simulada: por ahora devuelve un texto fijo y no llama al
 * modelo real.
 */
@Service
public class RivaTraductorStrategy implements TraductorStrategy {

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
		return "Traducciendo desde Riva";
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

}