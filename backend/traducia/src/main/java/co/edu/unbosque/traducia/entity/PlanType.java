package co.edu.unbosque.traducia.entity;

import java.util.Set;

/**
 * Enumeración que define los planes disponibles en el sistema.
 * 
 * FREE: Plan gratuito. ORIA: Plan intermedio. INFINITIA: Plan avanzado.
 */
public enum PlanType {
	FREE(Set.of(ProveedorTraduccion.DEEPL), 500),
	ORIA(Set.of(ProveedorTraduccion.DEEPL, ProveedorTraduccion.NEMOTRON), 2000),
	INFINITIA(Set.of(ProveedorTraduccion.DEEPL, ProveedorTraduccion.NEMOTRON, ProveedorTraduccion.RIVA), 5000);

	/**
	 * Conjunto de proveedores disponibles en el plan.
	 */
	private final Set<ProveedorTraduccion> listaProveedor;
	/**
	 * Número máximo de caracteres permitidos por solicitud.
	 */
	private final long numeroCaracterPorSolicitud;

	/**
	 * Crea la constante con sus atributos.
	 *
	 * @param listaProveedor             conjunto de proveedores disponibles en el
	 *                                   plan
	 * @param numeroCaracterPorSolicitud número máximo de caracteres permitidos por
	 *                                   solicitud
	 */
	private PlanType(Set<ProveedorTraduccion> listaProveedor, long numeroCaracterPorSolicitud) {
		this.listaProveedor = listaProveedor;
		this.numeroCaracterPorSolicitud = numeroCaracterPorSolicitud;
	}

	/**
	 * Obtiene el conjunto de proveedores disponibles en el plan.
	 *
	 * @return conjunto de proveedores disponibles en el plan
	 */
	public Set<ProveedorTraduccion> getListaProveedor() {
		return listaProveedor;
	}

	/**
	 * Obtiene el número máximo de caracteres permitidos por solicitud.
	 *
	 * @return número máximo de caracteres permitidos por solicitud
	 */
	public long getNumeroCaracterPorSolicitud() {
		return numeroCaracterPorSolicitud;
	}

}
