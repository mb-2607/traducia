package co.edu.unbosque.traducia.service.strategy;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import co.edu.unbosque.traducia.entity.ProveedorTraduccion;

/**
 * Registro de estrategias de traducción.
 *
 * Spring inyecta todas las implementaciones de {@link TraductorStrategy} y este
 * componente las organiza en un {@link EnumMap} para obtener la correcta según
 * el proveedor.
 */
@Component
public class TraductorStrategyRegistry {

	/**
	 * Mapa que asocia cada proveedor con su estrategia de traducción.
	 */
	private final Map<ProveedorTraduccion, TraductorStrategy> strategieMap;

	/**
	 * Crea el componente con sus dependencias inyectadas por Spring.
	 *
	 * @param listaStrategy lista de estrategias de traducción detectadas por Spring
	 */
	public TraductorStrategyRegistry(List<TraductorStrategy> listaStrategy) {
		listaStrategy.forEach((TraductorStrategy pt)->{
			System.out.println(pt.getProveedor() + " " + pt.toString());
		});
		this.strategieMap = new EnumMap<>(ProveedorTraduccion.class);
		listaStrategy.forEach((TraductorStrategy ts) -> strategieMap.put(ts.getProveedor(), ts));
	}

	/**
	 * Obtiene la estrategia de un proveedor.
	 *
	 * @param pt proveedor de traducción
	 * @return la estrategia registrada para ese proveedor
	 * @throws IllegalArgumentException si no hay estrategia para el proveedor
	 */
	public TraductorStrategy getStrategy(ProveedorTraduccion pt) {
		TraductorStrategy ts = strategieMap.get(pt);
		if (ts == null) {
			throw new IllegalArgumentException("Proveedor no soportado: " + pt);
		}
		return ts;
	}
}
