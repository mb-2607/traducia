package co.edu.unbosque.traducia.dto;

import co.edu.unbosque.traducia.entity.EstadoTraduccion;
import co.edu.unbosque.traducia.entity.ProveedorTraduccion;
import co.edu.unbosque.traducia.entity.TipoProveedor;

public class TraduccionResponseDTO {

	private EstadoTraduccion estado;
	private String origen;
	private String destino;
	private String textoTraducido;
	private TipoProveedor tipoProveedor;
	private ProveedorTraduccion proveedorTraduccion;

	public TraduccionResponseDTO() {

	}

	public TraduccionResponseDTO(EstadoTraduccion estado, String origen, String destino, String textoTraducido,
			TipoProveedor tipoProveedor, ProveedorTraduccion proveedorTraduccion) {
		this.estado = estado;
		this.origen = origen;
		this.destino = destino;
		this.textoTraducido = textoTraducido;
		this.tipoProveedor = tipoProveedor;
		this.proveedorTraduccion = proveedorTraduccion;
	}

	public String getOrigen() {
		return origen;
	}

	public void setOrigen(String origen) {
		this.origen = origen;
	}

	public String getDestino() {
		return destino;
	}

	public void setDestino(String destino) {
		this.destino = destino;
	}

	public String getTextoTraducido() {
		return textoTraducido;
	}

	public void setTextoTraducido(String textoTraducido) {
		this.textoTraducido = textoTraducido;
	}

	public TipoProveedor getTipoProveedor() {
		return tipoProveedor;
	}

	public void setTipoProveedor(TipoProveedor tipoProveedor) {
		this.tipoProveedor = tipoProveedor;
	}

	public ProveedorTraduccion getProveedorTraduccion() {
		return proveedorTraduccion;
	}

	public void setProveedorTraduccion(ProveedorTraduccion proveedorTraduccion) {
		this.proveedorTraduccion = proveedorTraduccion;
	}

	public EstadoTraduccion getEstado() {
		return estado;
	}

	public void setEstado(EstadoTraduccion estado) {
		this.estado = estado;
	}

}
