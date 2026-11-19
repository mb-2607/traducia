package co.edu.unbosque.traducia.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SolicitudTraduccionResponseDTO {

	private long id;
	private LocalDateTime fechaCreacion;
	private String textoOriginal;
	private String origen;
	private String destino;
	private List<TraduccionResponseDTO> listaTraduccion;

	public SolicitudTraduccionResponseDTO() {

	}

	public SolicitudTraduccionResponseDTO(long id, LocalDateTime fechaCreacion, String textoOriginal, String origen,
			String destino, List<TraduccionResponseDTO> listaTraduccion) {
		this.id = id;
		this.fechaCreacion = fechaCreacion;
		this.textoOriginal = textoOriginal;
		this.origen = origen;
		this.destino = destino;
		this.listaTraduccion = listaTraduccion;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(LocalDateTime fechaCreacion) {
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

}
