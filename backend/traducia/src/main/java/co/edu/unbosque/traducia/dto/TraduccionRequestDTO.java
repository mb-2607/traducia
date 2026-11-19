package co.edu.unbosque.traducia.dto;

import jakarta.validation.constraints.NotBlank;

public class TraduccionRequestDTO {

	private String origen;
	private String destino;
	
	@NotBlank
	private String texto;

	public TraduccionRequestDTO() {
		
	}

	public TraduccionRequestDTO(String origen, String destino, String texto) {
		this.origen = origen;
		this.destino = destino;
		this.texto = texto;
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

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

}
