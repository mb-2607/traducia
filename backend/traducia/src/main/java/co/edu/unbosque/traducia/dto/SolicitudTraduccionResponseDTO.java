package co.edu.unbosque.traducia.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de salida con una solicitud de traducción y las traducciones de cada proveedor.
 */
public class SolicitudTraduccionResponseDTO {

	/**
	 * Identificador de la solicitud.
	 */
	private long id;
	/**
	 * Fecha y hora de creación de la solicitud.
	 */
	private LocalDateTime fechaCreacion;
	/**
	 * Texto original que se desea traducir.
	 */
	private String textoOriginal;
	/**
	 * Código del idioma de origen.
	 */
	private String origen;
	/**
	 * Código del idioma de destino.
	 */
	private String destino;
	/**
	 * Lista de traducciones, una por cada proveedor consultado.
	 */
	private List<TraduccionResponseDTO> listaTraduccion;

	/**
	 * Constructor vacío, requerido por JPA y por la deserialización JSON.
	 */
	public SolicitudTraduccionResponseDTO() {

	}

	/**
	 * Crea una instancia con todos los atributos.
	 *
	 * @param id identificador de la solicitud
	 * @param fechaCreacion fecha y hora de creación de la solicitud
	 * @param textoOriginal texto original que se desea traducir
	 * @param origen código del idioma de origen
	 * @param destino código del idioma de destino
	 * @param listaTraduccion lista de traducciones, una por cada proveedor consultado
	 */
	public SolicitudTraduccionResponseDTO(long id, LocalDateTime fechaCreacion, String textoOriginal, String origen,
			String destino, List<TraduccionResponseDTO> listaTraduccion) {
		this.id = id;
		this.fechaCreacion = fechaCreacion;
		this.textoOriginal = textoOriginal;
		this.origen = origen;
		this.destino = destino;
		this.listaTraduccion = listaTraduccion; // skipcq: JAVA-E1086
	}

	/**
	 * Obtiene el identificador de la solicitud.
	 *
	 * @return identificador de la solicitud
	 */
	public long getId() {
		return id;
	}

	/**
	 * Establece el identificador de la solicitud.
	 *
	 * @param id identificador de la solicitud a asignar
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Obtiene la fecha y hora de creación de la solicitud.
	 *
	 * @return fecha y hora de creación de la solicitud
	 */
	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	/**
	 * Establece la fecha y hora de creación de la solicitud.
	 *
	 * @param fechaCreacion fecha y hora de creación de la solicitud a asignar
	 */
	public void setFechaCreacion(LocalDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	/**
	 * Obtiene el texto original que se desea traducir.
	 *
	 * @return texto original que se desea traducir
	 */
	public String getTextoOriginal() {
		return textoOriginal;
	}

	/**
	 * Establece el texto original que se desea traducir.
	 *
	 * @param textoOriginal texto original que se desea traducir a asignar
	 */
	public void setTextoOriginal(String textoOriginal) {
		this.textoOriginal = textoOriginal;
	}

	/**
	 * Obtiene la lista de traducciones, una por cada proveedor consultado.
	 *
	 * @return lista de traducciones, una por cada proveedor consultado
	 */
	public List<TraduccionResponseDTO> getListaTraduccion() {
		return listaTraduccion;
	}

	/**
	 * Establece la lista de traducciones, una por cada proveedor consultado.
	 *
	 * @param listaTraduccion lista de traducciones, una por cada proveedor consultado a asignar
	 */
	public void setListaTraduccion(List<TraduccionResponseDTO> listaTraduccion) {
		this.listaTraduccion = listaTraduccion; // skipcq: JAVA-E1086
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

}
