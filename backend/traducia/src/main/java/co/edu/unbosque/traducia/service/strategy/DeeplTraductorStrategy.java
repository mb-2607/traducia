package co.edu.unbosque.traducia.service.strategy;

import org.springframework.stereotype.Service;

import co.edu.unbosque.traducia.dto.TraduccionRequestDTO;
import co.edu.unbosque.traducia.entity.ProveedorTraduccion;

/**
 * Estrategia de traducción para el proveedor DeepL.
 *
 * Implementación simulada: por ahora devuelve un texto fijo y no llama a la API real.
 */
@Service
public class DeeplTraductorStrategy implements TraductorStrategy{

	/**
	 * Traduce el texto con DeepL.
	 *
	 * Implementación simulada que devuelve un texto fijo.
	 *
	 * @param dto petición con idiomas de origen y destino y texto
	 * @return texto fijo de prueba
	 */
	@Override
	public String traducir(TraduccionRequestDTO dto) {
		return "Tradujendo desde aquí DeepL";
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

	

}
