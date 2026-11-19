package co.edu.unbosque.traducia.service.strategy;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import co.edu.unbosque.traducia.entity.ProveedorTraduccion;

@Component
public class TraductorStrategyRegistry {
	
	private final Map<ProveedorTraduccion, TraductorStrategy> strategieMap;
	
	public TraductorStrategyRegistry(List<TraductorStrategy> listaStrategy) {
		this.strategieMap = new EnumMap<>(ProveedorTraduccion.class);
		listaStrategy.forEach((TraductorStrategy ts) -> strategieMap.put(ts.getProveedor(), ts));
	}
	
	public TraductorStrategy getStrategy(ProveedorTraduccion pt) {
		TraductorStrategy ts = strategieMap.get(pt);
		if(ts == null) {
			throw new IllegalArgumentException("Proveedor no soportado: " + pt);
		}
		return ts;
	}
}
