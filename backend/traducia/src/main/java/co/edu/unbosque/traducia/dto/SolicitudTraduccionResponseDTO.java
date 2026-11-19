package co.edu.unbosque.traducia.dto;

import java.util.Date;
import java.util.List;

public class SolicitudTraduccionResponseDTO {

	private long id;
	private Date fechaCreacion;
	private String textoOriginal;
	private List<TraduccionResponseDTO> listaTraduccion;

	public SolicitudTraduccionResponseDTO() {

	}

	public SolicitudTraduccionResponseDTO(long id, Date fechaCreacion, String textoOriginal,
			List<TraduccionResponseDTO> listaTraduccion) {
		this.id = id;
		this.fechaCreacion = fechaCreacion;
		this.textoOriginal = textoOriginal;
		this.listaTraduccion = listaTraduccion;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public String getTextoOriginal() {
		return textoOriginal;
	}

	public void setTextoOriginal(String textoOriginal) {
		this.textoOriginal = textoOriginal;
	}

	public List<TraduccionResponseDTO> getListaTraduccion() {
		return listaTraduccion;
	}

	public void setListaTraduccion(List<TraduccionResponseDTO> listaTraduccion) {
		this.listaTraduccion = listaTraduccion;
	}

	

}
