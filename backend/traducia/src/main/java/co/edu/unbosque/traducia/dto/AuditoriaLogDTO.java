package co.edu.unbosque.traducia.dto;

import java.time.LocalDateTime;

/**
 * Objeto de transferencia de datos que representa la información
 * de un registro de auditoría.
 * <p>
 * Se utiliza para transportar los datos de auditoría desde la capa
 * de servicio hacia las respuestas de la API, sin exponer directamente
 * la entidad de persistencia.
 * </p>
 */
public class AuditoriaLogDTO {

	/** Identificador único del registro de auditoría. */
	private Long id;
	/** Nombre del usuario asociado a la solicitud. */
	private String usuario;
	/** Método HTTP utilizado en la solicitud. */
	private String metodo;
	/** Ruta o URI solicitada. */
	private String ruta;
	/** Código de estado HTTP devuelto en la respuesta. */
	private int status;
	/** Dirección IP de origen de la solicitud. */
	private String ip;
	/** Duración del procesamiento de la solicitud, expresada en milisegundos. */
	private long duracionMs;
	/** Fecha y hora en que se generó el registro de auditoría. */
	private LocalDateTime fecha;

	/**
	 * Construye un DTO vacío.
	 */
	public AuditoriaLogDTO() {

	}

	/**
	 * Construye un DTO con todos los datos de un registro de auditoría.
	 *
	 * @param id         identificador del registro
	 * @param usuario    nombre del usuario asociado a la solicitud
	 * @param metodo     método HTTP utilizado
	 * @param ruta       ruta solicitada
	 * @param status     código de estado HTTP de la respuesta
	 * @param ip         dirección IP desde la que se realizó la solicitud
	 * @param duracionMs duración del procesamiento en milisegundos
	 * @param fecha      fecha y hora en que se registró la solicitud
	 */
	public AuditoriaLogDTO(Long id, String usuario, String metodo, String ruta, int status, String ip, long duracionMs,
			LocalDateTime fecha) {
		this.id = id;
		this.usuario = usuario;
		this.metodo = metodo;
		this.ruta = ruta;
		this.status = status;
		this.ip = ip;
		this.duracionMs = duracionMs;
		this.fecha = fecha;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getMetodo() {
		return metodo;
	}

	public void setMetodo(String metodo) {
		this.metodo = metodo;
	}

	public String getRuta() {
		return ruta;
	}

	public void setRuta(String ruta) {
		this.ruta = ruta;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public long getDuracionMs() {
		return duracionMs;
	}

	public void setDuracionMs(long duracionMs) {
		this.duracionMs = duracionMs;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}

}
