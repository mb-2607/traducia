package co.edu.unbosque.traducia.dto;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import co.edu.unbosque.traducia.entity.ProveedorTraduccion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

/**
 * DTO de respuesta con el detalle de un plan de traducción.
 */
public class PlanResponseDTO {

	/**
	 * Nombre del plan de traducción.
	 */
	@NotBlank(message = "El nombre del plan es obligatorio")
	private String plan;

	/**
	 * Cantidad máxima de caracteres permitidos por solicitud.
	 */
	@Positive(message = "El límite de caracteres por solicitud debe ser mayor a cero")
	private long numeroCaracterPorSolicitud;

	/**
	 * Conjunto de proveedores de traducción habilitados en el plan.
	 */
	@NotEmpty(message = "El plan debe tener al menos un proveedor habilitado")
	private Set<ProveedorTraduccion> proveedores;

	/**
	 * Constructor vacío requerido para serialización.
	 */
	public PlanResponseDTO() {
	}

	/**
	 * Crea el DTO con la información del plan.
	 *
	 * @param plan                       nombre del plan.
	 * @param numeroCaracterPorSolicitud límite de caracteres por solicitud.
	 * @param proveedores                proveedores habilitados en el plan.
	 */
	public PlanResponseDTO(String plan, long numeroCaracterPorSolicitud, Set<ProveedorTraduccion> proveedores) {
		this.plan = plan;
		this.numeroCaracterPorSolicitud = numeroCaracterPorSolicitud;
		this.proveedores = proveedores == null ? null : new LinkedHashSet<>(proveedores);
	}

	/**
	 * Obtiene el nombre del plan.
	 *
	 * @return nombre del plan.
	 */
	public String getPlan() {
		return plan;
	}

	/**
	 * Asigna el nombre del plan.
	 *
	 * @param plan nombre del plan.
	 */
	public void setPlan(String plan) {
		this.plan = plan;
	}

	/**
	 * Obtiene el límite de caracteres por solicitud.
	 *
	 * @return cantidad máxima de caracteres permitidos.
	 */
	public long getNumeroCaracterPorSolicitud() {
		return numeroCaracterPorSolicitud;
	}

	/**
	 * Asigna el límite de caracteres por solicitud.
	 *
	 * @param numeroCaracterPorSolicitud cantidad máxima de caracteres permitidos.
	 */
	public void setNumeroCaracterPorSolicitud(long numeroCaracterPorSolicitud) {
		this.numeroCaracterPorSolicitud = numeroCaracterPorSolicitud;
	}

	/**
	 * Obtiene los proveedores habilitados por el plan.
	 *
	 * @return conjunto inmutable de proveedores permitidos.
	 */
	public Set<ProveedorTraduccion> getProveedores() {
		return proveedores == null ? null : Collections.unmodifiableSet(new LinkedHashSet<>(proveedores));
	}

	/**
	 * Asigna los proveedores habilitados por el plan.
	 *
	 * @param proveedores conjunto de proveedores permitidos.
	 */
	public void setProveedores(Set<ProveedorTraduccion> proveedores) {
		this.proveedores = proveedores == null ? null : new LinkedHashSet<>(proveedores);
	}
}