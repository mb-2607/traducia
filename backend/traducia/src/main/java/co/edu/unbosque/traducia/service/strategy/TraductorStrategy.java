package co.edu.unbosque.traducia.service.strategy;

import co.edu.unbosque.traducia.dto.TraduccionRequestDTO;
import co.edu.unbosque.traducia.entity.ProveedorTraduccion;

/**
 * Contrato del patrón Strategy para los proveedores de traducción.
 *
 * Cada proveedor ({@link ProveedorTraduccion}) tiene una implementación propia.
 */
public interface TraductorStrategy {
	/**
	 * Traduce el texto de la petición con el proveedor de esta estrategia.
	 *
	 * @param dto petición con idiomas de origen y destino y texto
	 * @return el texto traducido
	 */
	String traducir(TraduccionRequestDTO dto);

	/**
	 * Indica a qué proveedor corresponde esta estrategia.
	 *
	 * @return el proveedor que implementa la estrategia
	 */
	ProveedorTraduccion getProveedor();
}
