package co.edu.unbosque.traducia.dto;

import co.edu.unbosque.traducia.entity.EstadoTraduccion;
import co.edu.unbosque.traducia.entity.ProveedorTraduccion;
import co.edu.unbosque.traducia.entity.TipoProveedor;

/**
 * DTO de salida con el resultado de un proveedor para una traducción.
 */
public class TraduccionResponseDTO {

	/**
	 * Estado de la traducción (exitosa o fallida).
	 */
	private EstadoTraduccion estado;
	/**
	 * Texto resultante de la traducción.
	 */
	private String textoTraducido;
	/**
	 * Tipo de proveedor (tradicional o de inteligencia artificial).
	 */
	private TipoProveedor tipoProveedor;
	/**
	 * Proveedor que generó la traducción.
	 */
	private ProveedorTraduccion proveedorTraduccion;

	/**
	 * Constructor vacío, requerido por JPA y por la deserialización JSON.
	 */
	public TraduccionResponseDTO() {

	}

	/**
	 * Crea una instancia con todos los atributos.
	 *
	 * @param estado estado de la traducción (exitosa o fallida)
	 * @param textoTraducido texto resultante de la traducción
	 * @param tipoProveedor tipo de proveedor (tradicional o de inteligencia artificial)
	 * @param proveedorTraduccion proveedor que generó la traducción
	 */
	public TraduccionResponseDTO(EstadoTraduccion estado, String textoTraducido, TipoProveedor tipoProveedor,
			ProveedorTraduccion proveedorTraduccion) {
		this.estado = estado;
		this.textoTraducido = textoTraducido;
		this.tipoProveedor = tipoProveedor;
		this.proveedorTraduccion = proveedorTraduccion;
	}

	/**
	 * Obtiene el texto resultante de la traducción.
	 *
	 * @return texto resultante de la traducción
	 */
	public String getTextoTraducido() {
		return textoTraducido;
	}

	/**
	 * Establece el texto resultante de la traducción.
	 *
	 * @param textoTraducido texto resultante de la traducción a asignar
	 */
	public void setTextoTraducido(String textoTraducido) {
		this.textoTraducido = textoTraducido;
	}

	/**
	 * Obtiene el tipo de proveedor (tradicional o de inteligencia artificial).
	 *
	 * @return tipo de proveedor (tradicional o de inteligencia artificial)
	 */
	public TipoProveedor getTipoProveedor() {
		return tipoProveedor;
	}

	/**
	 * Establece el tipo de proveedor (tradicional o de inteligencia artificial).
	 *
	 * @param tipoProveedor tipo de proveedor (tradicional o de inteligencia artificial) a asignar
	 */
	public void setTipoProveedor(TipoProveedor tipoProveedor) {
		this.tipoProveedor = tipoProveedor;
	}

	/**
	 * Obtiene el proveedor que generó la traducción.
	 *
	 * @return proveedor que generó la traducción
	 */
	public ProveedorTraduccion getProveedorTraduccion() {
		return proveedorTraduccion;
	}

	/**
	 * Establece el proveedor que generó la traducción.
	 *
	 * @param proveedorTraduccion proveedor que generó la traducción a asignar
	 */
	public void setProveedorTraduccion(ProveedorTraduccion proveedorTraduccion) {
		this.proveedorTraduccion = proveedorTraduccion;
	}

	/**
	 * Obtiene el estado de la traducción (exitosa o fallida).
	 *
	 * @return estado de la traducción (exitosa o fallida)
	 */
	public EstadoTraduccion getEstado() {
		return estado;
	}

	/**
	 * Establece el estado de la traducción (exitosa o fallida).
	 *
	 * @param estado estado de la traducción (exitosa o fallida) a asignar
	 */
	public void setEstado(EstadoTraduccion estado) {
		this.estado = estado;
	}

}
