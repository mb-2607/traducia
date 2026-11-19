package co.edu.unbosque.traducia.dto;

import co.edu.unbosque.traducia.entity.EstadoTraduccion;
import co.edu.unbosque.traducia.entity.ProveedorTraduccion;
import co.edu.unbosque.traducia.entity.TipoProveedor;

public class TraduccionResponseDTO {

	private EstadoTraduccion estado;
	private String textoTraducido;
	private TipoProveedor tipoProveedor;
	private ProveedorTraduccion proveedorTraduccion;

	public TraduccionResponseDTO() {

	}

	public TraduccionResponseDTO(EstadoTraduccion estado, String textoTraducido, TipoProveedor tipoProveedor,
			ProveedorTraduccion proveedorTraduccion) {
		this.estado = estado;
		this.textoTraducido = textoTraducido;
		this.tipoProveedor = tipoProveedor;
		this.proveedorTraduccion = proveedorTraduccion;
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
