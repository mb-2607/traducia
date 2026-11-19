package co.edu.unbosque.traducia.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para solicitar una traducción.
 *
 * Contiene los códigos de los idiomas de origen y destino y el texto, que no puede estar vacío.
 */
public class TraduccionRequestDTO {

	/**
	 * Código del idioma de origen.
	 */
	private String origen;
	/**
	 * Código del idioma de destino.
	 */
	private String destino;
	
	/**
	 * Texto a traducir.
	 */
	@NotBlank
	private String texto;

	/**
	 * Constructor vacío, requerido por JPA y por la deserialización JSON.
	 */
	public TraduccionRequestDTO() {
		
	}

	/**
	 * Crea una instancia con todos los atributos.
	 *
	 * @param origen código del idioma de origen
	 * @param destino código del idioma de destino
	 * @param texto texto a traducir
	 */
	public TraduccionRequestDTO(String origen, String destino, String texto) {
		this.origen = origen;
		this.destino = destino;
		this.texto = texto;
	}

	/**
	 * Obtiene el código del idioma de origen.
	 *
	 * @return código del idioma de origen
	 */
	public String getOrigen() {
		return origen;
	}

	/**
	 * Establece el código del idioma de origen.
	 *
	 * @param origen código del idioma de origen a asignar
	 */
	public void setOrigen(String origen) {
		this.origen = origen;
	}

	/**
	 * Obtiene el código del idioma de destino.
	 *
	 * @return código del idioma de destino
	 */
	public String getDestino() {
		return destino;
	}

	/**
	 * Establece el código del idioma de destino.
	 *
	 * @param destino código del idioma de destino a asignar
	 */
	public void setDestino(String destino) {
		this.destino = destino;
	}

	/**
	 * Obtiene el texto a traducir.
	 *
	 * @return texto a traducir
	 */
	public String getTexto() {
		return texto;
	}

	/**
	 * Establece el texto a traducir.
	 *
	 * @param texto texto a traducir a asignar
	 */
	public void setTexto(String texto) {
		this.texto = texto;
	}

}
