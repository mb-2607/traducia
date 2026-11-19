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

@Entity
@Table(name = "traduccion")
public class Traduccion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ProveedorTraduccion proveedorTraduccion;

	@Column(columnDefinition = "TEXT")
	private String textoTraducido;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EstadoTraduccion estado;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "solicitud_id")
	private SolicitudTraduccion solicitud;

	public Traduccion() {
	}

	public Traduccion(ProveedorTraduccion proveedorTraduccion, String textoTraducido, EstadoTraduccion estado,
			SolicitudTraduccion solicitud) {
		this.proveedorTraduccion = proveedorTraduccion;
		this.textoTraducido = textoTraducido;
		this.estado = estado;
		this.solicitud = solicitud;
	}

	public ProveedorTraduccion getProveedorTraduccion() {
		return proveedorTraduccion;
	}

	public void setProveedorTraduccion(ProveedorTraduccion proveedorTraduccion) {
		this.proveedorTraduccion = proveedorTraduccion;
	}

	public String getTextoTraducido() {
		return textoTraducido;
	}

	public void setTextoTraducido(String textoTraducido) {
		this.textoTraducido = textoTraducido;
	}

	public EstadoTraduccion getEstado() {
		return estado;
	}

	public void setEstado(EstadoTraduccion estado) {
		this.estado = estado;
	}

	public SolicitudTraduccion getSolicitud() {
		return solicitud;
	}

	public void setSolicitud(SolicitudTraduccion solicitud) {
		this.solicitud = solicitud;
	}

	public Long getId() {
		return id;
	}

}
