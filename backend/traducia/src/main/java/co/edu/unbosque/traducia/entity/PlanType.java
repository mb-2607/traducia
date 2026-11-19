package co.edu.unbosque.traducia.entity;

import java.util.Set;

/**
 * Enumeración que define los planes disponibles en el sistema.
 * 
 * FREE: Plan gratuito.
 * ORIA: Plan intermedio.
 * INFINITIA: Plan avanzado.
 */
public enum PlanType {
	FREE(Set.of(ProveedorTraduccion.DEEPL), 500),
	ORIA(Set.of(ProveedorTraduccion.DEEPL, ProveedorTraduccion.GEMINI), 2000),
	INFINITIA(Set.of(ProveedorTraduccion.DEEPL, ProveedorTraduccion.GEMINI, ProveedorTraduccion.OPENROUTER), 5000);
	
	private final Set<ProveedorTraduccion> listaProveedor;
	private final long numeroCaracterPorSolicitud;
	
	private PlanType(Set<ProveedorTraduccion> listaProveedor, long numeroCaracterPorSolicitud) {
		this.listaProveedor = listaProveedor;
		this.numeroCaracterPorSolicitud = numeroCaracterPorSolicitud;
	}
	public Set<ProveedorTraduccion> getListaProveedor() {
		return listaProveedor;
	}
	public long getNumeroCaracterPorSolicitud() {
		return numeroCaracterPorSolicitud;
	}
	
	
}
