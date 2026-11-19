package co.edu.unbosque.traducia.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entidad con el resultado de un proveedor para una solicitud (tabla <code>traduccion</code>).
 *
 * Guarda el proveedor, el texto traducido y si la traducción fue exitosa o fallida.
 */
@Entity
@Table(name = "traduccion")
public class Traduccion {

	/**
	 * Identificador único del registro.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // skipcq: JAVA-E1065

	/**
	 * Proveedor que generó la traducción.
	 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ProveedorTraduccion proveedorTraduccion;

	/**
	 * Texto resultante de la traducción.
	 */
	@Column(columnDefinition = "TEXT")
	private String textoTraducido;

	/**
	 * Estado de la traducción (exitosa o fallida).
	 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EstadoTraduccion estado;

	/**
	 * Solicitud de traducción a la que pertenece.
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "solicitud_id")
	private SolicitudTraduccion solicitud;

	/**
	 * Constructor vacío, requerido por JPA y por la deserialización JSON.
	 */
	public Traduccion() {
	}

	/**
	 * Crea una instancia con todos los atributos.
	 *
	 * @param proveedorTraduccion proveedor que generó la traducción
	 * @param textoTraducido texto resultante de la traducción
	 * @param estado estado de la traducción (exitosa o fallida)
	 * @param solicitud solicitud de traducción a la que pertenece
	 */
	public Traduccion(ProveedorTraduccion proveedorTraduccion, String textoTraducido, EstadoTraduccion estado,
			SolicitudTraduccion solicitud) {
		this.proveedorTraduccion = proveedorTraduccion;
		this.textoTraducido = textoTraducido;
		this.estado = estado;
		this.solicitud = solicitud;
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

	/**
	 * Obtiene la solicitud de traducción a la que pertenece.
	 *
	 * @return solicitud de traducción a la que pertenece
	 */
	public SolicitudTraduccion getSolicitud() {
		return solicitud;
	}

	/**
	 * Establece la solicitud de traducción a la que pertenece.
	 *
	 * @param solicitud solicitud de traducción a la que pertenece a asignar
	 */
	public void setSolicitud(SolicitudTraduccion solicitud) {
		this.solicitud = solicitud;
	}

	/**
	 * Obtiene el identificador único del registro.
	 *
	 * @return identificador único del registro
	 */
	public Long getId() {
		return id;
	}

}
