package co.edu.unbosque.traducia.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de respuesta con la información de un proveedor de traducción y su
 * disponibilidad respecto al plan del usuario autenticado.
 */
public class ProveedorResponseDTO {

	/**
	 * Nombre del proveedor de traducción.
	 */
	@NotBlank(message = "El nombre del proveedor es obligatorio")
	private String nombre;

	/**
	 * Tipo o categoría del proveedor de traducción.
	 */
	@NotBlank(message = "El tipo de proveedor es obligatorio")
	private String tipoProveedor;

	/**
	 * Indica si el proveedor está habilitado para el plan vigente del usuario.
	 */
	private boolean disponibleEnPlan;

	/**
	 * Constructor vacío requerido para serialización.
	 */
	public ProveedorResponseDTO() {
	}

	/**
	 * Crea el DTO con el nombre, tipo y disponibilidad del proveedor.
	 *
	 * @param nombre           nombre del proveedor de traducción.
	 * @param tipoProveedor    categoría o tipo del proveedor.
	 * @param disponibleEnPlan {@code true} si el proveedor está incluido en el plan del usuario.
	 */
	public ProveedorResponseDTO(String nombre, String tipoProveedor, boolean disponibleEnPlan) {
		this.nombre = nombre;
		this.tipoProveedor = tipoProveedor;
		this.disponibleEnPlan = disponibleEnPlan;
	}

	/**
	 * Obtiene el nombre del proveedor.
	 *
	 * @return nombre del proveedor.
	 */
	public String getNombre() {
		return nombre;
	}

	/**
	 * Asigna el nombre del proveedor.
	 *
	 * @param nombre nombre del proveedor.
	 */
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	/**
	 * Obtiene el tipo de proveedor.
	 *
	 * @return tipo de proveedor.
	 */
	public String getTipoProveedor() {
		return tipoProveedor;
	}

	/**
	 * Asigna el tipo de proveedor.
	 *
	 * @param tipoProveedor tipo de proveedor.
	 */
	public void setTipoProveedor(String tipoProveedor) {
		this.tipoProveedor = tipoProveedor;
	}

	/**
	 * Indica si el proveedor está disponible para el plan del usuario.
	 *
	 * @return {@code true} si está habilitado en el plan vigente.
	 */
	public boolean isDisponibleEnPlan() {
		return disponibleEnPlan;
	}

	/**
	 * Define si el proveedor está disponible para el plan del usuario.
	 *
	 * @param disponibleEnPlan {@code true} si está habilitado en el plan vigente.
	 */
	public void setDisponibleEnPlan(boolean disponibleEnPlan) {
		this.disponibleEnPlan = disponibleEnPlan;
	}
}