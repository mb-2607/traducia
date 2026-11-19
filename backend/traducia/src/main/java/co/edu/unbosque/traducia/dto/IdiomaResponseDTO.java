package co.edu.unbosque.traducia.dto;

/**
 * DTO de salida para recibir un idioma.
 *
 * Contiene el nombre y el código del respectivo idioma.
 */
public class IdiomaResponseDTO {
	
	/**
	 * Código del idioma en ISO.
	 */
	private String codigo;
	
	/**
	 * Nombre del idioma.
	 */
	private String nombre;
	
	/**
	 * Constructor vacío, requerido por JPA y por la deserialización JSON.
	 */
	public IdiomaResponseDTO() {

	}
	
	/**
	 * Crea una instancia con todos los atributos.
	 *
	 * @param codigo iso del idioma de origen
	 * @param nombre del idioma
	 */
	public IdiomaResponseDTO(String codigo, String nombre) {
		this.codigo = codigo;
		this.nombre = nombre;
	}
	
	/**
	 * Obtiene el código del idioma.
	 */
	public String getCodigo() {
		return codigo;
	}
	
	/**
	 * Modifica el código del idioma.
	 */
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}
	
	/**
	 * Obtiene el nombre del idioma.
	 */
	public String getNombre() {
		return nombre;
	}
	
	/**
	 * Modifica elnombre del idioma.
	 */
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

}
